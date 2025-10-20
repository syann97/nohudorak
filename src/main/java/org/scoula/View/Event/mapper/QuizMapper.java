package org.scoula.View.Event.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.scoula.View.Event.dto.QuizDto;

@Mapper
public interface QuizMapper {
	QuizDto findById(String id);
}

