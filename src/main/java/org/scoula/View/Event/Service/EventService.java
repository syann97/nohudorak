package org.scoula.View.Event.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.scoula.View.Event.dto.QuizDto;
import org.scoula.View.Event.mapper.QuizMapper;
import org.scoula.news.service.NewsService;
import org.scoula.user.service.UserService;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventService {

	private final UserService userService;
	private final NewsService newsService;
	private final QuizMapper quizMapper;

	public List<QuizDto> getQuiz(){
		List<QuizDto> quizDtos = new ArrayList<>();
		//100개 퀴즈 중 랜덤 7문제 반환
		for(int i = 0; i<7; i++) {
			Random random = new Random();
			int num = random.nextInt(100) + 1; // 0~99 → 1~100
			String id = "q" + num;
			quizDtos.add(quizMapper.findById(id));
		}
		return quizDtos;


	}


}
