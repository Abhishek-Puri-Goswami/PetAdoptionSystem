package com.petadoption.repository;

import com.petadoption.entity.Shelter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShelterRepository
        extends JpaRepository<Shelter, Long> {

    Page<Shelter> findByNameContainingIgnoreCase(
            String name, Pageable pageable);
}