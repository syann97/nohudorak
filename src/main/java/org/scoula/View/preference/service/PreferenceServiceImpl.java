package org.scoula.View.preference.service;
import org.scoula.View.preference.dto.PreferenceRequestDto;
import org.scoula.recommend.service.CustomRecommendService;
import org.scoula.user.dto.UserDto;
import org.scoula.user.service.UserService;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
/**
 * PreferenceService의 구현 클래스
 */
@Service
@RequiredArgsConstructor
public class PreferenceServiceImpl implements PreferenceService{
	private final UserService userService;
	private final CustomRecommendService customRecommendService;
	/**
	 * 설문 답변을 점수화하여 사용자의 투자 성향(tendency)을 계산하고,
	 * 이를 바탕으로 사용자 정보를 업데이트한 후 맞춤 추천 상품 목록을 갱신합니다.
	 *
	 * @implNote :경고: 주의: 현재 switch문에 break문이 없어 각 case가 연달아 실행(fall-through)됩니다.
	 * 예를 들어 q1이 1인 경우, 1, 2, 3, 4번 case가 모두 실행되어 점수가 의도와 다르게 누적될 수 있습니다.
	 * 각 case가 독립적으로 실행되도록 하려면 각 case의 끝에 ‘break;‘를 추가해야 합니다.
	 *
	 * @param requestDto 사용자의 설문 답변이 담긴 DTO
	 * @param userEmail 성향을 설정할 사용자의 이메일
	 */
	@Override
	public void setUserPreference(PreferenceRequestDto requestDto, String userEmail) {
		double startPoint = 0;
		startPoint += mapScore(requestDto.getQ1(), 3); // 3점 척도 기준
		startPoint += mapScore(requestDto.getQ2(), 2);
		startPoint += mapScore(requestDto.getQ3(), 3);
		startPoint += mapScore(requestDto.getQ4(), 3);
		startPoint += mapScore(requestDto.getQ5(), 2);
		if (Double.isNaN(startPoint)) startPoint = 0.0;
		// 점수 보정
		if(startPoint > 1) startPoint = 1;
		if(startPoint < -1) startPoint = -1;
		UserDto userDto = userService.getUser(userEmail);
		userDto.setTendency(startPoint);
		userService.updateUser(userEmail, userDto);
		customRecommendService.addCustomRecommend(userEmail);
	}
	/**
	 * 선택지 번호와 척도 크기를 기반으로 점수를 계산해주는 메서드
	 *
	 * @param answer 사용자가 고른 선택지 번호 (1부터 시작)
	 * @param scale 해당 문항의 선택지 개수 (3, 5, 7 등)
	 * @return -0.3 ~ +0.3 사이 점수
	 */
	private double mapScore(Integer answer, int scale) {
		if (answer == null || answer < 1 || answer > scale) return 0.0;
		if (scale == 2) {
			return (answer == 1) ? 0.3 : -0.3; // 1번이 보수적, 2번이 공격적
		}
		if (scale < 2) return 0.0;
		double t = 1.0 - ((double)(answer - 1) / (scale - 1)); // 보수적일수록 높게
		return -0.3 + (t * 0.6); // 최종 점수: -0.3 ~ +0.3
	}
}
