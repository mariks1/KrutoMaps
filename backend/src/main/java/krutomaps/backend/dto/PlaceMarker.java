package krutomaps.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PlaceMarker {
    private Long id;
    private String name;
    private String address;
    private Double lat;
    private Double lon;
    private List<String> rubrics;
    private String markerType;
}
