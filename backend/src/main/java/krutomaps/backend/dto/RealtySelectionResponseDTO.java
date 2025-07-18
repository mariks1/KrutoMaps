package krutomaps.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
@Schema(description = "Результат подбора недвижимости")
public class RealtySelectionResponseDTO {

    @Schema(
            description = "Список подобранных объектов (до 5)",
            implementation = RealtySummaryDTO.class
    )
    List<RealtySummaryDTO> realtyEntityList;

    @Schema(
            description = "Список предпочитаемых заведений",
            implementation = PlaceMarkerDTO.class
    )
    List<PlaceMarkerDTO> preferredPlaces;

    @Schema(
            description = "Список избегаемых заведений",
            implementation = PlaceMarkerDTO.class
    )
    List<PlaceMarkerDTO> avoidedPlaces;
}
