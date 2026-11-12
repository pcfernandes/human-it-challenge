package com.humanit.challenge.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springdoc.core.customizers.OpenApiCustomizer;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.http.MediaType;

/**
 * Shared application metadata and JWT bearer-authentication scheme for OpenAPI.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Human IT Challenge API")
                        .version("0.0.1-SNAPSHOT")
                        .description("REST API for managing Clients and Documents. Business operations require "
                                + "a JWT bearer token. POST /auth/login is public."))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT access token returned by POST /auth/login.")));
    }

    @Bean
    public OpenApiCustomizer problemDetailsResponses() {
        return openApi -> {
            openApi.getComponents().addSchemas("ProblemDetail", new ObjectSchema()
                    .description("RFC 9457 Problem Details response. Validation failures may include an errors array.")
                    .addProperty("type", new StringSchema().format("uri").description("URI reference identifying the problem type."))
                    .addProperty("title", new StringSchema().description("Short, human-readable summary of the problem."))
                    .addProperty("status", new Schema<Integer>().type("integer").format("int32").description("HTTP status code."))
                    .addProperty("detail", new StringSchema().description("Human-readable explanation specific to this occurrence."))
                    .addProperty("instance", new StringSchema().format("uri").description("URI reference for this problem occurrence."))
                    .addProperty("errors", new ArraySchema().items(new ObjectSchema()
                            .addProperty("field", new StringSchema())
                            .addProperty("message", new StringSchema()))));

            markNonBlankStrings(openApi, "ClientCreateRequest",
                    "firstName", "lastName", "taxIdentifier", "email", "phoneNumber");
            markNonBlankStrings(openApi, "ClientPutRequest",
                    "firstName", "lastName", "taxIdentifier", "email", "phoneNumber");
            markNonBlankStrings(openApi, "ClientDocumentPutRequest", "number");
            markNonBlankStrings(openApi, "DocumentCreateRequest", "number");
            markNonBlankStrings(openApi, "DocumentPutRequest", "number");

            openApi.getPaths().values().forEach(pathItem -> pathItem.readOperations().forEach(operation -> {
                operation.getResponses().values().stream()
                        .filter(response -> response.getDescription() != null
                                && response.getDescription().contains("(Problem Details)"))
                        .forEach(response -> response.content(new Content().addMediaType(
                                MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                new io.swagger.v3.oas.models.media.MediaType()
                                        .schema(new Schema<>().$ref("#/components/schemas/ProblemDetail")))));
            }));
        };
    }

    private void markNonBlankStrings(OpenAPI openApi, String schemaName, String... propertyNames) {
        Schema<?> schema = openApi.getComponents().getSchemas().get(schemaName);
        if (schema == null || schema.getProperties() == null) {
            return;
        }
        for (String propertyName : propertyNames) {
            Schema<?> property = (Schema<?>) schema.getProperties().get(propertyName);
            if (property != null) {
                // springdoc maps @NotBlank to minLength=0; report the actual non-empty constraint.
                property.setMinLength(1);
            }
        }
    }
}
