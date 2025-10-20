package org.scoula.gift.service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.scoula.asset.domain.AssetStatusVo;
import org.scoula.asset.mapper.AssetStatusMapper;
import org.scoula.exception.UserNotFoundException;
import org.scoula.gift.domain.RecipientVo;
import org.scoula.gift.domain.StrategyVo;
import org.scoula.gift.dto.AssetGiftRequestDto;
import org.scoula.gift.dto.CategoryGiftRequestDto;
import org.scoula.gift.dto.RecipientGiftRequestDto;
import org.scoula.gift.dto.RecipientTaxDetailDto;
import org.scoula.gift.dto.SimulationRequestDto;
import org.scoula.gift.dto.SimulationResponseDto;
import org.scoula.gift.dto.StrategyResponseDto;
import org.scoula.gift.dto.WillPageResponseDto;
import org.scoula.gift.mapper.RecipientMapper;
import org.scoula.gift.mapper.StrategyMapper;
import org.scoula.user.domain.UserVo;
import org.scoula.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class SimulationServiceImpl implements SimulationService {

	private final RecipientMapper recipientMapper;
	private final StrategyMapper strategyMapper;
	private final AssetStatusMapper assetStatusMapper;
	private final UserMapper userMapper;

	// 가중치 계산 시 사용할 상수
	private static final long WEIGHT_NORMALIZATION_FACTOR = 3_000_000L; // 300만원당 1점
	private static final int MAX_WEIGHT_SCORE = 120;
	private static final int TOP_STRATEGIES_LIMIT = 7;

	@Override
	public SimulationResponseDto runGiftTaxSimulation(SimulationRequestDto requestDto, String email) {
		TaxCalculationResult taxResult = calculateGiftTaxInternal(requestDto);
		List<StrategyResponseDto> strategies = generateTaxSavingStrategies(requestDto, taxResult);
		return new SimulationResponseDto(
			taxResult.getTotalEstimatedTax(),
			taxResult.getRecipientTaxDetails(),
			strategies);
	}

	private List<StrategyResponseDto> generateTaxSavingStrategies(SimulationRequestDto requestDto,
		TaxCalculationResult taxResult) {
		// 1. 조건에 맞는 모든 전략 규칙을 임시 리스트에 수집
		List<StrategyVo> matchedRules = new ArrayList<>();
		List<StrategyVo> allRules = strategyMapper.findAll();
		Map<String, List<StrategyVo>> rulesByCategory = allRules.stream()
			.collect(Collectors.groupingBy(StrategyVo::getRuleCategory));

		checkTotalAssetRules(matchedRules, rulesByCategory.get("총 자산 규모"), taxResult);
		checkRecipientRules(matchedRules, rulesByCategory.get("수증자 유형"), taxResult, requestDto);
		checkGiftHistoryRules(matchedRules, rulesByCategory.get("기존 증여이력"), taxResult);
		checkTaxPayerRules(matchedRules, rulesByCategory.get("증여세 납부자"), taxResult);
		checkAssetTypeRules(matchedRules, rulesByCategory.get("자산 유형"), requestDto);

		// 2. 수집된 각 전략의 동적 가중치를 계산
		List<WeightedStrategy> weightedStrategies = matchedRules.stream()
			.map(rule -> new WeightedStrategy(rule, calculateDynamicWeight(rule, taxResult)))
			.collect(Collectors.toList());

		log.info("===== 동적 가중치 계산 결과 (정렬 전) =====");
		weightedStrategies.forEach(ws ->
			log.info("[전략 가중치] 코드: {}, 레벨: {}, 가중치: {}",
				ws.getRule().getStrategyCode(),
				ws.getRule().getPriorityLevel(),
				String.format("%.2f", ws.getDynamicWeight()))
		);
		log.info("========================================");

		// 3. 2단계 정렬 로직 적용
		Comparator<WeightedStrategy> multiLevelComparator = Comparator
			.comparingInt((WeightedStrategy ws) -> ws.getRule().getPriorityLevel())
			.thenComparing(Comparator.comparingDouble(WeightedStrategy::getDynamicWeight).reversed());

		// 4. 정렬 후, 상위 7개만 선택하여 최종 DTO로 변환
		log.info("===== 최종 추천 Top 7 전략 (정렬 후) =====");
		List<StrategyResponseDto> top7Strategies = weightedStrategies.stream()
			.sorted(multiLevelComparator)
			.limit(TOP_STRATEGIES_LIMIT)
			// [로깅 추가] peek()를 사용하여 최종 선택된 7개의 전략 정보를 로그로 출력합니다.
			.peek(ws -> log.info("[최종 선택] 코드: {}, 레벨: {}, 가중치: {}",
				ws.getRule().getStrategyCode(),
				ws.getRule().getPriorityLevel(),
				String.format("%.2f", ws.getDynamicWeight())))
			.map(ws -> new StrategyResponseDto(ws.getRule().getRuleCategory(), ws.getRule().getMessage()))
			.collect(Collectors.toList());
		log.info("========================================");
		return top7Strategies;
	}

	// ... (calculateGiftTaxInternal, calculateDynamicWeight 등 다른 메서드는 동일)
	private TaxCalculationResult calculateGiftTaxInternal(SimulationRequestDto requestDto) {
		long totalCurrentGiftAmount = 0L;
		long totalEstimatedTax = 0L;
		List<RecipientTaxDetailDto> recipientTaxDetails = new ArrayList<>();
		List<RecipientVo> recipientsInSim = new ArrayList<>();
		Map<Integer, RecipientCalculationDetail> detailsByRecipient = new HashMap<>();

		for (RecipientGiftRequestDto giftRequest : requestDto.getSimulationList()) {
			RecipientVo recipient = recipientMapper.findById(giftRequest.getRecipientId());
			if (recipient == null)
				continue;
			recipientsInSim.add(recipient);

			long currentGiftAmount = giftRequest.getCategoriesToGift().stream()
				.flatMap(category -> category.getAssets().stream())
				.mapToLong(AssetGiftRequestDto::getGiftAmount).sum();
			totalCurrentGiftAmount += currentGiftAmount;

			long priorGiftAmount = (recipient.getPriorGiftAmount() != null) ? recipient.getPriorGiftAmount() : 0L;
			long totalCumulativeGiftAmount = currentGiftAmount + priorGiftAmount;
			long deductionAmount = getDeductionAmount(recipient);

			long cumulativeTaxableBase = Math.max(0, totalCumulativeGiftAmount - deductionAmount);
			long taxOnCumulative = calculateTax(cumulativeTaxableBase);

			long priorTaxableBase = Math.max(0, priorGiftAmount - deductionAmount);
			long taxOnPrior = calculateTax(priorTaxableBase);

			long finalTaxForCurrentGift = taxOnCumulative - taxOnPrior;

			double marginalTaxRate = getMarginalTaxRate(cumulativeTaxableBase);
			long surcharge = 0;

			if ("손자녀".equals(recipient.getRelationship())) {
				surcharge = (long)(finalTaxForCurrentGift * 0.3);
				finalTaxForCurrentGift += surcharge;
			}

			recipientTaxDetails.add(
				new RecipientTaxDetailDto(recipient.getRecipientName(), currentGiftAmount, finalTaxForCurrentGift));
			totalEstimatedTax += finalTaxForCurrentGift;

			detailsByRecipient.put(recipient.getRecipientId(),
				new RecipientCalculationDetail(recipient, marginalTaxRate, surcharge, finalTaxForCurrentGift));
		}
		return new TaxCalculationResult(totalEstimatedTax, recipientTaxDetails, recipientsInSim, totalCurrentGiftAmount,
			detailsByRecipient);
	}

	private double calculateDynamicWeight(StrategyVo rule, TaxCalculationResult taxResult) {
		Map<Integer, RecipientCalculationDetail> detailsMap = taxResult.getDetailsByRecipient();
		switch (rule.getStrategyCode()) {
			case "NOT_EXIST_SPOUSE":
				double maxMarginalRate = detailsMap.values()
					.stream()
					.mapToDouble(RecipientCalculationDetail::getMarginalTaxRate)
					.max()
					.orElse(0.0);
				long potentialSaving = (long)(600_000_000L * maxMarginalRate);
				return Math.min(MAX_WEIGHT_SCORE, (double)potentialSaving / WEIGHT_NORMALIZATION_FACTOR);
			case "EXIST_GRANDCHILD":
				long totalSurcharge = detailsMap.values()
					.stream()
					.mapToLong(RecipientCalculationDetail::getSurcharge)
					.sum();
				return Math.min(MAX_WEIGHT_SCORE, (double)totalSurcharge / WEIGHT_NORMALIZATION_FACTOR);
			case "EXIST_GIFTER_PAYS_TAX":
				long totalTaxPaidByGifter = detailsMap.values().stream()
					.filter(d -> "본인".equals(d.getRecipient().getGiftTaxPayer()))
					.mapToLong(RecipientCalculationDetail::getFinalTax)
					.sum();
				double gifterMarginalRate = detailsMap.values().stream()
					.filter(d -> "본인".equals(d.getRecipient().getGiftTaxPayer()))
					.mapToDouble(RecipientCalculationDetail::getMarginalTaxRate).findFirst().orElse(0.0);
				long additionalTax = (long)(totalTaxPaidByGifter * gifterMarginalRate);
				return Math.min(MAX_WEIGHT_SCORE, (double)additionalTax / WEIGHT_NORMALIZATION_FACTOR);
			case "CUMULATIVE_GIFT_EXCEEDS_DEDUCTION":
				long totalExceededAmount = detailsMap.values().stream()
					.mapToLong(d -> Math.max(0,
						(d.getRecipient().getPriorGiftAmount() != null ? d.getRecipient().getPriorGiftAmount() : 0L)
							- getDeductionAmount(d.getRecipient())))
					.sum();
				double maxRateOnExceeded = detailsMap.values().stream()
					.filter(d ->
						(d.getRecipient().getPriorGiftAmount() != null ? d.getRecipient().getPriorGiftAmount() : 0L)
							> getDeductionAmount(d.getRecipient()))
					.mapToDouble(RecipientCalculationDetail::getMarginalTaxRate).max().orElse(0.0);
				long taxOnExceeded = (long)(totalExceededAmount * maxRateOnExceeded);
				return Math.min(MAX_WEIGHT_SCORE, (double)taxOnExceeded / WEIGHT_NORMALIZATION_FACTOR);
			default:
				return rule.getBaseWeight();
		}
	}

	private void checkTotalAssetRules(List<StrategyVo> matchedRules, List<StrategyVo> rules,
		TaxCalculationResult taxResult) {
		if (rules == null)
			return;
		long totalGiftAmount = taxResult.getTotalCurrentGiftAmount();
		if (totalGiftAmount >= 5_000_000_000L) {
			rules.stream()
				.filter(r -> "TOTAL_ASSET_GT_5B".equals(r.getStrategyCode()))
				.findFirst()
				.ifPresent(matchedRules::add);
		} else if (totalGiftAmount >= 1_000_000_000L) {
			rules.stream()
				.filter(r -> "TOTAL_ASSET_GT_1B".equals(r.getStrategyCode()))
				.findFirst()
				.ifPresent(matchedRules::add);
		}
	}

	/**
	 * [수정] 수증자 관련 모든 전략을 통합하여 검사하는 메서드
	 */
	private void checkRecipientRules(List<StrategyVo> matchedRules, List<StrategyVo> rules,
		TaxCalculationResult taxResult, SimulationRequestDto requestDto) {
		if (rules == null)
			return;

		List<RecipientVo> recipients = taxResult.getRecipientsInSim();
		boolean hasSpouse = recipients.stream().anyMatch(r -> "배우자".equals(r.getRelationship()));
		boolean hasGrandChild = recipients.stream().anyMatch(r -> "손자녀".equals(r.getRelationship()));
		boolean hasMinor = recipients.stream().anyMatch(r -> isMinor(r.getBirthDate()));
		boolean hasUnmarriedChild = recipients.stream()
			.anyMatch(r -> "자녀".equals(r.getRelationship()) && (r.getIsMarried() == null || !r.getIsMarried()));
		boolean hasRealEstate = requestDto.getSimulationList()
			.stream()
			.flatMap(r -> r.getCategoriesToGift().stream())
			.anyMatch(cat -> "1".equals(cat.getAssetCategoryCode()));

		for (StrategyVo rule : rules) {
			switch (rule.getStrategyCode()) {
				// 기존 '수증자 유형' 전략들
				case "NOT_EXIST_SPOUSE":
					if (!hasSpouse)
						matchedRules.add(rule);
					break;
				case "EXIST_GRANDCHILD":
					if (hasGrandChild)
						matchedRules.add(rule);
					break;
				case "EXIST_MINOR_CHILD":
					if (hasMinor)
						matchedRules.add(rule);
					break;

				// 기존 '수증자 결혼여부'에서 통합된 전략들
				case "EXIST_UNMARRIED_CHILD":
					if (hasUnmarriedChild)
						matchedRules.add(rule);
					break;
				case "HAS_GIFT_REAL_ESTATE_AND_EXIST_GRANDCHILD":
					if (hasRealEstate && hasGrandChild)
						matchedRules.add(rule);
					break;
				case "HAS_GIFT_REAL_ESTATE_AND_EXIST_UNMARRIED_CHILD":
					if (hasRealEstate && hasUnmarriedChild)
						matchedRules.add(rule);
					break;
			}
		}
	}

	private void checkGiftHistoryRules(List<StrategyVo> matchedRules, List<StrategyVo> rules,
		TaxCalculationResult taxResult) {
		if (rules == null)
			return;
		for (StrategyVo rule : rules) {
			switch (rule.getStrategyCode()) {
				case "PRIOR_GIFT_EXISTS":
					if (taxResult.getRecipientsInSim()
						.stream()
						.anyMatch(r -> r.getHasPriorGift() != null && r.getHasPriorGift()))
						matchedRules.add(rule);
					break;
				case "CUMULATIVE_GIFT_EXCEEDS_DEDUCTION":
					if (taxResult.getRecipientsInSim().stream().anyMatch(r -> {
						long prior = (r.getPriorGiftAmount() != null) ? r.getPriorGiftAmount() : 0L;
						return prior > getDeductionAmount(r);
					}))
						matchedRules.add(rule);
					break;
			}
		}
	}

	private void checkTaxPayerRules(List<StrategyVo> matchedRules, List<StrategyVo> rules,
		TaxCalculationResult taxResult) {
		if (rules == null)
			return;
		boolean gifterPaysTax = taxResult.getRecipientsInSim().stream().anyMatch(r -> "본인".equals(r.getGiftTaxPayer()));
		if (gifterPaysTax) {
			rules.stream()
				.filter(r -> "EXIST_GIFTER_PAYS_TAX".equals(r.getStrategyCode()))
				.findFirst()
				.ifPresent(matchedRules::add);
		}
	}

	private void checkAssetTypeRules(List<StrategyVo> matchedRules, List<StrategyVo> rules,
		SimulationRequestDto requestDto) {
		if (rules == null)
			return;
		Set<String> assetCategoryCodes = requestDto.getSimulationList()
			.stream()
			.flatMap(r -> r.getCategoriesToGift().stream())
			.map(CategoryGiftRequestDto::getAssetCategoryCode)
			.collect(Collectors.toSet());
		boolean hasRealEstate = assetCategoryCodes.contains("1");
		boolean hasCashOrSavings = assetCategoryCodes.contains("2") || assetCategoryCodes.contains("3");
		List<Integer> businessAssetIds = requestDto.getSimulationList().stream()
			.flatMap(r -> r.getCategoriesToGift().stream()).filter(c -> "5".equals(c.getAssetCategoryCode()))
			.flatMap(c -> c.getAssets().stream()).map(AssetGiftRequestDto::getAssetId).collect(Collectors.toList());
		boolean isGiftingToSoleProprietorship = false;
		boolean isGiftingToCorporation = false;
		if (!businessAssetIds.isEmpty()) {
			for (Integer assetId : businessAssetIds) {
				AssetStatusVo assetInfo = assetStatusMapper.findAssetStatusById(assetId);
				if (assetInfo != null && assetInfo.getBusinessType() != null) {
					if ("개인 사업자".equals(assetInfo.getBusinessType()))
						isGiftingToSoleProprietorship = true;
					if ("법인 사업자".equals(assetInfo.getBusinessType()))
						isGiftingToCorporation = true;
				}
			}
		}
		for (StrategyVo rule : rules) {
			switch (rule.getStrategyCode()) {
				case "HAS_BIZ_TO_SOLE_PROPRIETORSHIP":
					if (isGiftingToSoleProprietorship)
						matchedRules.add(rule);
					break;
				case "HAS_BIZ_TO_CORPORATION":
					if (isGiftingToCorporation)
						matchedRules.add(rule);
					break;
				case "HAS_REAL_ESTATE_APPRECIATION":
				case "HAS_REAL_ESTATE_DEBT_SUCCESSION":
					if (hasRealEstate)
						matchedRules.add(rule);
					break;
				case "HAS_CASH":
					if (hasCashOrSavings)
						matchedRules.add(rule);
					break;
			}
		}
	}

	// [수정] checkMaritalStatusRules 메서드는 삭제되었습니다.

	private long calculateTax(long taxableBase) {
		if (taxableBase <= 100_000_000L)
			return (long)(taxableBase * 0.10);
		if (taxableBase <= 500_000_000L)
			return (long)(taxableBase * 0.20) - 10_000_000L;
		if (taxableBase <= 1_000_000_000L)
			return (long)(taxableBase * 0.30) - 60_000_000L;
		if (taxableBase <= 3_000_000_000L)
			return (long)(taxableBase * 0.40) - 160_000_000L;
		return (long)(taxableBase * 0.50) - 460_000_000L;
	}

	private double getMarginalTaxRate(long taxableBase) {
		if (taxableBase <= 100_000_000L)
			return 0.10;
		if (taxableBase <= 500_000_000L)
			return 0.20;
		if (taxableBase <= 1_000_000_000L)
			return 0.30;
		if (taxableBase <= 3_000_000_000L)
			return 0.40;
		return 0.50;
	}

	private long getDeductionAmount(RecipientVo recipient) {
		if (recipient == null || recipient.getRelationship() == null)
			return 0L;
		return switch (recipient.getRelationship()) {
			case "배우자" -> 600_000_000L;
			case "자녀", "손자녀" -> isMinor(recipient.getBirthDate()) ? 20_000_000L : 50_000_000L;
			case "기타", "형제자매" -> 10_000_000L;
			default -> 0L;
		};
	}

	private boolean isMinor(Date birthDate) {
		if (birthDate == null)
			return false;
		return Period.between(birthDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), LocalDate.now())
			.getYears() < 19;
	}

	@Getter
	@AllArgsConstructor
	private static class TaxCalculationResult {
		private final long totalEstimatedTax;
		private final List<RecipientTaxDetailDto> recipientTaxDetails;
		private final List<RecipientVo> recipientsInSim;
		private final long totalCurrentGiftAmount;
		private final Map<Integer, RecipientCalculationDetail> detailsByRecipient;
	}

	@Getter
	@AllArgsConstructor
	private static class RecipientCalculationDetail {
		private final RecipientVo recipient;
		private final double marginalTaxRate;
		private final long surcharge;
		private final long finalTax;
	}

	@Getter
	@AllArgsConstructor
	private static class WeightedStrategy {
		private final StrategyVo rule;
		private final double dynamicWeight;
	}

	@Transactional(readOnly = true)
	public WillPageResponseDto getUserInfoForWillPage(String email) {
		UserVo userVo = userMapper.findByEmail(email);
		if (userVo == null)
			throw new UserNotFoundException("사용자를 찾을 수 없습니다. email: " + email);
		Date birthDate = userVo.getBirth();
		String formattedBirthDate = null;
		if (birthDate != null) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			formattedBirthDate = sdf.format(birthDate);
		}
		return WillPageResponseDto.builder()
			.email(userVo.getEmail())
			.name(userVo.getUserName())
			.birth(formattedBirthDate)
			.build();
	}
}
