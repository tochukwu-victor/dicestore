package com.victoruk.dicestore.security.openapi;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@RequiredArgsConstructor
public class OpenApi30Config {

    private final OpenApiProperties properties;

    /**
     * Customizes the OpenAPI specification to set default example values for string properties to an
     * empty string ("").
     *
     * <p>This method ensures that for all schemas in the OpenAPI specification, any property of type
     * "string" without an explicitly defined example will have its example set to an empty string.
     * This improves consistency and ensures that API documentation does not default to "string" as an
     * example value.
     *
     * @return an {@link OpenApiCustomizer} that modifies the OpenAPI specification components.
     */
    @Bean
    public OpenApiCustomizer customOpenApiDefaultStringValues() {
        return openApi -> {
            openApi
                    .getComponents()
                    .getSchemas()
                    .values()
                    .forEach(
                            schema -> {
                                @SuppressWarnings("unchecked")
                                Map<String, Schema<?>> properties =
                                        schema.getProperties(); // Explicitly define the type
                                if (properties != null) { // Ensure properties is not null
                                    properties.forEach(
                                            (name, property) -> {
                                                if ("string".equals(property.getType()) && property.getExample() == null) {
                                                    // Set the default example to an empty string
                                                    property.setExample(org.apache.commons.lang3.StringUtils.EMPTY);
                                                }
                                            });
                                }
                            });
        };
    }

    /**
     * Configures the OpenApi 3.1 bean.
     *
     * @return the OpenApi 3.1 bean
     */
    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        final String apiTitle = String.format("%s API", StringUtils.capitalize(properties.getName()));

// inside your `customOpenAPI()` method
        var info = new Info()
                .title(apiTitle)
                .version(properties.getVersion())
                .description(properties.getDescription())
                .contact(new Contact()
                        .name("Dicestore Support Team")
                        .url("https://dicestore.com")
                        .email("TOCHUKWU.VICTORUK@gmail.com"))
                .license(new License()
                        .name("Apache 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0"));

        var securityScheme =
                new SecurityScheme()
                        .name(securitySchemeName)
                        .type(Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT");

        var components = new Components().addSecuritySchemes(securitySchemeName, securityScheme);

        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(components)
                .info(info);
    }

}