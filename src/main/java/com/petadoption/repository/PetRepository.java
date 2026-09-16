package com.petadoption.repository;

import com.petadoption.entity.Pet;
import com.petadoption.enums.PetStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PetRepository
        extends JpaRepository<Pet, Long>, JpaSpecificationExecutor<Pet> {
    long countByStatus(PetStatus status);
}