package krutomaps.backend.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "KrutoMaps API",
                version = "v1",
                description = "Сервис подбора недвижимости"
        )
)
@Configuration
public class OpenApiConfiguration {

}
