package krutomaps.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@Schema(description = "Диапазон площадей объектов (м²)")
public class AreaRangeResponseDTO {

    @PositiveOrZero
    @Schema(
            description = "Минимальная площадь",
            example     = "25.0"
    )
    Double minArea;

    @PositiveOrZero
    @Schema(
            description = "Максимальная площадь",
            example     = "120.5"
    )
    Double maxArea;
}

