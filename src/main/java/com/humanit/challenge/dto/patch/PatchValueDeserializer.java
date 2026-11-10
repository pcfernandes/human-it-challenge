package com.humanit.challenge.dto.patch;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * Jackson deserializer for {@link PatchValue}.
 *
 * <ul>
 *   <li>A missing property never reaches this deserializer: the DTO field keeps its
 *       {@link PatchValue#absent()} default.</li>
 *   <li>An explicit JSON {@code null} is resolved through {@link #getNullValue}, which returns
 *       {@link PatchValue#presentNull()}.</li>
 *   <li>Any other JSON value is bound with the contextual value type, so generic types such as
 *       {@code PatchValue<List<ClientDocumentPatchRequest>>} are resolved from the declaring
 *       property or the root type.</li>
 * </ul>
 */
public class PatchValueDeserializer extends StdDeserializer<PatchValue<?>> implements ContextualDeserializer {

    private static final long serialVersionUID = 1L;

    private final transient JsonDeserializer<Object> valueDeserializer;

    /** Required by Jackson for annotation-based instantiation; contextualized before use. */
    public PatchValueDeserializer() {
        this(null);
    }

    private PatchValueDeserializer(JsonDeserializer<Object> valueDeserializer) {
        super(PatchValue.class);
        this.valueDeserializer = valueDeserializer;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property)
            throws JsonMappingException {
        JavaType declared = property != null ? property.getType() : ctxt.getContextualType();
        JavaType valueType = declared != null ? declared.containedType(0) : null;
        if (valueType == null) {
            valueType = ctxt.constructType(Object.class);
        }
        @SuppressWarnings("unchecked")
        JsonDeserializer<Object> contentDeserializer =
                (JsonDeserializer<Object>) ctxt.findContextualValueDeserializer(valueType, property);
        return new PatchValueDeserializer(contentDeserializer);
    }

    @Override
    public PatchValue<?> deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
        if (valueDeserializer == null) {
            throw new IllegalStateException("PatchValue deserializer was used without contextualization");
        }
        Object value = valueDeserializer.deserialize(parser, ctxt);
        return PatchValue.of(value);
    }

    /** Invoked by Jackson when the JSON token is {@code null}. */
    @Override
    public PatchValue<?> getNullValue(DeserializationContext ctxt) {
        return PatchValue.presentNull();
    }
}
