package org.scoula.gift.domain;

import lombok.Data;

/**
 * DB의 strategy 테이블 정보를 담는 VO(Value Object) 클래스
 * 이 VO의 구조는 현재 로직에 맞게 올바르게 작성되었습니다.
 */
@Data
public class StrategyVo {
	/** 전략 규칙의 고유 ID (PK) */
	private int strategyId;

	/** Java 로직과 1:1로 매칭되는 고유 코드 */
	private String strategyCode;

	/** 규칙의 카테고리 (예: '수증자 유형', '자산 유형') */
	private String ruleCategory;

	/** 사용자에게 보여줄 최종 절세 전략 메시지 */
	private String message;

	/** 동적 가중치 계산이 어려운 전략에 부여되는 기본 점수 */
	private int baseWeight;

	/** 전략의 절대적인 중요도를 나타내는 우선순위 등급 (Tier) */
	private int priorityLevel;
}
