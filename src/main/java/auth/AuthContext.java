package auth;

public final class AuthContext {

    private static final ThreadLocal<String> BEARER = new ThreadLocal<>();
    private static final ThreadLocal<String> SESSION = new ThreadLocal<>();

    //Bearer token
    public static String getBearerToken() {
        return BEARER.get();
    }

    public static void setBearerToken(String token) {
        BEARER.set(token);
    }

    public static void clearBearerToken() {
        BEARER.remove();
    }

    //Session ID
    public static String getSessionId() {
        return SESSION.get();
    }

    public static void setSessionId(String sid) {
        SESSION.set(sid);
    }

    public static void clearSessionId() {
        SESSION.remove();
    }

    // Clear all auth context
    public static void clearAll() {
        clearBearerToken();
        clearSessionId();
    }


}
