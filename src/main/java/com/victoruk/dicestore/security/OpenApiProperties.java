package com.victoruk.dicestore.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@ConfigurationProperties(prefix = "openapi")
public class OpenApiProperties {

    private String name;
    private String version;
    private String description;

}
