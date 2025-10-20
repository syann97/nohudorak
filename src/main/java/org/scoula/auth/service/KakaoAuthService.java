package org.scoula.auth.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.scoula.auth.dto.KakaoLoginResponseDto;
import org.scoula.auth.dto.KakaoTokenResponseDto;
import org.scoula.auth.dto.KakaoUserInfoDto;
import org.scoula.auth.dto.RefreshTokenDto;
import org.scoula.auth.dto.TokenRefreshResponseDto;
import org.scoula.auth.mapper.RefreshTokenMapper;
import org.scoula.security.util.JwtProcessor;
import org.scoula.user.domain.UserVo;
import org.scoula.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * 카카오 OAuth 2.0 인증 서비스
 * <p>
 * 카카오 로그인 플로우를 처리하는 핵심 서비스 클래스입니다. 카카오 API와의 통신, JWT 토큰 생성, 사용자 정보 관리 등을 담당합니다.
 * <p>
 * 주요 기능: - 카카오 API를 통한 사용자 정보 조회 - JWT Access/Refresh Token 생성 및 관리 - 신규 회원 가입 처리 - 토큰 갱신 및 로그아웃
 * 처리
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class KakaoAuthService {

	/**
	 * 카카오 애플리케이션 클라이언트 ID
	 */
	@Value("${kakao.client.id}")
	private String kakaoClientId;

	/**
	 * 카카오 OAuth 리다이렉트 URI
	 */
	@Value("${kakao.redirect.uri}")
	private String kakaoRedirectUri;

	/**
	 * 사용자 정보 데이터베이스 매퍼
	 */
	private final UserMapper userMapper;

	/**
	 * 리프레시 토큰 데이터베이스 매퍼
	 */
	private final RefreshTokenMapper refreshTokenMapper;

	/**
	 * HTTP 요청을 위한 RestTemplate
	 */
	private final RestTemplate restTemplate = new RestTemplate();

	/**
	 * JSON 파싱을 위한 ObjectMapper
	 */
	private final ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * JWT 토큰 생성 및 검증 프로세서
	 */
	private final JwtProcessor jwtProcessor;

	/**
	 * 최근 로그인 처리한 사용자의 신규 회원 여부
	 */
	private boolean lastProcessedUserIsNew = false;

	/**
	 * 최근 로그인 처리한 사용자의 성향 미정의 여부
	 */
	private boolean lastProcessedUserTendencyNotDefined = false;

	/**
	 * 카카오 로그인 전체 플로우를 처리하는 메인 메서드
	 * <p>
	 * 인가 코드를 받아 카카오 API 호출부터 JWT 토큰 생성까지의 전체 과정을 처리합니다.
	 * <p>
	 * 처리 단계: 1. 카카오 액세스 토큰 획득 2. 카카오 사용자 정보 조회 3. 기존 회원 확인 또는 신규 회원 생성 4. JWT 토큰 쌍 생성 5. 리프레시 토큰 DB
	 * 저장
	 *
	 * @param code 카카오에서 발급한 인가 코드
	 * @return JWT 토큰과 사용자 정보를 포함한 로그인 응답 DTO
	 */
	@Transactional
	public KakaoLoginResponseDto processKakaoLogin(String code) {
		// 1. 카카오 API를 통해 사용자 정보 받아오기
		KakaoUserInfoDto kakaoUserInfo = getKakaoUserInfo(getKakaoAccessToken(code).getAccessToken());

		// 2. 이메일 추출 및 신규 회원 여부 확인 (DB 저장 전)
		String email = extractEmailFromKakaoInfo(kakaoUserInfo);
		UserVo existingUser = userMapper.findByEmail(email);
		lastProcessedUserIsNew = (existingUser == null);

		// 3. DB에서 사용자를 찾거나 새로 가입시키기
		UserVo user = findOrCreateUser(kakaoUserInfo);

		// 4. 성향 미정의 여부 확인 (기존 회원인 경우에만)
		lastProcessedUserTendencyNotDefined = false;
		if (!lastProcessedUserIsNew) {
			lastProcessedUserTendencyNotDefined = (user.getTendency() == null);
		}

		Map<String, Object> claims = new HashMap<>();
		claims.put("name", user.getUserName()); // 프런트가 우선 읽는 키
		claims.put("email", user.getEmail());   // 안전하게 같이 담아두기

		// 5. 우리 서비스의 Access Token과 Refresh Token 생성
		String accessToken = jwtProcessor.generateAccessToken(user.getEmail(), claims);
		String refreshTokenValue = jwtProcessor.generateRefreshToken(user.getEmail());

		// 6. 생성된 리프레시 토큰 정보를 DB에 저장
		RefreshTokenDto refreshTokenDto = RefreshTokenDto.builder()
			.email(user.getEmail())
			.tokenValue(refreshTokenValue)
			.expiresAt(LocalDateTime.now().plusWeeks(2)) // 2주 후 만료
			.build();

		refreshTokenMapper.saveRefreshToken(refreshTokenDto);

		// 7. 클라이언트에게 전달할 최종 응답 DTO 생성
		return KakaoLoginResponseDto.builder()
			.accessToken(accessToken)
			.refreshToken(refreshTokenValue)
			.userEmail(user.getEmail())
			.userName(user.getUserName())
			.build();
	}

	/**
	 * 카카오 사용자 이메일 추출
	 * <p>
	 * 카카오 사용자 정보에서 이메일을 안전하게 추출합니다.
	 *
	 * @param userInfo 카카오 사용자 정보
	 * @return 사용자 이메일
	 */
	private String extractEmailFromKakaoInfo(KakaoUserInfoDto userInfo) {
		if (userInfo == null || userInfo.getKakaoAccount() == null) {
			log.error("카카오 사용자 정보를 가져올 수 없습니다.");
			throw new IllegalArgumentException("유효하지 않은 카카오 사용자 정보입니다.");
		}
		return userInfo.getKakaoAccount().getEmail();
	}

	/**
	 * 카카오 액세스 토큰 획득
	 * <p>
	 * 인가 코드를 사용하여 카카오 인증 서버로부터 액세스 토큰을 요청합니다. OAuth 2.0 Authorization Code Grant 플로우의 두 번째 단계입니다.
	 *
	 * @param code 카카오에서 발급한 인가 코드
	 * @return 카카오 액세스 토큰 및 관련 정보
	 */
	private KakaoTokenResponseDto getKakaoAccessToken(String code) {
		// UTF-8 인코딩 처리를 위한 메시지 컨버터 추가
		restTemplate.getMessageConverters()
			.add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

		String tokenUrl = "https://kauth.kakao.com/oauth/token";

		// 요청 헤더 설정
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

		// 토큰 요청에 필요한 파라미터 설정
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("grant_type", "authorization_code");
		params.add("client_id", kakaoClientId);
		params.add("redirect_uri", kakaoRedirectUri);
		params.add("code", code);

		HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<>(params, headers);

		// 카카오 인증 서버에 토큰 요청
		ResponseEntity<KakaoTokenResponseDto> response = restTemplate.exchange(
			tokenUrl,
			HttpMethod.POST,
			kakaoTokenRequest,
			KakaoTokenResponseDto.class
		);
		return response.getBody();
	}

	/**
	 * 카카오 사용자 정보 조회
	 * <p>
	 * 카카오 액세스 토큰을 사용하여 사용자의 기본 정보를 조회합니다. 이메일, 닉네임, 생년월일 등의 정보를 가져옵니다.
	 *
	 * @param accessToken 카카오 액세스 토큰
	 * @return 카카오 사용자 정보 DTO
	 */
	private KakaoUserInfoDto getKakaoUserInfo(String accessToken) {
		String userInfoUrl = "https://kapi.kakao.com/v2/user/me";

		// Bearer 토큰 형식으로 인증 헤더 설정
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + accessToken);
		headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

		HttpEntity<MultiValueMap<String, String>> kakaoUserInfoRequest = new HttpEntity<>(headers);

		// 카카오 API로 사용자 정보 요청
		ResponseEntity<String> response = restTemplate.exchange(
			userInfoUrl,
			HttpMethod.POST,
			kakaoUserInfoRequest,
			String.class
		);

		try {
			// JSON 응답을 KakaoUserInfoDto 객체로 변환
			return objectMapper.readValue(response.getBody(), KakaoUserInfoDto.class);
		} catch (Exception e) {
			log.error("Failed to parse Kakao user info: ", e);
			throw new RuntimeException("Failed to parse Kakao user info", e);
		}
	}

	/**
	 * 사용자 조회 또는 신규 생성
	 * <p>
	 * 카카오 사용자 정보를 바탕으로 기존 회원을 조회하거나 신규 회원을 생성합니다. 이메일을 기준으로 중복 가입을 방지합니다.
	 *
	 * @param userInfo 카카오로부터 조회한 사용자 정보
	 * @return 기존 또는 새로 생성된 사용자 정보
	 */
	private UserVo findOrCreateUser(KakaoUserInfoDto userInfo) {
		// 카카오 계정에서 이메일 추출
		if (userInfo == null || userInfo.getKakaoAccount() == null) {
			log.error("카카오 사용자 정보를 가져올 수 없습니다.");
			throw new IllegalArgumentException("유효하지 않은 카카오 사용자 정보입니다.");
		}

		String email = userInfo.getKakaoAccount().getEmail();

		// 기존 회원 확인
		Optional<UserVo> existingUser = Optional.ofNullable(userMapper.findByEmail(email));
		if (existingUser.isPresent()) {
			return existingUser.get();
		}

		// 닉네임 추출 (properties → kakao_account.profile 순서로 우선순위)
		String nickname = "카카오유저";
		if (userInfo.getProperties() != null && userInfo.getProperties().getNickname() != null) {
			nickname = userInfo.getProperties().getNickname();
		} else if ((userInfo.getKakaoAccount() != null) && (userInfo.getKakaoAccount().getProfile()
			!= null) && (
			userInfo.getKakaoAccount().getProfile().getNickname() != null)) {
			nickname = userInfo.getKakaoAccount().getProfile().getNickname();
		}

		// 새로운 사용자 생성 및 DB 저장
		UserVo newUser = UserVo.builder()
			.email(email)
			.userName(nickname)
			.build();
		userMapper.save(newUser);

		return newUser;
	}

	/**
	 * JWT 토큰 재발급
	 * <p>
	 * 만료된 Access Token을 Refresh Token을 사용하여 재발급합니다. Refresh Token도 함께 갱신하여 Refresh Token Rotation을
	 * 구현합니다.
	 *
	 * @param refreshToken 클라이언트에서 전송한 Refresh Token
	 * @return 새로 발급된 Access Token과 Refresh Token
	 * @throws RuntimeException Refresh Token이 유효하지 않은 경우
	 */
	@Transactional
	public TokenRefreshResponseDto reissueTokens(String refreshToken) {
		// 1. Refresh Token에서 사용자 정보 추출 및 DB 검증
		String userEmail = jwtProcessor.getUsername(refreshToken);
		RefreshTokenDto storedToken = refreshTokenMapper.findTokenByUserEmail(userEmail);

		// 2. 저장된 토큰과 일치하는지 검증
		if (storedToken == null || !storedToken.getTokenValue().equals(refreshToken)) {
			throw new RuntimeException("Invalid Refresh Token");
		}

		// 3. 새로운 토큰 쌍 생성
		String newAccessToken = jwtProcessor.generateAccessToken(userEmail);
		String newRefreshToken = jwtProcessor.generateRefreshToken(userEmail);

		// 4. DB에 새로운 Refresh Token으로 갱신 (기존 토큰 무효화)
		RefreshTokenDto newRefreshTokenDto = RefreshTokenDto.builder()
			.email(userEmail)
			.tokenValue(newRefreshToken)
			.expiresAt(LocalDateTime.now().plusWeeks(2))
			.build();
		refreshTokenMapper.saveRefreshToken(newRefreshTokenDto);

		// 5. 새로운 토큰 쌍을 클라이언트에 반환
		return new TokenRefreshResponseDto(newAccessToken, newRefreshToken);
	}

	/**
	 * 로그아웃 처리
	 * <p>
	 * 데이터베이스에서 사용자의 Refresh Token을 삭제하여 로그아웃을 처리합니다. 클라이언트는 별도로 로컬 저장소의 토큰을 제거해야 합니다.
	 *
	 * @param email 로그아웃할 사용자의 이메일
	 */
	public void logout(String email) {
		// DB에서 해당 사용자의 Refresh Token 삭제
		refreshTokenMapper.deleteByEmail(email);
	}

	/**
	 * 최근 processKakaoLogin에서 처리한 사용자의 신규 회원 여부 반환
	 *
	 * @return 신규 회원이면 true, 기존 회원이면 false
	 */
	public boolean getLastProcessedUserIsNew() {
		return lastProcessedUserIsNew;
	}

	/**
	 * 최근 processKakaoLogin에서 처리한 사용자의 성향 미정의 여부 반환
	 *
	 * @return 성향 미정의면 true, 정의됨이면 false
	 */
	public boolean getLastProcessedUserTendencyNotDefined() {
		return lastProcessedUserTendencyNotDefined;
	}
}
