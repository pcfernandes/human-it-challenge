package com.humanit.challenge.dto.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.humanit.challenge.dto.document.DocumentResponse;

class ClientResponseShapeTest {

    @Test
    void clientResponseEmbedsDocumentResponses() {
        var documents = Arrays.stream(ClientResponse.class.getRecordComponents())
                .filter(component -> component.getName().equals("documents"))
                .findFirst()
                .orElseThrow();

        assertThat(documents.getGenericType().getTypeName())
                .isEqualTo(List.class.getName() + "<" + DocumentResponse.class.getName() + ">");
    }

    @Test
    void documentResponseExposesScalarClientIdOnly() {
        var components = Arrays.stream(DocumentResponse.class.getRecordComponents())
                .map(component -> component.getName() + ":" + component.getType().getName())
                .toList();

        assertThat(components).contains("clientId:java.lang.Long");
        assertThat(components).noneMatch(c -> c.endsWith(":" + ClientResponse.class.getName())
                || c.endsWith(":com.humanit.challenge.domain.Client"));
    }
}
