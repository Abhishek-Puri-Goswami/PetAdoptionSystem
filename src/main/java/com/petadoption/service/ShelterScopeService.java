package com.petadoption.service;

import com.petadoption.entity.User;
import com.petadoption.enums.RoleType;
import com.petadoption.exception.BusinessException;
import com.petadoption.exception.ResourceNotFoundException;
import com.petadoption.repository.UserRepository;
import com.petadoption.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Shared shelter-data-isolation checks used by every service that lists or
 * manages shelter-owned records (adoption applications, appointments).
 * A SYSTEM_ADMIN always has full access; every other caller is restricted
 * to their own shelter's data.
 */
@Service
@RequiredArgsConstructor
public class ShelterScopeService {

    private final UserRepository userRepository;

    public User currentUser() {

        String email = SecurityUtil.getCurrentUserEmail();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));
    }

    public boolean isSystemAdmin(User user) {

        return user.getRoles().stream()
                .anyMatch(role ->
                        role.getName() == RoleType.ROLE_SYSTEM_ADMIN);
    }

    public Long requireOwnShelterId(User user) {

        if (user.getShelter() == null) {
            throw new BusinessException(
                    "You must be assigned to a shelter to perform "
                            + "this action");
        }

        return user.getShelter().getId();
    }

    /**
     * Throws unless the caller is a SYSTEM_ADMIN or the resource belongs
     * to the caller's own shelter.
     */
    public void verifyShelterAccess(
            User caller,
            Long resourceShelterId) {

        if (isSystemAdmin(caller)) {
            return;
        }

        Long callerShelterId = requireOwnShelterId(caller);

        if (!callerShelterId.equals(resourceShelterId)) {
            throw new BusinessException(
                    "You do not have access to this shelter's data");
        }
    }
}
