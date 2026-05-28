package auth;

public interface AuthProvider {
    void ensureAuthenticated(); // sets AuthContext values for current thread
}
