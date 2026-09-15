package com.petadoption.repository;

import com.petadoption.entity.Pet;
import com.petadoption.enums.PetStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PetRepository extends JpaRepository<Pet, Long> {
    long countByStatus(PetStatus status);
}