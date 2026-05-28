package api;

import api.dtos.master.MasterCreateRequest;
import api.dtos.master.MasterCreateResponse;
import api.support.BaseApiClient;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import utilities.ConfigReader;

import java.util.Map;

public class MasterDvpApiRestAssured extends BaseApiClient implements MasterDvpApi {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    private final ConfigReader cfg = ConfigReader.getConfigReader();

    private String path(int divisionId) {

        return "/api/v1/master-dvps/" + divisionId + "/create";
    }

    @Override
    public MasterCreateResponse createMasterJson(int divisionId, MasterCreateRequest request) {

        try {
            String json = MAPPER.writeValueAsString(request);
            Response r = postJson(path(divisionId), request);

            if (r.statusCode() < 200 || r.statusCode() >= 300) {
                throw new RuntimeException(
                        "MDVP creation failed (" + r.statusCode() + "): " + r.asString()
                );
            }

            return r.as(MasterCreateResponse.class);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to create MDVP for division " + divisionId +
                            ", masterTitle=" + request.masterTitle, e
            );

        }

    }
}
