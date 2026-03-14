package com.victoruk.dicestore.common.utility;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * Centralises token hashing so the algorithm is never duplicated.
 * Uses SHA-256 via Apache Commons Codec (already on the Spring Boot classpath).
 *
 * Raw token  → emailed to user
 * Hashed token → stored in DB
 *
 * On reset: hash the incoming raw token, then compare against the stored hash.
 */
public final class TokenHashUtil {

    private TokenHashUtil() {}

    public static String hash(String rawToken) {
        return DigestUtils.sha256Hex(rawToken);
    }
}