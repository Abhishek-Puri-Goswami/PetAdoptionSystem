package com.petadoption.repository;

import com.petadoption.entity.AdoptionApplication;
import com.petadoption.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdoptionApplicationRepository
        extends JpaRepository<AdoptionApplication, Long> {

    List<AdoptionApplication> findByAdopterId(Long adopterId);

    List<AdoptionApplication> findByPetId(Long petId);

    List<AdoptionApplication> findByPetIdAndStatusIn(
            Long petId, List<ApplicationStatus> statuses);

    boolean existsByPetIdAndAdopterIdAndStatusIn(
            Long petId, Long adopterId, List<ApplicationStatus> statuses);

    boolean existsByPetId(Long petId);

    long countByStatus(ApplicationStatus status);

}