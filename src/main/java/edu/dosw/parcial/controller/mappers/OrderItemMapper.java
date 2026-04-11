package edu.dosw.parcial.controller.mappers;

import edu.dosw.parcial.controller.dtos.response.OrderItemResponse;
import edu.dosw.parcial.persistence.entities.OrderItemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(target = "productId", expression = "java(String.valueOf(entity.getProductId()))")
    OrderItemResponse toResponse(OrderItemEntity entity);
}
