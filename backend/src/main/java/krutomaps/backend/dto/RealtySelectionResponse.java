package krutomaps.backend.dto;

import krutomaps.backend.entity.Realty;
import lombok.Builder;
import lombok.Data;

import java.util.List;



@Data
@Builder
public class RealtySelectionResponse {
    private List<Realty> realtyList;
    private List<PlaceMarker> preferredPlaces;
    private List<PlaceMarker> avoidedPlaces;
}
