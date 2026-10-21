package com.prueba.transport_orders.repository.specification;

import com.prueba.transport_orders.domain.entity.Order;
import com.prueba.transport_orders.dto.order.OrderFilter;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderSpecifications {


     private OrderSpecifications() {
    }

    public static Specification<Order> withFilter(OrderFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }
            if (hasText(filter.getOrigin())) {
                predicates.add(cb.like(cb.lower(root.get("origin")),
                        "%" + filter.getOrigin().trim().toLowerCase() + "%"));
            }
            if (hasText(filter.getDestination())) {
                predicates.add(cb.like(cb.lower(root.get("destination")),
                        "%" + filter.getDestination().trim().toLowerCase() + "%"));
            }
            if (filter.getFrom() != null) {
                LocalDateTime fromDateTime = filter.getFrom().atStartOfDay();
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), fromDateTime));
            }
            if (filter.getTo() != null) {
                LocalDateTime toExclusive = filter.getTo().plusDays(1).atStartOfDay();
                predicates.add(cb.lessThan(root.get("createdAt"), toExclusive));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

}
