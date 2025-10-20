package org.scoula.news.scheduler;

import org.scoula.news.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 뉴스 크롤링 스케줄러
 * - 매일 정오(12:00)에 뉴스 데이터를 자동으로 크롤링하고 저장합니다.
 */
@Component
@EnableScheduling
public class NewsScheduler {

	@Autowired
	private NewsService newsService;

	/**
	 * 매일 정오에 뉴스 크롤링 및 저장 작업 실행
	 */
	@Scheduled(cron = "0 0 12 * * *") // 매일 정오에 실행
	public void scheduleNewsCrawling() {
		newsService.crawlAndSaveNews();
	}
}
