package com.petadoption.mapper;

import com.petadoption.dto.request.PetRequestDto;
import com.petadoption.dto.response.PetResponseDto;
import com.petadoption.entity.Pet;
import com.petadoption.entity.PetImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PetMapper {

    Pet toEntity(PetRequestDto dto);

    @Mapping(target = "shelterId", source = "shelter.id")
    @Mapping(target = "shelterName", source = "shelter.name")
    @Mapping(target = "imageUrls", source = "images", qualifiedByName = "toImageUrls")
    PetResponseDto toResponseDto(Pet pet);

    @Named("toImageUrls")
    default List<String> toImageUrls(List<PetImage> images) {
        return images == null
                ? List.of()
                : images.stream().map(PetImage::getImageUrl).toList();
    }

}
