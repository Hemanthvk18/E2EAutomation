package auth;

public final class AuthContext {

    private static final ThreadLocal<String> BEARER = new ThreadLocal<>();
    private static final ThreadLocal<String> SESSION = new ThreadLocal<>();

    public static String getBearerToken() {
        return BEARER.get();
    }

    public static void setBearerToken(String token) {
        BEARER.set(token);
    }

    public static void clearBearerToken() {
        BEARER.remove();
    }

    public static String getSessionId() {
        return SESSION.get();
    }

    public static void setSessionId(String sid) {
        SESSION.set(sid);
    }

    public static void clearSessionId() {
        SESSION.remove();
    }

    public static void clearAll() {
        clearBearerToken();
        clearSessionId();
    }


}
