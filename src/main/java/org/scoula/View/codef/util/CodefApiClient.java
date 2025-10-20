package org.scoula.View.codef.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;
import org.scoula.View.codef.service.CodefTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;

/**
 * CODEF API와 직접 통신하는 클라이언트 클래스입니다.
 * 토큰 발급, ConnectedId 생성, 계좌 정보 조회 등 API 호출 로직을 담당합니다.
 */
@Component
@Log4j2
public class CodefApiClient {

	// application-dev.properties에서 주입받는 CODEF 설정 값들
	@Value("${codef.oauth.domain}")
	private String oauthDomain;
	@Value("${codef.api.domain}")
	private String apiDomain;
	@Value("${codef.client.id}")
	private String clientId;
	@Value("${codef.client.secret}")
	private String clientSecret;
	@Value("${codef.public.key}")
	private String publicKey;

	// CODEF API 엔드포인트 경로 상수
	private static final String GET_TOKEN_PATH = "/oauth/token";
	private static final String CREATE_ACCOUNT_PATH = "/v1/account/create";
	private static final String KR_BK_1_P_001_PATH = "/v1/kr/bank/p/account/account-list";
	private static final ObjectMapper mapper = new ObjectMapper();

	/** 토큰 관리를 담당하는 서비스 (순환 참조 해결을 위해 setter로 주입) */
	private CodefTokenService codefTokenService;

	/**
	 * CodefTokenService의 인스턴스를 설정합니다. (순환 참조 해결용)
	 * @param codefTokenService 주입할 서비스 객체
	 */
	public void setCodefTokenService(CodefTokenService codefTokenService) {
		this.codefTokenService = codefTokenService;
	}

	/**
	 * CodefTokenService를 통해 현재 유효한 Access Token을 가져옵니다.
	 * @return Access Token 문자열
	 */
	private String getAccessToken() {
		if (codefTokenService != null) {
			return codefTokenService.getAccessToken();
		}
		return null;
	}

	/**
	 * CODEF OAuth 서버로부터 새로운 Access Token을 발급받습니다.
	 * @return 발급된 토큰 정보를 담은 Map 객체
	 */
	public Map<String, Object> publishToken() {
		BufferedReader br = null;
		try {
			URL url = new URL(oauthDomain + GET_TOKEN_PATH);
			String params = "grant_type=client_credentials&scope=read";

			HttpURLConnection con = (HttpURLConnection)url.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			// Basic 인증 헤더 생성
			String auth = clientId + ":" + clientSecret;
			String authHeader = "Basic " + Base64.encodeBase64String(auth.getBytes());
			con.setRequestProperty("Authorization", authHeader);
			con.setDoInput(true);
			con.setDoOutput(true);

			try (OutputStream os = con.getOutputStream()) {
				os.write(params.getBytes());
			}

			int responseCode = con.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK) {
				log.error("Failed to get access token. Response Code: {}", responseCode);
				return null;
			}

			StringBuilder responseStr = new StringBuilder();
			br = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			while ((inputLine = br.readLine()) != null) {
				responseStr.append(inputLine);
			}

			String decoded = URLDecoder.decode(responseStr.toString(), "UTF-8");
			return mapper.readValue(decoded, Map.class);

		} catch (Exception e) {
			log.error("Error while publishing token: {}", e.getMessage(), e);
			return null;
		} finally {
			if (br != null)
				try {
					br.close();
				} catch (IOException e) {
					log.error("Error closing reader", e);
				}
		}
	}

	/**
	 * CODEF API를 호출하여 ConnectedId를 생성합니다.
	 * @param bodyMap 요청 본문에 포함될 데이터
	 * @return API 응답 결과를 담은 Map 객체
	 */
	public Map<String, Object> createConnectedId(Map<String, Object> bodyMap) {
		BufferedReader br = null;
		try {
			URL url = new URL(apiDomain + CREATE_ACCOUNT_PATH);
			HttpURLConnection con = (HttpURLConnection)url.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Authorization", "Bearer " + getAccessToken());
			con.setDoInput(true);
			con.setDoOutput(true);

			String jsonInputString = mapper.writeValueAsString(bodyMap);
			try (OutputStream os = con.getOutputStream()) {
				os.write(jsonInputString.getBytes("UTF-8"));
			}

			int responseCode = con.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK) {
				log.error("Failed to create ConnectedId. Response Code: {}", responseCode);
				try (BufferedReader errorBr = new BufferedReader(new InputStreamReader(con.getErrorStream()))) {
					StringBuilder errorResponse = new StringBuilder();
					String errorLine;
					while ((errorLine = errorBr.readLine()) != null) {
						errorResponse.append(errorLine);
					}
					log.error("Error Response: {}", errorResponse.toString());
				}
				return null;
			}

			StringBuilder responseStr = new StringBuilder();
			br = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			while ((inputLine = br.readLine()) != null) {
				responseStr.append(inputLine);
			}

			String decoded = URLDecoder.decode(responseStr.toString(), "UTF-8");
			return mapper.readValue(decoded, Map.class);

		} catch (Exception e) {
			log.error("Error while creating ConnectedId: {}", e.getMessage(), e);
			return null;
		} finally {
			if (br != null)
				try {
					br.close();
				} catch (IOException e) {
					log.error("Error closing reader", e);
				}
		}
	}

	/**
	 * CODEF API를 호출하여 특정 기관의 계좌 목록 정보를 조회합니다.
	 * @param connectedId 조회할 사용자의 ConnectedId
	 * @param organization 조회할 기관 코드
	 * @return API 응답 결과를 담은 Map 객체
	 */
	public Map<String, Object> getAccountInfo(String connectedId, String organization) {
		BufferedReader br = null;
		try {
			URL url = new URL(apiDomain + KR_BK_1_P_001_PATH);
			HttpURLConnection con = (HttpURLConnection)url.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Authorization", "Bearer " + getAccessToken());
			con.setDoInput(true);
			con.setDoOutput(true);

			Map<String, Object> bodyMap = new HashMap<>();
			bodyMap.put("connectedId", connectedId);
			bodyMap.put("organization", organization);

			String jsonInputString = mapper.writeValueAsString(bodyMap);
			try (OutputStream os = con.getOutputStream()) {
				os.write(jsonInputString.getBytes("UTF-8"));
			}

			int responseCode = con.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK) {
				log.error("Failed to get account info. Response Code: {}", responseCode);
				try (BufferedReader errorBr = new BufferedReader(new InputStreamReader(con.getErrorStream()))) {
					StringBuilder errorResponse = new StringBuilder();
					String errorLine;
					while ((errorLine = errorBr.readLine()) != null) {
						errorResponse.append(errorLine);
					}
					log.error("Error Response: {}", errorResponse.toString());
				}
				return null;
			}

			StringBuilder responseStr = new StringBuilder();
			br = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			while ((inputLine = br.readLine()) != null) {
				responseStr.append(inputLine);
			}

			String decoded = URLDecoder.decode(responseStr.toString(), "UTF-8");
			return mapper.readValue(decoded, Map.class);

		} catch (Exception e) {
			log.error("Error while getting account info: {}", e.getMessage(), e);
			return null;
		} finally {
			if (br != null)
				try {
					br.close();
				} catch (IOException e) {
					log.error("Error closing reader", e);
				}
		}
	}

	/**
	 * 주어진 평문을 RSA 공개키로 암호화합니다.
	 * @param plainText 암호화할 문자열
	 * @param base64PublicKey Base64로 인코딩된 공개키 문자열
	 * @return Base64로 인코딩된 암호문 문자열
	 * @throws Exception 암호화 과정에서 발생할 수 있는 예외
	 */
	public String encryptRSA(String plainText, String base64PublicKey) throws Exception {
		byte[] bytePublicKey = java.util.Base64.getDecoder().decode(base64PublicKey);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(bytePublicKey));

		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		byte[] bytePlain = plainText.getBytes("UTF-8");
		log.info("Plain text byte length: {}", bytePlain.length);
		return java.util.Base64.getEncoder().encodeToString(cipher.doFinal(bytePlain));
	}

	/**
	 * 설정 파일에서 주입받은 공개키를 반환합니다.
	 * @return 공개키 문자열
	 */
	public String getPublicKey() {
		return publicKey;
	}
}