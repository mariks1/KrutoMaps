package krutomaps.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import krutomaps.backend.dto.AreaRangeResponse;
import krutomaps.backend.dto.RealtySelectionRequest;
import krutomaps.backend.dto.PriceRangeResponse;
import krutomaps.backend.dto.RealtySelectionResponse;
import krutomaps.backend.service.RealtyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RealtyController {

    private final RealtyService realtyService;

    @PostMapping("/select-realty")
    public RealtySelectionResponse selectRealty(@RequestBody RealtySelectionRequest request) {
        System.out.println("Получен запрос: " + request);

        try {
            return realtyService.findTop5ByCriteria(request);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
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
