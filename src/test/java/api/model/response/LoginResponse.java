package api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginResponse {

    public String token;
    public String userId;
    public String message;

    //ex : response.token = "abc123xyz" --> response.getToken() --> "abc123xyz"

}
