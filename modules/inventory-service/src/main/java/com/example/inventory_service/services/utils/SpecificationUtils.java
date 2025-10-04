package com.example.inventory_service.services.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SpecificationUtils {
    public static <S> Specification<S> equal(String field, Object value) {
        if (value == null) return Specification.where(null);
        return ((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(field), value));
    }

    public static <S, T extends Comparable<? super T>> Specification<S> between(String field, T from, T to) {
        if (from == null && to == null) return Specification.where(null);
        if (from != null && to != null) {
            return (((root, query, criteriaBuilder) -> criteriaBuilder.between(root.get(field), from, to)));
        }
        if (from != null)
            return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get(field), from);
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get(field), to);
    }

    public static <S> Specification<S> iLike(String field, String value) {
        if (value == null || value.isBlank()) return Specification.where(null);
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(criteriaBuilder.lower(root.get(field)), "%" + value.toLowerCase() + "%");
    }
}
