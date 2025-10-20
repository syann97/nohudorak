package org.scoula.product.dto;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Fund(펀드) DTO
 */
@ApiModel(value = "FundDto", description = "펀드 상품 상세 정보를 담는 DTO")
@SuperBuilder
@ToString(callSuper = true)
@Getter
public class FundDto extends ProductDetailDto<FundOptionDto> {
}
