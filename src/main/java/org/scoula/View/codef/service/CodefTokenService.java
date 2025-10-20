package org.scoula.View.codef.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.scoula.View.codef.dto.ConnectedIdRequestDto;
import org.scoula.View.codef.util.CodefApiClient;
import org.scoula.asset.domain.AssetStatusVo;
import org.scoula.asset.dto.AssetStatusRequestDto;
import org.scoula.asset.service.AssetStatusService;
import org.scoula.user.service.UserService;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * CODEF API와의 통신을 관리하는 서비스 클래스.
 * Access Token 발급 및 관리, ConnectedId 생성, 계좌 정보 조회 및 저장을 담당합니다.
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class CodefTokenService {

	private final CodefApiClient codefApiClient;
	private final AssetStatusService assetStatusService;
	private final UserService userService;

	// TODO: Access Token을 안전하게 저장하고 관리하는 로직 추가 필요
	/** CODEF API Access Token */
	private String accessToken;

	/** Access Token의 만료 시간을 저장 (Unix 타임스탬프, 밀리초) */
	private long tokenExpiryTime;

	/**
	 * 의존성 주입 후 초기화 작업을 수행합니다.
	 * CodefApiClient에 현재 서비스의 인스턴스를 설정하여 상호 참조를 해결합니다.
	 */
	@PostConstruct
	public void init() {
		codefApiClient.setCodefTokenService(this);
	}

	/**
	 * 유효한 CODEF Access Token을 반환합니다.
	 * 토큰이 없거나 만료된 경우, 자동으로 새로운 토큰을 발급받아 갱신합니다.
	 * @return 유효한 Access Token 문자열
	 */
	public String getAccessToken() {
		// TODO: 토큰 만료 여부 확인 및 갱신 로직 추가
		if (accessToken == null || isTokenExpired()) {
			log.info("Access Token is null or expired. Publishing new token...");
			Map<String, Object> tokenMap = codefApiClient.publishToken();
			if (tokenMap != null && tokenMap.containsKey("access_token")) {
				this.accessToken = (String)tokenMap.get("access_token");
				// 토큰 유효 기간 (초)
				int expiresIn = (Integer)tokenMap.get("expires_in");
				this.tokenExpiryTime = System.currentTimeMillis() + (expiresIn * 1000L); // 현재 시간 + 유효 기간
				log.info("New Access Token published successfully.");
				log.info(accessToken);
			} else {
				log.error("Failed to publish new Access Token.");
				return null;
			}
		}
		return accessToken;
	}

	/**
	 * 현재 저장된 Access Token이 만료되었는지 확인합니다.
	 * @return 만료되었으면 true, 아니면 false
	 */
	private boolean isTokenExpired() {
		// 만료 시간보다 현재 시간이 크면 토큰 만료
		return System.currentTimeMillis() >= tokenExpiryTime;
	}

	/**
	 * 사용자의 금융사 계정 정보를 바탕으로 CODEF ConnectedId를 생성합니다.
	 * 이 과정에서 계정 비밀번호를 암호화하여 전송합니다.
	 * @param requestDto ConnectedId 생성에 필요한 계정 정보 목록
	 * @return CODEF API로부터 반환된 결과 맵
	 */
	public Map<String, Object> createConnectedId(ConnectedIdRequestDto requestDto) {
		try {
			// 리스트 내부의 account 객체 수정 (id/password 암호화)
			for (Map<String, Object> account : requestDto.getAccountList()) {
				if (account.containsKey("password")) {
					String encryptedPassword = codefApiClient.encryptRSA((String)account.get("password"),
							codefApiClient.getPublicKey())
						.replaceAll("\n", "");
					account.put("password", encryptedPassword);
				}
				log.info("🔐 암호화 후 account: {}", account); // ✅ 추가
			}

			// 계정 리스트 통째로 담은 요청
			Map<String, Object> bodyMap = new HashMap<>();
			bodyMap.put("accountList", requestDto.getAccountList());

			return codefApiClient.createConnectedId(bodyMap);

		} catch (Exception e) {
			log.error("Error while creating ConnectedId: {}", e.getMessage(), e);
			return null;
		}
	}

	/**
	 * 발급받은 ConnectedId를 사용하여 사용자의 계좌 정보를 조회하고,
	 * AssetStatusService를 통해 자산 현황을 갱신합니다.
	 * 총자산 계산 및 업데이트는 AssetStatusService가 담당합니다.
	 *
	 * @param userEmail   자산 정보를 저장할 사용자의 이메일
	 * @param connectedId 계좌 정보 조회를 위한 ConnectedId
	 */
	public void saveAccountInfo(String userEmail, String connectedId) {
		String organization = "0004"; // 은행 코드 (0004는 예시)
		log.info("📥 계좌 정보 조회 요청: connectedId={}, organization={}", connectedId, organization);

		Map<String, Object> result = getAccountInfo(connectedId, organization);
		if (result == null || !result.containsKey("data")) {
			log.error("❌ CODEF 계좌 응답 오류 또는 data 없음");
			throw new RuntimeException("CODEF에서 계좌 정보를 받아올 수 없습니다.");
		}

		Map<String, Object> data = (Map<String, Object>)result.get("data");
		List<Map<String, Object>> resDepositTrust = (List<Map<String, Object>>)data.get("resDepositTrust");

		// 1. 기존 '예적금' 자산을 AssetStatusService를 통해 모두 삭제합니다.
		//    삭제 후 총자산 업데이트는 AssetStatusService가 내부적으로 처리합니다.
		List<AssetStatusVo> existingAssets = assetStatusService.getFullAssetStatusByEmail(userEmail);
		for (AssetStatusVo vo : existingAssets) {
			if ("2".equals(vo.getAssetCategoryCode())) { // "2"가 예적금 카테고리
				// deleteAssetStatus를 호출하면 내부적으로 updateUserAssetSummary가 호출됩니다.
				assetStatusService.deleteAssetStatus(vo.getAssetId(), userEmail);
			}
		}
		log.info("기존 예적금 자산 삭제 요청 완료.");

		// CODEF에서 가져온 계좌가 없을 경우 여기서 로직이 종료될 수 있습니다.
		if (resDepositTrust == null || resDepositTrust.isEmpty()) {
			log.info("🔎 CODEF에서 가져온 새 예금/신탁 내역이 없어, 기존 자산 삭제 후 종료합니다.");
			return;
		}

		// 2. CODEF에서 가져온 새 계좌를 AssetStatusService를 통해 추가합니다.
		//    추가 후 총자산 업데이트 역시 AssetStatusService가 내부적으로 처리합니다.
		for (Map<String, Object> account : resDepositTrust) {
			try {
				AssetStatusRequestDto asset = new AssetStatusRequestDto();
				asset.setAssetCategoryCode("2");
				asset.setAssetName((String)account.get("resAccountName"));
				asset.setAmount(Long.parseLong((String)account.get("resAccountBalance")));
				asset.setBusinessType(null);

				// addAssetStatus를 호출하면 내부적으로 updateUserAssetSummary가 호출됩니다.
				assetStatusService.addAssetStatus(userEmail, asset);

			} catch (Exception e) {
				log.error("❗ 신규 계좌 저장 요청 실패: {}", e.getMessage(), e);
			}
		}
		log.info("새 예적금 자산 추가 요청 완료.");
	}

	/**
	 * CodefApiClient를 통해 특정 기관의 계좌 정보를 조회합니다.
	 * @param connectedId 조회할 사용자의 ConnectedId
	 * @param organization 조회할 기관 코드
	 * @return CODEF API로부터 반환된 결과 맵
	 */
	public Map<String, Object> getAccountInfo(String connectedId, String organization) {
		return codefApiClient.getAccountInfo(connectedId, organization);
	}
}
