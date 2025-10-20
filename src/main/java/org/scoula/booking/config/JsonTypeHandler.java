package org.scoula.booking.config; // 혹은 공통 패키지

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.scoula.booking.dto.DocInfoDto;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * MyBatis에서 DocInfoDto 객체를 JSON 문자열로 변환하여 데이터베이스에 저장하고,
 * 데이터베이스의 JSON 문자열을 DocInfoDto 객체로 변환하는 역할을 하는 커스텀 TypeHandler 입니다.
 */
@MappedTypes(DocInfoDto.class) // 이 TypeHandler가 처리할 Java 타입을 지정합니다.
public class JsonTypeHandler extends BaseTypeHandler<DocInfoDto> {

	// JSON 직렬화/역직렬화를 위한 Jackson ObjectMapper (thread-safe)
	private static final ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * DocInfoDto 객체를 JSON 문자열로 변환하여 PreparedStatement에 파라미터로 설정합니다.
	 * @param ps PreparedStatement 객체
	 * @param i 파라미터 인덱스
	 * @param parameter 변환할 DocInfoDto 객체
	 * @param jdbcType JDBC 타입
	 */
	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, DocInfoDto parameter, JdbcType jdbcType) throws SQLException {
		try {
			// 객체를 JSON 문자열로 변환하여 DB에 저장
			ps.setString(i, objectMapper.writeValueAsString(parameter));
		} catch (JsonProcessingException e) {
			throw new SQLException("DocInfoDto를 JSON 문자열로 변환하는 중 오류 발생", e);
		}
	}

	/**
	 * ResultSet에서 컬럼 이름으로 JSON 문자열을 가져와 DocInfoDto 객체로 변환합니다.
	 * @param rs ResultSet 객체
	 * @param columnName 컬럼 이름
	 * @return 변환된 DocInfoDto 객체 (JSON이 null이면 null 반환)
	 */
	@Override
	public DocInfoDto getNullableResult(ResultSet rs, String columnName) throws SQLException {
		String json = rs.getString(columnName);
		return json == null ? null : parseJson(json);
	}

	/**
	 * ResultSet에서 컬럼 인덱스로 JSON 문자열을 가져와 DocInfoDto 객체로 변환합니다.
	 * @param rs ResultSet 객체
	 * @param columnIndex 컬럼 인덱스
	 * @return 변환된 DocInfoDto 객체 (JSON이 null이면 null 반환)
	 */
	@Override
	public DocInfoDto getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		String json = rs.getString(columnIndex);
		return json == null ? null : parseJson(json);
	}

	/**
	 * CallableStatement에서 컬럼 인덱스로 JSON 문자열을 가져와 DocInfoDto 객체로 변환합니다.
	 * @param cs CallableStatement 객체
	 * @param columnIndex 컬럼 인덱스
	 * @return 변환된 DocInfoDto 객체 (JSON이 null이면 null 반환)
	 */
	@Override
	public DocInfoDto getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		String json = cs.getString(columnIndex);
		return json == null ? null : parseJson(json);
	}

	/**
	 * JSON 문자열을 파싱하여 DocInfoDto 객체로 변환하는 private 헬퍼 메서드입니다.
	 * @param json 파싱할 JSON 문자열
	 * @return 변환된 DocInfoDto 객체
	 * @throws SQLException JSON 파싱 중 오류 발생 시
	 */
	private DocInfoDto parseJson(String json) throws SQLException {
		try {
			return objectMapper.readValue(json, DocInfoDto.class);
		} catch (JsonProcessingException e) {
			throw new SQLException("JSON 문자열을 DocInfoDto로 파싱하는 중 오류 발생", e);
		}
	}
}