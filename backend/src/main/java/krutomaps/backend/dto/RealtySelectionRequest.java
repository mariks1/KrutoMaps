package krutomaps.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RealtySelectionRequest {
    private List<String> wantToSee;
    private List<String> dontWantToSee;
    private Integer priceFrom;
    private Integer priceTo;
    private String floorOption;
    private List<String> placeOptions;
    private Integer areaFrom;
    private Integer areaTo;
}
