package fit.se.be_phone_store.config;

import org.springframework.context.annotation.Configuration;

/**
 * Session Configuration (Simplified)
 * Basic session configuration without Redis dependency
 *
 * NOTE: For production, consider using Redis for session storage
 */
@Configuration
public class SessionConfig {

    /**
     * Session Configuration Properties
     */
    public static class SessionProperties {

        // Session timeout in seconds (30 minutes)
        public static final int MAX_INACTIVE_INTERVAL = 1800;

        // Cookie configuration
        public static final String COOKIE_NAME = "PHONE_STORE_SESSION";
        public static final String COOKIE_PATH = "/";
        public static final boolean COOKIE_HTTP_ONLY = true;
        public static final boolean COOKIE_SECURE = false; // Set to true in production with HTTPS

        // Session attribute keys
        public static final String USER_SESSION_KEY = "USER_INFO";
        public static final String CART_SESSION_KEY = "CART_INFO";
        public static final String LAST_ACTIVITY_KEY = "LAST_ACTIVITY";
        public static final String LOGIN_TIME_KEY = "LOGIN_TIME";
        public static final String USER_ROLE_KEY = "USER_ROLE";

        // Session validation
        public static final long SESSION_CLEANUP_INTERVAL = 300000; // 5 minutes
        public static final long IDLE_TIMEOUT_WARNING = 300000; // 5 minutes before expiry
    }
}