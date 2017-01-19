package eu.h2020.symbiote.model;

/**
 * Class used as a response to RPC call requesting resource creation
 */
public class ResourceCreationResponse {
    private int status;
    private Resource resource;

    public ResourceCreationResponse() {
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    @Override
    public String toString() {
        return "ResourceCreationResponse{" +
                "status=" + status +
                ", resource=" + resource +
                '}';
    }
}
