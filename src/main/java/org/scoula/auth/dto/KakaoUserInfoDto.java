package org.scoula.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "카카오 사용자 정보 DTO", description = "카카오 API를 통해 조회한 사용자의 전체 정보")
public class KakaoUserInfoDto {

	@ApiModelProperty(value = "회원번호", required = true, example = "1234567890")
	private Long id;

	@ApiModelProperty(value = "서비스와 연결된 시각", example = "2024-07-25T14:30:00Z")
	@JsonProperty("connected_at")
	private String connectedAt;

	@ApiModelProperty(value = "사용자 프로퍼티 정보")
	private Properties properties;

	@ApiModelProperty(value = "카카오 계정 상세 정보")
	@JsonProperty("kakao_account")
	private KakaoAccount kakaoAccount;

	// --- Nested Classes ---

	@Data
	@ApiModel(value = "카카오 사용자 속성", description = "사용자의 닉네임, 프로필 사진 등 기본 정보")
	public static class Properties {
		@ApiModelProperty(value = "닉네임", example = "홍길동")
		private String nickname;

		@ApiModelProperty(value = "프로필 이미지 URL (640x640)", example = "http://k.kakaocdn.net/dn/.../img_640x640.jpg")
		@JsonProperty("profile_image")
		private String profileImage;

		@ApiModelProperty(value = "썸네일 이미지 URL (110x110)", example = "http://k.kakaocdn.net/dn/.../img_110x110.jpg")
		@JsonProperty("thumbnail_image")
		private String thumbnailImage;
	}

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ApiModel(value = "카카오 계정 정보", description = "이메일, 생일 등 사용자의 카카오 계정 관련 정보")
	public static class KakaoAccount {
		@ApiModelProperty(value = "닉네임 제공 동의 필요 여부", example = "false")
		@JsonProperty("profile_nickname_needs_agreement")
		private Boolean profileNicknameNeedsAgreement;

		@ApiModelProperty(value = "프로필 상세 정보")
		private Profile profile;

		@ApiModelProperty(value = "이메일 제공 동의 필요 여부", example = "false")
		private Boolean emailNeedsAgreement;

		@ApiModelProperty(value = "이메일 유효 여부", example = "true")
		private Boolean isEmailValid;

		@ApiModelProperty(value = "이메일 인증 여부", example = "true")
		private Boolean isEmailVerified;

		@ApiModelProperty(value = "카카오계정 대표 이메일", example = "test@example.com")
		private String email;

		@ApiModelProperty(value = "출생 연도 (YYYY 형식)", example = "2000")
		private String birthyear;

		@ApiModelProperty(value = "생일 (MMDD 형식)", example = "0110")
		private String birthday;

		@Data
		@JsonIgnoreProperties(ignoreUnknown = true)
		@ApiModel(value = "카카오 프로필 정보", description = "카카오 계정에 설정된 프로필 상세 정보")
		public static class Profile {
			@ApiModelProperty(value = "닉네임", example = "홍길동")
			private String nickname;

			@ApiModelProperty(value = "썸네일 이미지 URL", example = "http://k.kakaocdn.net/dn/.../img_110x110.jpg")
			@JsonProperty("thumbnail_image_url")
			private String thumbnailImageUrl;

			@ApiModelProperty(value = "프로필 이미지 URL", example = "http://k.kakaocdn.net/dn/.../img_640x640.jpg")
			@JsonProperty("profile_image_url")
			private String profileImageUrl;

			@ApiModelProperty(value = "프로필 이미지가 기본 이미지인지 여부", example = "false")
			@JsonProperty("is_default_image")
			private Boolean isDefaultImage;
		}
	}
}
