package com.petadoption.service.impl;

import com.petadoption.dto.request.AdoptionRequestDto;
import com.petadoption.dto.response.AdoptionResponseDto;
import com.petadoption.entity.AdoptionApplication;
import com.petadoption.entity.Pet;
import com.petadoption.entity.User;
import com.petadoption.enums.ApplicationStatus;
import com.petadoption.enums.PetStatus;
import com.petadoption.exception.BusinessException;
import com.petadoption.exception.ResourceNotFoundException;
import com.petadoption.mapper.AdoptionMapper;
import com.petadoption.notification.EmailService;
import com.petadoption.notification.EmailTemplateBuilder;
import com.petadoption.notification.NotificationConstants;
import com.petadoption.repository.AdoptionApplicationRepository;
import com.petadoption.repository.PetRepository;
import com.petadoption.repository.UserRepository;
import com.petadoption.service.AdoptionService;
import com.petadoption.service.AuditLogService;
import com.petadoption.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdoptionServiceImpl implements AdoptionService {

    private final AdoptionApplicationRepository adoptionRepository;
    private final PetRepository petRepository;
    private final UserRepository userRepository;
    private final AdoptionMapper adoptionMapper;
    private final EmailService emailService;
    private final AuditLogService auditLogService;

    private static final List<ApplicationStatus> ACTIVE_STATUSES =
            List.of(ApplicationStatus.PENDING,
                    ApplicationStatus.UNDER_REVIEW,
                    ApplicationStatus.APPROVED);

    @Override
    @Transactional
    public AdoptionResponseDto createApplication(
            AdoptionRequestDto request) {

        Pet pet = petRepository.findById(request.petId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Pet not found"));

        String email =
                SecurityUtil.getCurrentUserEmail();

        User adopter =
                userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Adopter not found"));

        if (pet.getStatus() == PetStatus.ADOPTED) {
            throw new BusinessException(
                    "Pet has already been adopted");
        }

        if (adoptionRepository.existsByPetIdAndAdopterIdAndStatusIn(
                pet.getId(), adopter.getId(), ACTIVE_STATUSES)) {

            throw new BusinessException(
                    "You already have an active application for this pet");
        }

        AdoptionApplication application =
                new AdoptionApplication();

        application.setPet(pet);
        application.setAdopter(adopter);
        application.setApplicantNotes(
                request.applicantNotes());
        AdoptionApplication saved =
                adoptionRepository.save(application);

        auditLogService.saveAuditLog(
                "ADOPTION_APPLICATION_CREATED",
                "AdoptionApplication",
                String.valueOf(saved.getId()),
                SecurityUtil.getCurrentUserEmail(),
                "Adoption application created");

        return adoptionMapper.toResponseDto(saved);
    }

    @Override
    public List<AdoptionResponseDto> getAllApplications() {

        return adoptionRepository.findAll()
                .stream()
                .map(adoptionMapper::toResponseDto)
                .toList();
    }

    @Override
    public List<AdoptionResponseDto>
    getMyApplications() {

        String email =
                SecurityUtil.getCurrentUserEmail();

        User user =
                userRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"));

        return adoptionRepository
                .findByAdopterId(user.getId())
                .stream()
                .map(adoptionMapper::toResponseDto)
                .toList();
    }

    @Override
    public AdoptionResponseDto getMyApplicationById(Long id) {

        String email =
                SecurityUtil.getCurrentUserEmail();

        User user =
                userRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"));

        AdoptionApplication application =
                adoptionRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Application not found"));

        if (!application.getAdopter()
                .getId()
                .equals(user.getId())) {

            throw new BusinessException(
                    "You can only view your own application");
        }

        return adoptionMapper.toResponseDto(application);
    }

    @Override
    public AdoptionResponseDto getApplicationById(Long id) {

        AdoptionApplication application =
                adoptionRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Application not found"));

        return adoptionMapper.toResponseDto(application);
    }

    @Override
    @Transactional
    public AdoptionResponseDto approveApplication(Long id) {

        AdoptionApplication application =
                adoptionRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Application not found"));

        Pet pet = application.getPet();

        if (pet.getStatus() == PetStatus.ADOPTED) {
            throw new BusinessException(
                    "Pet has already been adopted");
        }

        application.setStatus(ApplicationStatus.APPROVED);
        pet.setStatus(PetStatus.ADOPTED);

        petRepository.save(pet);

        AdoptionApplication updated =
                adoptionRepository.save(application);

        List<AdoptionApplication> otherPendingApplications =
                adoptionRepository.findByPetIdAndStatusIn(
                                pet.getId(),
                                List.of(ApplicationStatus.PENDING,
                                        ApplicationStatus.UNDER_REVIEW))
                        .stream()
                        .filter(other -> !other.getId().equals(updated.getId()))
                        .toList();

        for (AdoptionApplication other : otherPendingApplications) {

            other.setStatus(ApplicationStatus.REJECTED);
            adoptionRepository.save(other);

            auditLogService.saveAuditLog(
                    "ADOPTION_APPLICATION_REJECTED",
                    "AdoptionApplication",
                    String.valueOf(other.getId()),
                    SecurityUtil.getCurrentUserEmail(),
                    "Auto-rejected: pet was adopted via another application");

            emailService.sendEmail(
                    other.getAdopter().getEmail(),
                    NotificationConstants.ADOPTION_REJECTED_SUBJECT,
                    EmailTemplateBuilder.adoptionRejected(
                            other.getAdopter().getFirstName())
            );
        }

        auditLogService.saveAuditLog(
                "ADOPTION_APPLICATION_APPROVED",
                "AdoptionApplication",
                String.valueOf(updated.getId()),
                SecurityUtil.getCurrentUserEmail(),
                "Adoption application approved");

        emailService.sendEmail(
                application.getAdopter().getEmail(),
                NotificationConstants.ADOPTION_APPROVED_SUBJECT,
                EmailTemplateBuilder.adoptionApproved(
                        application.getAdopter()
                                .getFirstName())
        );

        return adoptionMapper.toResponseDto(updated);
    }

    @Override
    public AdoptionResponseDto rejectApplication(Long id) {

        AdoptionApplication application =
                adoptionRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Application not found"));

        application.setStatus(ApplicationStatus.REJECTED);

        AdoptionApplication updated =
                adoptionRepository.save(application);

        auditLogService.saveAuditLog(
                "ADOPTION_APPLICATION_REJECTED",
                "AdoptionApplication",
                String.valueOf(updated.getId()),
                SecurityUtil.getCurrentUserEmail(),
                "Adoption application rejected");

        emailService.sendEmail(
                application.getAdopter().getEmail(),
                NotificationConstants.ADOPTION_REJECTED_SUBJECT,
                EmailTemplateBuilder.adoptionRejected(
                        application.getAdopter()
                                .getFirstName())
        );

        return adoptionMapper.toResponseDto(updated);
    }
}