package org.scoula.gift.service;

import org.scoula.gift.dto.SimulationRequestDto;
import org.scoula.gift.dto.SimulationResponseDto;
import org.scoula.gift.dto.WillPageResponseDto;

public interface SimulationService {
	/**
	 * 증여세 시뮬레이션을 실행하고 전체 결과(세금, 절세전략 등)를 반환합니다.
	 */
	SimulationResponseDto runGiftTaxSimulation(SimulationRequestDto requestDto, String email);

	WillPageResponseDto getUserInfoForWillPage(String email);
}
