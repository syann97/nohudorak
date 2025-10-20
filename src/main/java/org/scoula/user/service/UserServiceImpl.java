package org.scoula.user.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import org.scoula.asset.dto.AssetStatusSummaryDto;
import org.scoula.asset.mapper.AssetStatusMapper;
import org.scoula.auth.mapper.RefreshTokenMapper;
import org.scoula.booking.domain.BookingVo;
import org.scoula.booking.dto.BookingDto;
import org.scoula.booking.mapper.BookingMapper;
import org.scoula.branch.mapper.BranchMapper;
import org.scoula.exception.BranchNotFoundException;
import org.scoula.exception.UserNotFoundException;
import org.scoula.gift.mapper.RecipientMapper;
import org.scoula.recommend.mapper.CustomRecommendMapper;
import org.scoula.user.domain.UserVo;
import org.scoula.user.dto.MyPageResponseDto;
import org.scoula.user.dto.UserBranchNameDto;
import org.scoula.user.dto.UserDto;
import org.scoula.user.dto.UserGraphDto;
import org.scoula.user.dto.UserInfoResponseDto;
import org.scoula.user.dto.UserInfoUpdateRequestDto;
import org.scoula.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * UserServiceImpl 클래스
 *
 * 사용자 관련 비즈니스 로직을 처리하는 서비스 구현체입니다.
 * - 회원가입, 정보 조회/수정, 선호 지점 관리, 탈퇴 처리
 * - 마이페이지 정보 조회: 자산 현황, 예약 내역, 상위 백분위 계산
 * - 사용자 포인트 증감, 자산 증감 등의 기능 제공
 *
 * 주요 기능:
 * 1. 사용자 정보 조회 및 업데이트
 * 2. 선호 지점 조회 및 업데이트
 * 3. 탈퇴 처리 (연관 데이터 삭제)
 * 4. 마이페이지용 DTO 조립 (자산 현황, 예약 정보, 상위 백분위)
 * 5. 포인트 및 자산 증감 처리
 *
 * 내부 헬퍼 메서드:
 * - calculateAssetPercentile: 특정 사용자의 자산 상위 백분위 계산
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserAssetUpdater {

	private final UserMapper userMapper;
	private final RefreshTokenMapper refreshTokenMapper;
	private final BookingMapper bookingMapper;
	private final AssetStatusMapper assetStatusMapper;
	private final CustomRecommendMapper customRecommendMapper;
	private final RecipientMapper recipientMapper;
	private final BranchMapper branchMapper;

	/**
	 * 이메일로 사용자를 조회합니다.
	 *
	 * @param email 조회할 사용자 이메일
	 * @return UserDto 사용자 정보 DTO
	 * @throws NoSuchElementException 해당 이메일의 사용자가 없는 경우
	 */
	@Override
	public UserDto getUser(String email) {
		UserVo userVO = Optional.ofNullable(userMapper.findByEmail(email))
			.orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다: " + email));
		return UserDto.of(userVO);
	}

	/**
	 * 회원가입을 처리합니다.
	 *
	 * @param userDto 가입할 사용자 정보 DTO
	 */
	@Transactional
	@Override
	public void join(UserDto userDto) {
		UserVo userVO = userDto.toVo();
		userMapper.save(userVO);
	}

	/**
	 * 사용자 정보를 업데이트합니다.
	 *
	 * @param email   업데이트 대상 사용자 이메일
	 * @param userDto 변경할 사용자 정보 DTO
	 * @throws NoSuchElementException 해당 이메일의 사용자가 없는 경우
	 */
	@Transactional
	@Override
	public void updateUser(String email, UserDto userDto) {
		UserVo user = Optional.ofNullable(userMapper.findByEmail(email))
			.orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다: " + email));

		// DTO의 정보로 기존 VO 객체의 상태를 업데이트
		user.setUserName(userDto.getUserName());
		user.setUserPhone(userDto.getUserPhone());
		user.setBirth(userDto.getBirth());
		user.setConnectedId(userDto.getConnectedId());
		user.setBranchId(userDto.getBranchId());
		user.setAsset(userDto.getAsset());
		user.setFilename1(userDto.getFilename1());
		user.setFilename2(userDto.getFilename2());
		user.setTendency(userDto.getTendency());
		user.setAssetProportion(userDto.getAssetProportion());

		userMapper.update(user);
	}

	/**
	 * 사용자 기본 정보를 조회합니다.
	 *
	 * @param email 사용자 이메일
	 * @return UserInfoResponseDto 사용자 기본 정보 응답 DTO
	 * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
	 */
	@Override
	public UserInfoResponseDto getUserInfo(String email) {
		// 1. Mapper를 통해 DB에서 사용자 정보를 조회
		UserVo userVo = Optional.ofNullable(userMapper.findByEmail(email))
			.orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + email));

		// 2. 조회된 UserVo 객체를 DTO로 변환하여 반환
		return UserInfoResponseDto.of(userVo);
	}

	/**
	 * 사용자 기본 정보를 수정합니다. (이름, 전화번호, 생년월일)
	 *
	 * @param email      수정할 사용자 이메일
	 * @param requestDto 수정 요청 DTO
	 * @throws UserNotFoundException 해당 이메일의 사용자가 없는 경우
	 */
	@Transactional
	@Override
	public void updateUserInfo(String email, UserInfoUpdateRequestDto requestDto) {
		// 1. 수정 요청을 한 사용자가 DB에 존재하는지 확인
		UserVo user = Optional.ofNullable(userMapper.findByEmail(email))
			.orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + email));

		// 2. DTO의 데이터로 기존 UserVo 객체의 필드를 업데이트
		user.setUserName(requestDto.getUserName());
		user.setUserPhone(requestDto.getUserPhone());
		user.setBirth(requestDto.getBirth());

		// 3. Mapper를 호출하여 DB에 변경사항 저장
		userMapper.updateUserInfo(user);
	}

	/**
	 * 사용자의 연결된 외부 계정 ID를 업데이트합니다.
	 *
	 * @param email       사용자 이메일
	 * @param connectedId 새로운 연결 계정 ID
	 */
	@Transactional
	@Override
	public void updateConnectedId(String email, String connectedId) {
		userMapper.updateConnectedId(email, connectedId);
	}

	/**
	 * 사용자의 선호 지점 정보를 조회합니다.
	 *
	 * @param email 사용자 이메일
	 * @return UserBranchNameDto 지점 이름 DTO (설정되지 않은 경우 null 포함)
	 * @throws UserNotFoundException 해당 이메일의 사용자가 없는 경우
	 * @throws BranchNotFoundException 유효하지 않은 지점 ID일 경우
	 */
	@Override
	public UserBranchNameDto getBranchInfo(String email) {
		// 1. UserMapper를 통해 사용자 정보를 먼저 조회합니다.
		UserVo user = userMapper.findByEmail(email);
		if (user == null) {
			throw new UserNotFoundException("해당 이메일의 사용자를 찾을 수 없습니다: " + email);
		}

		// 2. 조회된 사용자 정보에서 branchId를 가져옵니다.
		Integer branchId = user.getBranchId();
		if (branchId == null) {
			// 사용자는 있지만 선호 지점이 설정되지 않은 경우,
			// branchName이 null인 DTO 객체를 생성하여 반환합니다.
			return new UserBranchNameDto(null);
		}

		// 3. 조회된 branchId를 이용해 BranchMapper로 지점 이름을 조회합니다.
		String branchName = branchMapper.findBranchNameById(branchId);
		if (branchName == null) {
			// 사용자가 유효하지 않은 branchId를 가지고 있는 경우의 예외처리
			throw new BranchNotFoundException(
				"ID(" + branchId + ")에 해당하는 지점을 찾을 수 없습니다. (사용자: " + email + ")");
		}

		// 4. 최종적으로 조회된 지점 이름을 DTO에 담아 반환합니다.
		return new UserBranchNameDto(branchName);
	}

	/**
	 * 사용자의 선호 지점을 변경합니다.
	 *
	 * @param email    사용자 이메일
	 * @param branchId 변경할 지점 ID
	 * @throws BranchNotFoundException 해당 ID의 지점이 없는 경우
	 */
	@Transactional
	@Override
	public void updateBranchId(String email, Integer branchId) {
		// 1. 지점 존재 여부 확인
		boolean branchExists = branchMapper.existsById(branchId);

		// 2. 존재하지 않으면 예외 발생
		if (!branchExists) {
			// 커스텀 예외를 만들거나, 적절한 표준 예외를 사용합니다.
			throw new BranchNotFoundException("ID가 " + branchId + "인 지점을 찾을 수 없습니다.");
		}

		// 3. 유효성 검사를 통과하면 업데이트 수행
		userMapper.updateBranchId(email, branchId);
	}

	/**
	 * 사용자를 탈퇴 처리합니다.
	 * (연관된 토큰, 예약, 자산, 추천, 선물 데이터까지 순차적으로 삭제)
	 *
	 * @param email 탈퇴할 사용자 이메일
	 * @throws UserNotFoundException 해당 이메일의 사용자가 없는 경우
	 */
	@Transactional
	@Override
	public void withdrawUser(String email) {
		// 사용자와 관련된 모든 데이터를 순서대로 삭제
		refreshTokenMapper.deleteByEmail(email);
		bookingMapper.deleteByEmail(email);
		assetStatusMapper.deleteByEmail(email);
		customRecommendMapper.deleteAllProductsByEmail(email);
		recipientMapper.deleteByEmail(email);

		int affectedRows = userMapper.deleteByEmail(email);
		if (affectedRows == 0) { // 삭제된 사용자가 없으면 예외 발생
			throw new UserNotFoundException("해당 이메일의 사용자를 찾을 수 없습니다: " + email);
		}
	}

	/**
	 * 마이페이지 데이터를 조회합니다.
	 *
	 * @param email 사용자 이메일
	 * @return MyPageResponseDto 마이페이지 데이터 DTO
	 *   - 사용자 기본 정보 및 자산 현황
	 *   - 다가오는 예약 내역
	 *   - 자산 상위 백분위
	 */
	@Override
	public MyPageResponseDto getMyPageData(String email) {
		// 1. 사용자 정보 조회
		UserVo userVo = Optional.ofNullable(userMapper.findByEmail(email))
			.orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다: " + email));

		// 2. 자산 현황 요약 목록 조회
		List<AssetStatusSummaryDto> assetList = assetStatusMapper.findAssetStatusSummaryByEmail(email)
			.stream()
			.map(AssetStatusSummaryDto::of)
			.collect(Collectors.toList());

		// 3. 다가오는 예약 내역 목록 조회
		List<BookingVo> bookingVos = bookingMapper.findUpcomingByUserEmail(email);
		List<BookingDto> bookingDtos = bookingVos.stream()
			.map(BookingDto::of)
			.collect(Collectors.toList());

		// 4. 자산 상위 백분위 계산 로직
		Double percentile = calculateAssetPercentile(userVo.getAsset());

		// 5. 최종 DTO 조립
		UserGraphDto userInfoDto = UserGraphDto.builder()
			.userName(userVo.getUserName())
			.assetStatus(assetList)
			.build();

		return MyPageResponseDto.builder()
			.userInfo(userInfoDto)
			.bookingInfo(bookingDtos)
			.assetPercentile(percentile) // 계산된 백분위 값 추가
			.build();
	}

	/**
	 * 사용자의 포인트를 증감합니다.
	 *
	 * @param email 사용자 이메일
	 * @param delta 증감할 포인트 값 (양수/음수)
	 */
	@Override
	public void addPoint(String email, int delta) {
		userMapper.addPoint(email, delta);
	}

	/**
	 * 자산의 상위 백분위를 계산하는 private 헬퍼 메소드
	 *
	 * @param myAsset 현재 사용자의 자산
	 * @return 소수점 첫째 자리까지 계산된 상위 백분위 (%), 자산이 없거나 사용자가 적을 경우 null 반환
	 */
	private Double calculateAssetPercentile(Long myAsset) {
		// 사용자의 자산 정보가 없으면 계산 불가
		if (myAsset == null) {
			return null;
		}

		// 자산 정보가 있는 전체 사용자 수 조회
		long totalUsers = userMapper.countAllUsersWithAsset();

		// 비교 대상 사용자가 1명 이하면 백분위 의미 없음
		if (totalUsers <= 1) {
			return 100.0; // 혼자일 경우 상위 100%로 표시
		}

		// 나보다 자산이 많은 사용자 수 조회
		long usersWithMoreAsset = userMapper.countUsersWithMoreAsset(myAsset);

		// 상위 백분위 계산: (나의 등수 / 전체 인원) * 100
		double myRank = (double)(usersWithMoreAsset + 1);
		double rawPercentile = (myRank / totalUsers) * 100.0;

		// 소수점 첫째 자리까지 반올림하여 반환
		return new BigDecimal(rawPercentile)
			.setScale(1, RoundingMode.HALF_UP)
			.doubleValue();
	}

	/**
	 * 특정 사용자의 총 자산을 업데이트합니다.
	 *
	 * @param email  사용자 이메일
	 * @param amount 증감할 금액 (양수/음수)
	 */
	@Transactional
	@Override
	public void updateUserAsset(String email, long amount) {
		UserDto userDto = getUser(email);
		userDto.setAsset(userDto.getAsset() + amount);
		updateUser(email, userDto);
	}
}
