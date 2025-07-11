package krutomaps.backend.controller;

import krutomaps.backend.dto.AreaRangeResponse;
import krutomaps.backend.dto.RealtySelectionRequest;
import krutomaps.backend.dto.PriceRangeResponse;
import krutomaps.backend.dto.RealtySelectionResponse;
import krutomaps.backend.entity.Realty;
import krutomaps.backend.service.RealtyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RealtyController {

    private final RealtyService realtyService;

    @PostMapping("/select-realty")
    public RealtySelectionResponse selectRealty(@RequestBody RealtySelectionRequest request) {
        System.out.println("Получен запрос: " + request);
        return realtyService.findTop5ByCriteria(request);
    }

    @GetMapping("/price-range")
    public PriceRangeResponse getPriceRange() {
        return realtyService.getPriceRange();
    }

    @GetMapping("/area-range")
    public AreaRangeResponse getAreaRange() {
        return realtyService.getAreaRange();
    }

}
