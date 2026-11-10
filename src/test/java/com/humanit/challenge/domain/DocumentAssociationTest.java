package com.humanit.challenge.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class DocumentAssociationTest {

    @Test
    void assignClientRejectsNull() {
        Document document = new Document("document-number", null, LocalDate.of(2030, 1, 1));

        assertThatThrownBy(() -> document.assignClient(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("client must not be null");
    }
}
