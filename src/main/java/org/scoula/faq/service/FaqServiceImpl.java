package org.scoula.faq.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import org.scoula.faq.dto.FaqDto;
import org.scoula.faq.dto.FaqListDto;
import org.scoula.faq.mapper.FaqMapper;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/**
 * FAQ 관련 비즈니스 로직을 처리하는 서비스 구현 클래스
 */
@Service
@RequiredArgsConstructor
public class FaqServiceImpl implements FaqService {

	private final FaqMapper faqMapper;

	/**
	 * FAQ 목록을 요약된 정보(ID, 카테고리, 제목)만으로 조회합니다.
	 * @return FaqListDto 리스트
	 */
	@Override
	public List<FaqListDto> getFaqList() {
		return faqMapper.getAllFaqs().stream()
			.map(FaqListDto::from)
			.collect(Collectors.toList());
	}

	/**
	 * 모든 FAQ 목록을 상세 내용까지 포함하여 조회합니다.
	 * (클라이언트에서 필터링하거나, 페이지 이동 없이 상세 내용을 보여줄 때 유용합니다.)
	 * @return FaqDto 리스트
	 */
	@Override
	public List<FaqDto> getAllFaqsWithContent() {
		return faqMapper.getAllFaqs().stream()
			.map(FaqDto::from)
			.collect(Collectors.toList());
	}

	/**
	 * FAQ ID로 특정 FAQ의 상세 정보를 조회합니다.
	 * @param faqId 조회할 FAQ의 ID
	 * @return 해당 FAQ의 상세 정보 DTO
	 * @throws NoSuchElementException 해당 ID의 FAQ가 없을 경우 발생
	 */
	@Override
	public FaqDto getFaqById(Integer faqId) {
		return Optional.ofNullable(faqMapper.getFaqById(faqId))
			.map(FaqDto::from)
			.orElseThrow(() -> new NoSuchElementException("FAQ not found with id: " + faqId));
	}
}