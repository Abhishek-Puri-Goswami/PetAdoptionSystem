package com.petadoption.repository;

import com.petadoption.entity.PetImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PetImageRepository extends JpaRepository<PetImage, Long> {

    Optional<PetImage> findByIdAndPetId(Long id, Long petId);

    List<PetImage> findByPetId(Long petId);
}
