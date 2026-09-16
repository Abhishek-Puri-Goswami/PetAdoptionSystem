package com.petadoption.repository;

import com.petadoption.dto.request.PetSearchCriteria;
import com.petadoption.entity.Pet;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class PetSpecification {

    private PetSpecification() {
    }

    public static Specification<Pet> fromCriteria(
            PetSearchCriteria criteria) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (criteria == null) {
                return cb.conjunction();
            }

            if (StringUtils.hasText(criteria.species())) {
                predicates.add(cb.equal(
                        cb.lower(root.get("species")),
                        criteria.species().toLowerCase()));
            }

            if (criteria.energyLevel() != null) {
                predicates.add(cb.equal(
                        root.get("energyLevel"), criteria.energyLevel()));
            }

            if (criteria.temperament() != null) {
                predicates.add(cb.equal(
                        root.get("temperament"), criteria.temperament()));
            }

            if (criteria.minAge() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("age"), criteria.minAge()));
            }

            if (criteria.maxAge() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("age"), criteria.maxAge()));
            }

            if (criteria.status() != null) {
                predicates.add(cb.equal(
                        root.get("status"), criteria.status()));
            }

            if (StringUtils.hasText(criteria.search())) {

                String like = "%" + criteria.search().toLowerCase() + "%";

                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("breed")), like),
                        cb.like(cb.lower(root.get("description")), like)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
