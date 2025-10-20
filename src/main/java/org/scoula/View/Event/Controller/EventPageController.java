package org.scoula.View.Event.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.scoula.View.Event.Service.EventService;
import org.scoula.View.Event.dto.QuizDto;
import org.scoula.news.dto.NewsDto;
import org.scoula.news.service.NewsService;
import org.scoula.user.dto.UserDto;
import org.scoula.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;

//changed
@Api(tags = "이벤트 페이지 API", description = " 이벤트 화면 및 뉴스 데이터 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/event")
public class EventPageController {
	private final EventService eventService;
	private final NewsService newsService;
	private final UserService userService;

	@GetMapping("")
	public ResponseEntity<?> getPage(Authentication authentication){
		Map<String,Object> response = new HashMap<>();
		List<NewsDto> newsDtos = newsService.getAllNews();
		int point = userService.getUser(authentication.getName()).getPoint();
		response.put("news",newsDtos);
		response.put("point",point);
		return ResponseEntity.ok(response);
	}
	@PostMapping("")
	public ResponseEntity<Void> addPoint(Authentication authentication){
		UserDto userDto = userService.getUser(authentication.getName());
		//사용자 현재 포인트에 500 ++
		int plusedPoint =  500;
		userDto.setPoint(plusedPoint);
		userService.addPoint(authentication.getName(),plusedPoint);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
	@GetMapping("/quiz")
	public ResponseEntity<List<QuizDto>> getQuiz(){
		return ResponseEntity.ok(eventService.getQuiz());
	}
}
