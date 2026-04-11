package edu.dosw.parcial.controller.mappers;

import edu.dosw.parcial.controller.dtos.response.ProductResponse;
import edu.dosw.parcial.persistence.entities.ProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "id", expression = "java(String.valueOf(entity.getId()))")
    @Mapping(target = "status", expression = "java(entity.getStatus().name())")
    ProductResponse toResponse(ProductEntity entity);
}
