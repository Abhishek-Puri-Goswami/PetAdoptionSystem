package com.petadoption.repository;

import com.petadoption.entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicalRecordRepository
        extends JpaRepository<MedicalRecord, Long> {

    List<MedicalRecord> findByPetId(Long petId);

    boolean existsByPetId(Long petId);
}
