package edu.dosw.parcial.controller.mappers;

import edu.dosw.parcial.controller.dtos.response.OrderResponse;
import edu.dosw.parcial.persistence.entities.OrderEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = OrderItemMapper.class)
public interface OrderMapper {

    @Mapping(target = "id", expression = "java(String.valueOf(entity.getId()))")
    @Mapping(target = "userId", expression = "java(String.valueOf(entity.getUser().getId()))")
    @Mapping(target = "status", expression = "java(entity.getStatus().name())")
    @Mapping(target = "items", source = "items")
    OrderResponse toResponse(OrderEntity entity);
}
