package krutomaps.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
@Schema(description = "Маркер заведения на карте")
public class PlaceMarkerDTO {

    @Schema(description = "Уникальный идентификатор", example = "42")
    Long id;

    @NotBlank
    @Schema(description = "Название заведения", example = "Coffee Bar")
    String name;

    @NotBlank
    @Schema(description = "Адрес", example = "ул. Пушкина, д. 10")
    String address;

    @NotNull
    @Schema(description = "Широта", example = "55.7558")
    Double latitude;

    @NotNull
    @Schema(description = "Долгота", example = "37.6173")
    Double longitude;

    @Schema(
            description = "Список рубрик (тегов)",
            example     = "[\"кафе\", \"кофейня\"]"
    )
    List<String> rubrics;


    @Schema(
            description = "Отметка: предпочитаемые или избегаемые заведения",
            example     = "PREFERRED"
    )
    MarkerType markerType;

    public enum MarkerType {
        @Schema(description = "Предпочитаемые") PREFERRED,
        @Schema(description = "Избегаемые") AVOIDED }

}
