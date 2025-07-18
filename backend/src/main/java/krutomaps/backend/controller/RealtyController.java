package krutomaps.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import krutomaps.backend.dto.AreaRangeResponseDTO;
import krutomaps.backend.dto.RealtySelectionRequestDTO;
import krutomaps.backend.dto.PriceRangeResponseDTO;
import krutomaps.backend.dto.RealtySelectionResponseDTO;
import krutomaps.backend.service.RealtyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Valid
@Tag(name = "Realty", description = "Подбор и агрегаты по недвижимости")
public class RealtyController {

    private final RealtyService realtyService;


    @PostMapping("/selection")
    @Operation(
            summary = "Подобрать топ‑5 объектов по критериям",
            description = """
            Возвращает до пяти объектов недвижимости, удовлетворяющих заданным фильтрам.
            Если ни один объект не найден — код 204 (No Content).
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Найдены объекты",
                    content = @Content(schema = @Schema(implementation = RealtySelectionResponseDTO.class))),
            @ApiResponse(responseCode = "204", description = "Ничего не найдено"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации входных данных")
    })
    public ResponseEntity<RealtySelectionResponseDTO> selectRealty(
            @Valid
            @Parameter(description = "Критерии отбора", required = true)
            @RequestBody RealtySelectionRequestDTO request) {

        log.info("POST /realty/selection {}", request);

        RealtySelectionResponseDTO dto = realtyService.findTop5ByCriteria(request);

        return dto.getRealtyEntityList().isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(dto);
    }

    @GetMapping("/price-range")
    @Operation(
            summary = "Диапазон цен",
            description = "Возвращает минимальную и максимальную цену всех объявлений."
    )
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = PriceRangeResponseDTO.class)))
    public ResponseEntity<PriceRangeResponseDTO> getPriceRange() {
        return ResponseEntity.ok(realtyService.getPriceRange());
    }


    @GetMapping("/area-range")
    @Operation(
            summary = "Диапазон площадей",
            description = "Возвращает минимальную и максимальную площадь объектов."
    )
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = AreaRangeResponseDTO.class)))
    public ResponseEntity<AreaRangeResponseDTO> getAreaRange() {
        return ResponseEntity.ok(realtyService.getAreaRange());
    }

}
