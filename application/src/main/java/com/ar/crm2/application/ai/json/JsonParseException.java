package com.ar.crm2.application.ai.json;

/**
 * Unchecked exception thrown when the parser encounters invalid JSON.
 *
 * <p>The mapper catches this exception at its boundary and re-throws
 * the application-layer controlled exception
 * {@code AccionInvalidaException.forInvalidInput(...)} so callers never
 * see a low-level parser type. The class is public only because the
 * mapper lives in a sibling package — its use is still confined to the
 * {@code application.ai} package tree.
 */
public final class JsonParseException extends RuntimeException {

    public JsonParseException(String message) {
        super(message);
    }
}