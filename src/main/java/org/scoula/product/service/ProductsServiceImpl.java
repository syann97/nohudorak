package org.scoula.product.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.scoula.product.domain.DepositVo;
import org.scoula.product.domain.FundDailyReturnVo;
import org.scoula.product.domain.FundVo;
import org.scoula.product.domain.GoldVo;
import org.scoula.product.domain.MortgageVo;
import org.scoula.product.domain.ProductVo;
import org.scoula.product.domain.SavingVo;
import org.scoula.product.domain.TrustVo;
import org.scoula.product.dto.DepositSavingOptionDto;
import org.scoula.product.dto.FundSimpleOptionDto;
import org.scoula.product.dto.MortgageOptionDto;
import org.scoula.product.dto.ProductDto;
import org.scoula.product.mapper.FundDailyReturnMapper;
import org.scoula.product.mapper.ProductMapper;
import org.scoula.product.struct.ProductVoToMapper;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductsServiceImpl implements ProductService {
	private final ProductMapper productMapper;
	private final FundDailyReturnMapper fundDailyReturnMapper;

	/**
	 * 전체 금융상품 목록 조회
	 * VO 객체를 DTO로 변환하여 카테고리별 리스트를 Map으로 반환합니다.
	 *
	 * @return 카테고리별 금융상품 목록 Map
	 */
	public Map<String, List<? extends ProductDto>> findAllProducts() {
		List<? extends ProductVo> list = productMapper.findAllProduct();

		List<ProductDto<DepositSavingOptionDto>> depositList = new ArrayList<>();
		List<ProductDto<DepositSavingOptionDto>> savingList = new ArrayList<>();
		List<ProductDto<MortgageOptionDto>> mortgageList = new ArrayList<>();
		List<ProductDto<FundSimpleOptionDto>> fundList = new ArrayList<>();
		List<ProductDto<Void>> goldList = new ArrayList<>();
		List<ProductDto<Void>> trustList = new ArrayList<>();

		// VO -> DTO
		for (ProductVo p : list) {
			if (p instanceof DepositVo) {
				// 예금 상품 처리 VO -> DTO
				depositList.add(ProductVoToMapper.toDepositSimpleDto((DepositVo)p));
			} else if (p instanceof SavingVo) {
				savingList.add(ProductVoToMapper.toSavingSimpleDto((SavingVo)p));
			} else if (p instanceof MortgageVo) {
				mortgageList.add(ProductVoToMapper.toMortgageSimpleDto((MortgageVo)p));
			} else if (p instanceof FundVo) {
				// 펀드 상품 처리 VO -> DTO
				fundList.add(ProductVoToMapper.toFundSimpleDto((FundVo)p));
			} else if (p instanceof GoldVo) {
				goldList.add(ProductVoToMapper.toGoldSimpleDto((GoldVo)p));
			} else if (p instanceof TrustVo) {
				trustList.add(ProductVoToMapper.toTrustSimpleDto((TrustVo)p));
			}
		}

		// 카테고리별 Map으로 반환
		return Map.of(
			"deposit", depositList,
			"saving", savingList,
			"mortgage", mortgageList,
			"fund", fundList,
			"gold", goldList,
			"trust", trustList
		);
	}

	/**
	 * 상품 코드로 상세정보 조회
	 *
	 * @param finPrdtCd 조회할 금융상품 코드
	 * @return 해당 금융상품 VO
	 * @throws NoSuchElementException 존재하지 않는 상품인 경우
	 */
	public ProductVo getProductDetail(String finPrdtCd) {
		Map<String, Object> result = new HashMap<>();
		ProductVo product = productMapper.findProductDetail(finPrdtCd);
		result.put("product", product);

		if (product == null) {
			throw new NoSuchElementException("해당 상품을 찾을 수 없습니다: " + finPrdtCd);
		}
		return product;
	}

	/**
	 * 상품 코드로 상품명 조회
	 *
	 * @param finPrdtCd 조회할 금융상품 코드
	 * @return 금융상품명
	 */
	@Override
	public String getProductNameByCode(String finPrdtCd) {
		return productMapper.findProductDetail(finPrdtCd).getFinPrdtNm();
	}

	/**
	 * 펀드 코드로 해당 펀드의 일별 수익률 조회
	 *
	 * @param finPrdtCd 조회할 펀드 코드
	 * @return 해당 펀드의 일별 수익률 리스트
	 */
	@Override
	public List<FundDailyReturnVo> getFundDailyReturnByCode(String finPrdtCd) {
		return fundDailyReturnMapper.findByFundCode(finPrdtCd);
	}
}
