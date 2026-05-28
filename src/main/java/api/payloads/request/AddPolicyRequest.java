package api.payloads.request;

public class AddPolicyRequest {

    private String insuranceJobId;

    private String policyNumber;

    public String getInsuranceJobId() {
        return insuranceJobId;
    }

    public void setInsuranceJobId(String insuranceJobId) {
        this.insuranceJobId = insuranceJobId;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }
}