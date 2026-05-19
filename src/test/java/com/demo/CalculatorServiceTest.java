package com.demo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculatorServiceTest {

    private final CalculatorService calculatorService = new CalculatorService();

    @Test
    void addShouldReturnSum() {
        assertEquals(5, calculatorService.add(2, 3));
    }

    @Test
    void subtractShouldReturnDifference() {
        assertEquals(1, calculatorService.subtract(3, 2));
    }
}