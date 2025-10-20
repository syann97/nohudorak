package org.scoula.news.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.scoula.news.domain.NewsVo;
import org.scoula.news.dto.NewsDto;
import org.scoula.news.mapper.NewsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j2;

/**
 * 뉴스 관련 서비스 구현체
 * - 외부 뉴스 사이트에서 데이터 크롤링
 * - DB에 저장/업데이트
 * - 전체 뉴스 조회 기능 제공
 */
@Log4j2
@Service
public class NewsServiceImpl implements NewsService {

	// 카테고리별 대표 키워드 매핑
	private static final Map<Integer, String> CATEGORY_KEYWORDS = Map.of(
		0, "최신",
		1, "예금",
		2, "적금",
		3, "주택담보",
		4, "금",
		5, "펀드",
		6, "신탁"
	);
	@Autowired
	private NewsMapper newsMapper;

	/**
	 * 뉴스 사이트를 크롤링하여 DB에 저장/업데이트합니다.
	 * - 카테고리별 대표 기사 1건 유지 (0번 카테고리: 최신 기사 5건)
	 * @return 업데이트된 카테고리 번호 리스트
	 */
	@Override
	public List<Integer> crawlAndSaveNews() {
		List<Integer> updatedCategories = new ArrayList<>();

		try {
			// 뉴스 사이트 HTML 문서 로드
			Document doc = Jsoup.connect(
				"https://www.newswire.co.kr/?md=A10&act=article&no=199&perpage=100"
			).get();

			// 뉴스 목록 선택
			Elements newsList = doc.select(".news-column");

			// 카테고리별 키워드 매핑
			Map<Integer, List<String>> categoryKeywords = Map.of(
				1, List.of("예금"),
				2, List.of("적금"),
				3, List.of("주택", "담보", "대출"),
				4, List.of("\\b금\\b", "순금", "골드바", "금 투자", "금 시세"),
				5, List.of("펀드"),
				6, List.of("신탁", "상속", "증여", "유언")
			);

			// 카테고리 1~6 처리
			for (Map.Entry<Integer, List<String>> entry : categoryKeywords.entrySet()) {
				Integer category = entry.getKey();
				List<String> keywords = entry.getValue();

				for (Element news : newsList) {
					String title = news.select("h5 a").text();

					boolean isMatch = false;
					if (category == 3) { // 담보/주택/대출: 1개 이상 포함
						long count = keywords.stream().filter(title::contains).count();
						isMatch = count >= 1;
					} else if (category == 4) { // 금: 정규식 키워드
						for (String keyword : keywords) {
							if (title.matches(".*" + keyword + ".*")) {
								isMatch = true;
								break;
							}
						}
					} else { // 단순 포함 여부 확인
						for (String keyword : keywords) {
							if (title.contains(keyword)) {
								isMatch = true;
								break;
							}
						}
					}

					if (!isMatch) {
						continue;
					}

					// 뉴스 정보 추출
					String link = news.select("h5 a").attr("href");
					String summary = news.select(".content a").text();
					String date = news.select(".info .mdate").text();

					// 기존 뉴스 조회
					NewsVo existing = newsMapper.findByCategory(category);
					NewsVo newNews = new NewsVo(null, category, title, link, date, summary, null);

					// 새 뉴스 삽입 또는 기존 뉴스 업데이트
					if (existing == null) {
						newsMapper.insertNews(newNews);
						updatedCategories.add(category);
					} else if (!existing.getTitle().equals(title)) {
						newsMapper.upsertNews(newNews);
						updatedCategories.add(category);
					}

					break; // 카테고리 대표 1건만 처리
				}
			}

			// 카테고리 0: 최신 기사 5건 저장
			int maxCount = Math.min(newsList.size(), 5);
			for (int i = 0; i < maxCount; i++) {
				Element news = newsList.get(i);
				String title = news.select("h5 a").text();
				String link = news.select("h5 a").attr("href");
				String summary = news.select(".content a").text();
				String date = news.select(".info .mdate").text();

				NewsVo newNews = new NewsVo(null, 0, title, link, date, summary, null);
				newsMapper.upsertNews(newNews); // category+link 기준 UPSERT
			}
			updatedCategories.add(0);

		} catch (IOException e) {
			log.error("IOException occurred while crawling and saving news", e);
		}

		return updatedCategories;
	}

	/**
	 * DB에 저장된 전체 뉴스 목록을 조회하여 DTO 리스트로 반환합니다.
	 * @return 뉴스 DTO 리스트
	 */
	@Override
	public List<NewsDto> getAllNews() {
		return newsMapper.findAll().stream()
			.map(n -> new NewsDto(n.getCategory(), n.getTitle(), n.getLink(), n.getDate(),
				n.getSummary()))
			.collect(Collectors.toList());
	}
}
