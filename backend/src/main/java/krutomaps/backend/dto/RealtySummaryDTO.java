package krutomaps.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@Schema(description = "Краткая информация об объекте недвижимости")
public class RealtySummaryDTO {

    @Schema(description = "ID объекта", example = "101")
    Long id;

    @Schema(description = "Адрес", example = "ул. Ленина, д. 5")
    String address;

    @Schema(description = "Тип (например, аренда)", example = "Аренда")
    String mainType;

    @Schema(description = "Сегмент (офисные / производственные / торговые)", example = "Офисные")
    String segmentType;

    @Schema(description = "Цена аренды в месяц, ₽", example = "75000")
    Double leasePrice;

    @Schema(description = "Широта", example = "55.7512")
    Double latitude;

    @Schema(description = "Долгота", example = "37.6184")
    Double longitude;

    @Schema(description = "Общая площадь, м²", example = "45.5")
    Double totalArea;
}
