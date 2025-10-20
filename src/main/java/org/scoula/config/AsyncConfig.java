package org.scoula.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncConfig { // 스레드 풀 설정 전용 Config 파일

	@Bean(name = "ragTaskExecutor")
	@Qualifier("ragTaskExecutor") // GptServiceImpl에서 주입할 수 있도록 이름 지정
	public Executor ragTaskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(10);   // 기본 스레드 수
		executor.setMaxPoolSize(10);  // 최대 스레드 수

		// [수정] TaskRejectedException 해결
		// 700개가 넘는 청크를 처리해야 하므로 큐 용량을 50 -> 1000으로 대폭 증가
		executor.setQueueCapacity(1000);

		executor.setThreadNamePrefix("RagAsync-");

		// 큐가 꽉 찼을 때의 정책 (AbortPolicy: 예외를 발생시키고 작업을 거부 - 기본값)
		// 이 정책을 유지해야 큐 용량이 부족할 경우 빠르게 인지할 수 있습니다.
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());

		executor.initialize();
		return executor;
	}

	// (만약 다른 종류의 스레드 풀이 필요하다면 여기에 추가로 Bean을 정의하면 됩니다.)
}