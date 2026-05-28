package api;


import api.dtos.master.MasterCreateRequest;
import api.dtos.master.MasterCreateResponse;

public interface MasterDvpApi {

    MasterCreateResponse createMasterJson(int divisionId, MasterCreateRequest req);
}
