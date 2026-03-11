package com.community.common.util;

import com.community.common.constant.SecurityConstants;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtil {
    private SecurityUtil() {
    }

    public static String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? null : authentication.getName();
    }

    public static Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getCredentials() == null) {
            return null;
        }
        Object credentials = authentication.getCredentials();
        if (credentials instanceof Claims claims) {
            Object uid = claims.get(SecurityConstants.CLAIM_USER_ID);
            if (uid instanceof Integer intValue) {
                return intValue.longValue();
            }
            if (uid instanceof Long longValue) {
                return longValue;
            }
        }
        return null;
    }

    public static String currentRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getCredentials() == null) {
            return null;
        }
        Object credentials = authentication.getCredentials();
        if (credentials instanceof Claims claims) {
            Object role = claims.get(SecurityConstants.CLAIM_ROLE);
            return role == null ? null : role.toString();
        }
        return null;
    }
}

