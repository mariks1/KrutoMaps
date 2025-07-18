package krutomaps.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Передача строки")
public class MessageInfoDTO {
    @Schema(description = "Произвольная строка", example = "hello world!")
    private String message;
}