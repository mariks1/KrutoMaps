package krutomaps.backend.dto;

import lombok.Value;

@Value
public class ErrorResponseDTO {
    String code;
    String message;
}
