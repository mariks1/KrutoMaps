package krutomaps.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@Schema(description = "Диапазон цен объектов (₽)")
public class PriceRangeResponseDTO {

    @PositiveOrZero
    @Schema(description = "Минимальная цена", example = "45000")
    Double minPrice;

    @PositiveOrZero
    @Schema(description = "Максимальная цена", example = "180000")
    Double maxPrice;
}
