package krutomaps.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import krutomaps.backend.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Rubrics", description = "Работа с рубриками заведений")
public class PlaceController {

    private final PlaceRepository placeRepository;

    @GetMapping("/rubrics")
    @Operation(
            summary = "Список всех рубрик",
            description = "Возвращает отсортированный (без учёта регистра) список уникальных рубрик заведений."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    @Cacheable("ALL_RUBRICS")
    public List<String> rubrics() {
        return placeRepository.findAllUniqueRubrics()
                .stream()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }
}
