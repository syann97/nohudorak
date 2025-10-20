package org.scoula.sms.controller;

import org.scoula.sms.dto.SmsRequestDto;
import org.scoula.sms.dto.SmsResponseDto;
import org.scoula.sms.service.SmsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * SMS 발송 관련 API를 처리하는 컨트롤러
 */
@Api(tags = "SMS 발송 API", description = "SMS 메시지 발송 관련 API")
@RestController
@RequestMapping("/api/sms") // 기본 API 경로
@RequiredArgsConstructor
@Log4j2
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class SmsController {

	private final SmsService smsService;

	@ApiOperation(value = "API 테스트", notes = "SMS API 서버의 연결 상태를 확인합니다.")
	@GetMapping("/test")
	public ResponseEntity<String> test() {
		return ResponseEntity.ok("SMS API 연결 성공!");
	}

	@ApiOperation(value = "SMS 발송", notes = "수신자에게 예약 완료 등 안내 SMS를 발송합니다.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "발송 성공"),
		@ApiResponse(code = 500, message = "서버 내부 오류 또는 발송 실패")
	})
	@PostMapping("/send")
	public ResponseEntity<SmsResponseDto> sendSms(@RequestBody SmsRequestDto request) {
		log.info("SMS 발송 요청: " + request);
		// 단일 SMS 발송
		SmsResponseDto response = smsService.sendSms(request);

		return ResponseEntity.ok(response);
	}

	// 추후 추가 가능한 예약 전송...
	@ApiOperation(value = "예약 SMS 발송 (미구현)", notes = "특정 시간에 SMS를 예약 발송합니다.")
	@PostMapping("/reservation")
	public ResponseEntity<SmsResponseDto> sendReservationSms(@RequestBody SmsRequestDto request) {
		log.info("예약 SMS 발송 요청: " + request);

		// 예약 정보를 기반으로 SMS 전송
		SmsResponseDto response = smsService.sendSms(request);

		return ResponseEntity.ok(response);
	}
}
