package com.petadoption.mapper;

import com.petadoption.dto.request.PetRequestDto;
import com.petadoption.dto.response.PetResponseDto;
import com.petadoption.entity.Pet;
import com.petadoption.dto.response.PetImageResponseDto;
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
    @Mapping(target = "images", source = "images", qualifiedByName = "toImages")
    PetResponseDto toResponseDto(Pet pet);

    // Upload order (ascending id) so the gallery order is stable.
    @Named("toImages")
    default List<PetImageResponseDto> toImages(List<PetImage> images) {
        return images == null
                ? List.of()
                : images.stream()
                .sorted(java.util.Comparator.comparing(
                        PetImage::getId,
                        java.util.Comparator.nullsFirst(
                                java.util.Comparator.naturalOrder())))
                .map(image -> new PetImageResponseDto(
                        image.getId(), image.getImageUrl()))
                .toList();
    }

}
