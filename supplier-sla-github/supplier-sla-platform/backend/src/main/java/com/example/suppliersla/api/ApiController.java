package com.example.suppliersla.api;

import com.example.suppliersla.demo.DemoDataService;
import com.example.suppliersla.engine.EvaluationRequest;
import com.example.suppliersla.engine.EvaluationResponse;
import com.example.suppliersla.engine.SlaEvaluationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "http://localhost:5173")
public class ApiController {
    private final DemoDataService demoDataService;
    private final SlaEvaluationService evaluationService;

    public ApiController(DemoDataService demoDataService, SlaEvaluationService evaluationService) {
        this.demoDataService = demoDataService;
        this.evaluationService = evaluationService;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "application", "Supplier SLA Platform");
    }

    @GetMapping("/demo/monthly-report")
    public Map<String, Object> report(@RequestParam(defaultValue = "2026-10") String month) {
        return demoDataService.monthlyReport(month);
    }

    @GetMapping("/demo/orders")
    public Object orders() {
        return demoDataService.orders();
    }

    @GetMapping("/demo/orders/{orderNumber}/explanation")
    public Object explanation(@PathVariable String orderNumber) {
        return demoDataService.explanation(orderNumber);
    }

    @PostMapping("/evaluations/preview")
    public EvaluationResponse preview(@Valid @RequestBody EvaluationRequest request) {
        return evaluationService.evaluate(request);
    }
}
