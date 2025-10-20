package org.scoula.config;

import javax.servlet.Filter;
import javax.servlet.ServletRegistration;

import org.scoula.security.config.SecurityConfig;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class WebConfig extends AbstractAnnotationConfigDispatcherServletInitializer {

	@Override
	protected void customizeRegistration(ServletRegistration.Dynamic registration) {
		registration.setInitParameter("throwExceptionIfNoHandlerFound", "true");
		registration.setInitParameter("throwExceptionIfNoHandlerFound", "true");
	}

	@Override
	protected Class<?>[] getRootConfigClasses() {
		return new Class[] {RootConfig.class, SecurityConfig.class};
	}

	@Override
	protected Class<?>[] getServletConfigClasses() {
		// Swagger 설정 클래스 추가
		return new Class[] {ServletConfig.class, SwaggerConfig.class};
	}

	@Override
	protected String[] getServletMappings() {
		return new String[] {
			"/",                        // 기본 매핑
			"/swagger-ui.html",         // Swagger UI 메인 페이지
			"/swagger-resources/**",    // Swagger 리소스
			"/v2/api-docs",            // API 명세 JSON
			"/webjars/**"              // WebJar 리소스 (CSS, JS 등)
		};
	}

	// Post body 문자 인코딩 필터 설정
	@Override
	protected Filter[] getServletFilters() {
		CharacterEncodingFilter encodingFilter = new CharacterEncodingFilter();
		encodingFilter.setEncoding("UTF-8");
		encodingFilter.setForceEncoding(true);
		return new Filter[] {encodingFilter};
	}
}
