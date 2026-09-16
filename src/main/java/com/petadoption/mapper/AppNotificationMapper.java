package com.petadoption.mapper;

import com.petadoption.dto.response.AppNotificationResponseDto;
import com.petadoption.entity.AppNotification;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AppNotificationMapper {

    AppNotificationResponseDto toResponseDto(AppNotification notification);

}
