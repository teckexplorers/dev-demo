package com.demo;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/calculator")
public class CalculatorController {

    private final CalculatorService calculatorService;

    public CalculatorController(CalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    @GetMapping(value = "/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public CalculationResponse add(@RequestParam int a, @RequestParam int b) {
        return new CalculationResponse("add", a, b, calculatorService.add(a, b));
    }

    @GetMapping(value = "/subtract", produces = MediaType.APPLICATION_JSON_VALUE)
    public CalculationResponse subtract(@RequestParam int a, @RequestParam int b) {
        return new CalculationResponse("subtract", a, b, calculatorService.subtract(a, b));
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}