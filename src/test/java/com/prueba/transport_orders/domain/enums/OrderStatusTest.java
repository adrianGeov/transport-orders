package com.prueba.transport_orders.domain.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderStatusTest {

     @ParameterizedTest(name = "{0} -> {1} = {2}")
    @CsvSource({
            "CREATED,    IN_TRANSIT, true",
            "CREATED,    CANCELLED,  true",
            "CREATED,    DELIVERED,  false",
            "CREATED,    CREATED,    false",
            "IN_TRANSIT, DELIVERED,  true",
            "IN_TRANSIT, CANCELLED,  true",
            "IN_TRANSIT, CREATED,    false",
            "DELIVERED,  CANCELLED,  false",
            "DELIVERED,  IN_TRANSIT, false",
            "CANCELLED,  CREATED,    false",
            "CANCELLED,  IN_TRANSIT, false"
    })
    @DisplayName("Valida el flujo de transiciones entre estados")
    void canTransitionTo(OrderStatus current, OrderStatus target, boolean expected) {
        assertThat(current.canTransitionTo(target)).isEqualTo(expected);
    }

    @Test
    @DisplayName("Una transición a null no está permitida")
    void canTransitionTo_nullTarget_returnsFalse() {
        assertThat(OrderStatus.CREATED.canTransitionTo(null)).isFalse();
    }

}
