package org.scoula.config;

import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;

// Spring Security 필터 등록
public class SecurityInitializer extends AbstractSecurityWebApplicationInitializer {
	// Dispatcher 진입 전에 Spring security 필터 체인으로 먼저 진입하게끔 함
}
