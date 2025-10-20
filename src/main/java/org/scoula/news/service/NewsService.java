package org.scoula.news.service;

import java.util.List;

import org.scoula.news.dto.NewsDto;

public interface NewsService {

	List<Integer> crawlAndSaveNews();

	List<NewsDto> getAllNews();
}
