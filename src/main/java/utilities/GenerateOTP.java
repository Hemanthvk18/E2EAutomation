package utilities;


import com.warrenstrange.googleauth.GoogleAuthenticator;
public class GenerateOTP {
    private static ThreadLocal<GoogleAuthenticator> threadLocalGAuth = new ThreadLocal<GoogleAuthenticator>() {

        @Override
        protected GoogleAuthenticator initialValue() {
            return new GoogleAuthenticator();
        }
    };

    public static String generateGoogleAuthOTP(String secretKey) {
        GoogleAuthenticator gAuth = threadLocalGAuth.get();
        int code = gAuth.getTotpPassword(secretKey);
        String otp = String.format("%06d", code);
        return otp;
    }
}
