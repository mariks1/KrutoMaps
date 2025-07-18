package krutomaps.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
@Schema(description = "Критерии для подбора недвижимости")
public class RealtySelectionRequestDTO {

    @Schema(
            description = "Рубрики заведений, которые хочу видеть рядом",
            example     = "[\"кафе\", \"парк\"]"
    )
    List<String> wantToSee;

    @Schema(
            description = "Рубрики заведений, которые хочу избегать",
            example     = "[\"клубы\"]"
    )
    List<String> dontWantToSee;

    @PositiveOrZero
    @Schema(description = "Минимальная цена, ₽", example = "500")
    Integer priceFrom;

    @PositiveOrZero
    @Schema(description = "Максимальная цена, ₽", example = "15000")
    Integer priceTo;

    @Schema(
            description = "Параметр «этаж» (любой / до 5-го / только 1-й)",
            example     = "любой"
    )
    String floorOption;

    @Schema(
            description = "Типы заведений (офисные, производственные, торговые, любые)",
            example     = "[\"производственные\", \"торговые\"]"
    )
    List<String> placeOptions;

    @Positive
    @Schema(description = "Минимальная площадь, м²", example = "30")
    Integer areaFrom;

    @Positive
    @Schema(description = "Максимальная площадь, м²", example = "100")
    Integer areaTo;
}
