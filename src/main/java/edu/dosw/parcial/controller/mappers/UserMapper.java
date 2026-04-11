package edu.dosw.parcial.controller.mappers;

import edu.dosw.parcial.controller.dtos.response.UserResponse;
import edu.dosw.parcial.persistence.entities.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", expression = "java(String.valueOf(entity.getId()))")
    @Mapping(target = "role", expression = "java(entity.getRole().name())")
    UserResponse toResponse(UserEntity entity);
}
