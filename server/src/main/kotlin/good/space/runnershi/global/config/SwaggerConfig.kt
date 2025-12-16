package good.space.runnershi.global.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .addSecurityItem(SecurityRequirement().addList("Bearer Authentication"))
            .components(
                Components().addSecuritySchemes(
                    "Bearer Authentication",
                    createAPIKeyScheme()
                )
            )
            .info(
                Info()
                    .title("Runner's High API")
                    .description("러너스하이 서비스의 API 명세서입니다.")
                    .version("1.0.0")
            )
    }

    private fun createAPIKeyScheme(): SecurityScheme {
        return SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .bearerFormat("JWT")
            .scheme("bearer")
    }
}
