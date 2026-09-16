package com.petadoption.mapper;

import com.petadoption.dto.response.UserResponseDto;
import com.petadoption.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(
            target = "roles",
            expression =
                    "java(user.getRoles()" +
                            ".stream()" +
                            ".map(role -> role.getName().name())" +
                            ".collect(java.util.stream.Collectors.toSet()))")
    @Mapping(target = "shelterId", source = "shelter.id")
    @Mapping(target = "shelterName", source = "shelter.name")
    UserResponseDto toResponseDto(User user);
}