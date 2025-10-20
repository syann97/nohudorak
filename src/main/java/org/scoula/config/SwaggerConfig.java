package org.scoula.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration        // Spring 설정 클래스임을 명시
@EnableSwagger2      // Swagger 2.0 활성화
public class SwaggerConfig {
	// API 문서 메타 정보 상수
	private final String API_NAME = "노후도락 사용자 서버";
	private final String API_VERSION = "1.0";
	private final String API_DESCRIPTION = "노후도락 사용자 서버 API 명세서";

	/**
	 * API 문서 기본 정보 설정
	 */
	private ApiInfo apiInfo() {
		return new ApiInfoBuilder()
			.title(API_NAME)                    // API 문서 제목
			.description(API_DESCRIPTION)       // API 문서 설명
			.version(API_VERSION)               // API 버전
			.build();
	}

	/**
	 * Swagger 문서 생성 설정
	 */
	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2)    // Swagger 2.0 사용
			.select()
			.apis(RequestHandlerSelectors.withClassAnnotation(
				RestController.class))  // @RestController가 붙은 클래스만 문서화 대상으로 지정
			.paths(PathSelectors.any())  // 모든 경로 포함
			.build()
			.apiInfo(apiInfo())         // 위에서 설정한 API 정보 적용
			// 👇 아래 보안 관련 설정을 추가합니다.
			.securityContexts(Collections.singletonList(securityContext()))
			.securitySchemes(Collections.singletonList(apiKey()));
	}

	// JWT 인증 방식을 설명하는 ApiKey 객체를 생성합니다.
	private ApiKey apiKey() {
		return new ApiKey("JWT", "Authorization", "header");
	}

	// 어떤 경로에서 JWT 인증을 적용할지 설정합니다.
	private SecurityContext securityContext() {
		return SecurityContext.builder()
			.securityReferences(defaultAuth())
			.build();
	}

	private List<SecurityReference> defaultAuth() {
		AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
		AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
		authorizationScopes[0] = authorizationScope;
		return Arrays.asList(new SecurityReference("JWT", authorizationScopes));
	}
}
