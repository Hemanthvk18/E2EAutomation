package api.dtos.master;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties (ignoreUnknown = true)
public class MasterCreateResponse {
    public boolean success;
    public boolean response;
    public String message;
    public Data data;

    @JsonIgnoreProperties (ignoreUnknown = true)
    public static class Data {
        public Integer id;
        public String masterNumber; // if API returns it
        public String masterTitle;
        public Integer mainscopeId;
        public Integer masterDvpTypeId;
        public String createdDate;
        public String createdUserId;
        public String createdUserName;
        public String updatedDate;
        public String updatedUserId;
        public String updatedUserName;

    }

}