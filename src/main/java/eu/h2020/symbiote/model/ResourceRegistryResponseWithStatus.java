package eu.h2020.symbiote.model;

import eu.h2020.symbiote.core.cci.ResourceRegistryResponse;

/**
 * Created by jawora on 13.04.17.
 */
public class ResourceRegistryResponseWithStatus {
    private int status;
    private ResourceRegistryResponse resourceRegistryResponse;

    public ResourceRegistryResponseWithStatus() {
        this.resourceRegistryResponse = new ResourceRegistryResponse();
    }

    public ResourceRegistryResponseWithStatus(int status, ResourceRegistryResponse resourceRegistryResponse) {
        this.status = status;
        this.resourceRegistryResponse = resourceRegistryResponse;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public ResourceRegistryResponse getResourceRegistryResponse() {
        return resourceRegistryResponse;
    }

    public void setResourceRegistryResponse(ResourceRegistryResponse resourceRegistryResponse) {
        this.resourceRegistryResponse = resourceRegistryResponse;
    }
}
