package eu.h2020.symbiote.model;

/**
 * Class used as a response to RPC call requesting resource operation
 */
public class RpcResourceResponse {
    private int status;
    private Resource resource;

    public RpcResourceResponse() {
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
        return "RpcResourceResponse{" +
                "status=" + status +
                ", resource=" + resource +
                '}';
    }
}
