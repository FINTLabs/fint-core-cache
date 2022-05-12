package no.fintlabs.cache;

import org.springframework.util.StringUtils;

/**
 * Based on rfc2141: https://www.ietf.org/rfc/rfc2141.txt
 */
public class CacheUri {

    public static String create(String orgId, String model) {

        if (!StringUtils.hasText(orgId)) {
            throw new IllegalArgumentException("OrgId must be set on cache implementation");
        }

        if (!StringUtils.hasText(model)) {
            throw new IllegalArgumentException("Model must be set on cache implementation");
        }

        return String.format("urn:fintlabs.no:%s:%s", orgId.toLowerCase(), model.toLowerCase());
    }
}
