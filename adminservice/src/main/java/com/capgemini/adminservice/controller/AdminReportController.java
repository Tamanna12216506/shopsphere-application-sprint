package com.capgemini.adminservice.controller;


import com.capgemini.adminservice.dto.RevenueReportDTO;
import com.capgemini.adminservice.service.AdminReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final AdminReportService reportService;

    // GET /api/admin/reports/revenue
    @GetMapping("/revenue")
    public ResponseEntity<RevenueReportDTO> getRevenueReport() {
        return ResponseEntity.ok(reportService.getRevenueReport());
    }
}
