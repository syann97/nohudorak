package org.scoula.sms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "SMS 발송 응답 DTO", description = "SMS 발송 후 결과를 담는 객체")
public class SmsResponseDto {
	@ApiModelProperty(value = "발송 성공 여부", example = "true")
	private boolean success;

	@ApiModelProperty(value = "결과 메시지", example = "SMS 전송 성공")
	private String message;

	@ApiModelProperty(value = "메시지 ID (성공 시)", example = "M2V2bWVzc2FnZWlk")
	private String messageId;

	@ApiModelProperty(value = "에러 코드 (실패 시)", example = "SMS_SEND_ERROR")
	private String errorCode;
}
