package krutomaps.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AreaRangeResponse {
    private Double minArea;
    private Double maxArea;
}

