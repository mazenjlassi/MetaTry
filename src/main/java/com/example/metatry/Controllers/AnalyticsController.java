package com.example.metatry.Controllers;

import com.example.metatry.DTOs.AnalyticsRequest;
import com.example.metatry.Services.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @PostMapping("/update")
    public String updateAnalytics(@RequestBody AnalyticsRequest request){

        analyticsService.updateMetrics(request);

        return "Analytics updated successfully";
    }
}