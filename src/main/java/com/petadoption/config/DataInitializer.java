package com.petadoption.config;

import com.petadoption.entity.Role;
import com.petadoption.enums.RoleType;
import com.petadoption.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {

        for (RoleType roleType : RoleType.values()) {

            roleRepository.findByName(roleType)
                    .orElseGet(() -> {
                        Role role = new Role();
                        role.setName(roleType);
                        return roleRepository.save(role);
                    });
        }
    }
}