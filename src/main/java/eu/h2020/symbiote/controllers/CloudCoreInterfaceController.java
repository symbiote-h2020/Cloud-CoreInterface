package eu.h2020.symbiote.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringPlatform;
import eu.h2020.symbiote.communication.RabbitManager;
import eu.h2020.symbiote.core.cci.RDFResourceRegistryRequest;
import eu.h2020.symbiote.core.cci.ResourceRegistryRequest;
import eu.h2020.symbiote.core.cci.ResourceRegistryResponse;
import eu.h2020.symbiote.core.internal.CoreResourceRegistryRequest;
import eu.h2020.symbiote.core.internal.CoreResourceRegistryResponse;
import eu.h2020.symbiote.core.internal.DescriptionType;
import eu.h2020.symbiote.core.model.resources.Resource;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

/**
 * Class defining all REST endpoints.
 * <p>
 * CloudCoreInterface, as the name suggests, is just an interface, therefore it forwards all requests to
 * modules responsible for handling them via RabbitMQ.
 */
@RestController
public class CloudCoreInterfaceController {
    private static final String URI_PREFIX = "/cloudCoreInterface/v1";

    public static final Log log = LogFactory.getLog(CloudCoreInterfaceController.class);

    private enum CoreOperationType {CREATE, MODIFY, DELETE}

    private final RabbitManager rabbitManager;

    /**
     * Class constructor which autowires RabbitManager bean.
     *
     * @param rabbitManager RabbitManager bean
     */
    @Autowired
    public CloudCoreInterfaceController(RabbitManager rabbitManager) {
        this.rabbitManager = rabbitManager;
    }

    private CoreResourceRegistryRequest prepareRdfRequest(String platformId, RDFResourceRegistryRequest resourceRegistryRequest, String token) {
        CoreResourceRegistryRequest coreRequest = new CoreResourceRegistryRequest();
        coreRequest.setToken(token);
        coreRequest.setDescriptionType(DescriptionType.RDF);
        coreRequest.setPlatformId(platformId);

        try {
            ObjectMapper mapper = new ObjectMapper();

            String resourcesJson = mapper.writeValueAsString(resourceRegistryRequest.getRdfInfo());
            coreRequest.setBody(resourcesJson);

            log.debug("Request for Core Services prepared");

        } catch (JsonProcessingException e) {
            log.error("Error while handling resource creation request", e);
            return null;
        }

        return coreRequest;
    }

    private CoreResourceRegistryRequest prepareBasicRequest(String platformId, ResourceRegistryRequest resourceRegistryRequest, String token) {
        if (resourceRegistryRequest == null)
            return null;

        CoreResourceRegistryRequest coreRequest = new CoreResourceRegistryRequest();
        coreRequest.setToken(token);
        coreRequest.setDescriptionType(DescriptionType.BASIC);
        coreRequest.setPlatformId(platformId);

        try {
            ObjectMapper mapper = new ObjectMapper();

            String resourcesJson = mapper.writerFor(new TypeReference<Map<String, Resource>>() {
            }).writeValueAsString(resourceRegistryRequest.getResources());
            coreRequest.setBody(resourcesJson);

            log.debug("Request for Core Services prepared");


        } catch (JsonProcessingException e) {
            log.error("Error while handling resource creation request", e);
            return null;
        }

        return coreRequest;
    }

    private ResponseEntity handleCoreResourceRequest(CoreResourceRegistryRequest coreResourceRegistryRequest, CoreOperationType coreOperationType) {
        ResourceRegistryResponse response = new ResourceRegistryResponse();

        if (coreResourceRegistryRequest == null) {
            log.error("Error while handling resource request");
            response.setMessage("Error while parsing message body. No data has been processed by Core Services.");
            response.setResources(null);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        log.debug("Sending request to Core Services: " + coreResourceRegistryRequest.getBody());
        CoreResourceRegistryResponse coreResponse = null;
        switch (coreOperationType) {
            case CREATE:
                coreResponse = rabbitManager.sendResourceCreationRequest(coreResourceRegistryRequest);
                break;
            case MODIFY:
                coreResponse = rabbitManager.sendResourceModificationRequest(coreResourceRegistryRequest);
                break;
            case DELETE:
                coreResponse = rabbitManager.sendResourceRemovalRequest(coreResourceRegistryRequest);
                break;
        }

        //Timeout or exception on our side
        if (coreResponse == null) {
            log.debug("Timeout on handling request by Core Services");
            response.setMessage("Timeout on Core Services side. Operation might have been performed, but response did not arrive on time.");
            response.setResources(null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        log.debug("Response from Core Services received: " + coreResponse.getStatus() + ", " + coreResponse.getMessage() + ", " + coreResponse.getBody());

        Map<String, Resource> responseMapOfResources = null;

        if (coreResponse.getBody() != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                responseMapOfResources = mapper.readValue(coreResponse.getBody(), new TypeReference<Map<String, Resource>>() {
                });
            } catch (IOException e) {
                log.error("Error while parsing response body from Core Services", e);
                response.setMessage("Error while parsing response body from Core Service. Operation might have been performed, but response was malformed.");
                response.setResources(null);
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        response.setMessage(coreResponse.getMessage());
        response.setResources(responseMapOfResources);

        log.debug("ResourceRegistryResponse created and returned to endpoint");

        return new ResponseEntity<>(response, HttpStatus.valueOf(coreResponse.getStatus()));
    }


    /**
     * Endpoint for creating resources using RDF description.
     *
     * @param platformId              ID of a platform that resources belong to
     * @param resourceRegistryRequest request containing resources to be registered
     * @param token                   authorization token
     * @return created resources (with resourceId filled) with appropriate HTTP status code
     */
    @ApiOperation(value = "Register resources (RDF)",
            notes = "Register resources using extended RDF description",
            response = ResourceRegistryResponse.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns resources map in a form <internalId, Resource>", response = String.class, responseContainer = "Map"),
            @ApiResponse(code = 500, message = "Error on server side")})
    @RequestMapping(method = RequestMethod.POST,
            value = URI_PREFIX + "/platforms/{platformId}/rdfResources")
    public ResponseEntity createRdfResources(@ApiParam(value = "ID of a platform that resources belong to", required = true) @PathVariable("platformId") String platformId,
                                             @ApiParam(value = "Request body, containing RDF description of resources to register", required = true) @RequestBody RDFResourceRegistryRequest resourceRegistryRequest,
                                             @ApiParam(value = "A valid token issued by a member of the SymbIoTe Security Roaming", required = true) @RequestHeader("X-Auth-Token") String token) {
        log.debug("Request for creation of RDF resources");

        CoreResourceRegistryRequest coreRequest = prepareRdfRequest(platformId, resourceRegistryRequest, token);
        return handleCoreResourceRequest(coreRequest, CoreOperationType.CREATE);
    }

    /**
     * Endpoint for modifying resources using RDF description.
     *
     * @param platformId              ID of a platform that resources belong to
     * @param resourceRegistryRequest request containing resources to be modified
     * @param token                   authorization token
     * @return modified resources with appropriate HTTP status code
     */
    @ApiOperation(value = "Modify resources (RDF)",
            notes = "Modify registered resources using extended RDF description",
            response = ResourceRegistryResponse.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns resources map in a form <internalId, Resource>", response = String.class, responseContainer = "Map"),
            @ApiResponse(code = 500, message = "Error on server side")})
    @RequestMapping(method = RequestMethod.PUT,
            value = URI_PREFIX + "/platforms/{platformId}/rdfResources")
    public ResponseEntity modifyRdfResource(@ApiParam(value = "ID of a platform that resources belong to", required = true) @PathVariable("platformId") String platformId,
                                            @ApiParam(value = "Request body, containing RDF description of resources to modify", required = true) @RequestBody RDFResourceRegistryRequest resourceRegistryRequest,
                                            @ApiParam(value = "A valid token issued by a member of the SymbIoTe Security Roaming", required = true) @RequestHeader("X-Auth-Token") String token) {
        log.debug("Request for modification of RDF resources");

        CoreResourceRegistryRequest coreRequest = prepareRdfRequest(platformId, resourceRegistryRequest, token);
        return handleCoreResourceRequest(coreRequest, CoreOperationType.MODIFY);
    }

    /**
     * Endpoint for deleting resources using RDF description.
     *
     * @param platformId              ID of a platform that resources belong to
     * @param resourceRegistryRequest request containing resources to be deleted
     * @param token                   authorization token
     * @return deleted resources with appropriate HTTP status code
     */
    @ApiOperation(value = "Delete resources (RDF)",
            notes = "Delete registered resources using extended RDF description",
            response = ResourceRegistryResponse.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns resources map in a form <internalId, Resource>", response = String.class, responseContainer = "Map"),
            @ApiResponse(code = 500, message = "Error on server side")})
    @RequestMapping(method = RequestMethod.DELETE,
            value = URI_PREFIX + "/platforms/{platformId}/rdfResources")
    public ResponseEntity deleteRdfResource(@ApiParam(value = "ID of a platform that resources belong to", required = true) @PathVariable("platformId") String platformId,
                                            @ApiParam(value = "Request body, containing RDF description of resources to delete", required = true) @RequestBody RDFResourceRegistryRequest resourceRegistryRequest,
                                            @ApiParam(value = "A valid token issued by a member of the SymbIoTe Security Roaming", required = true) @RequestHeader("X-Auth-Token") String token) {
        log.debug("Request for removal of RDF resources");

        CoreResourceRegistryRequest coreRequest = prepareRdfRequest(platformId, resourceRegistryRequest, token);
        return handleCoreResourceRequest(coreRequest, CoreOperationType.DELETE);

    }

    /**
     * Endpoint for creating resources using JSON description.
     *
     * @param platformId              ID of a platform that resources belong to
     * @param resourceRegistryRequest request containing resources to be registered
     * @param token                   authorization token
     * @return created resources (with resourceId filled) with appropriate HTTP status code
     */
    @ApiOperation(value = "Create resources (JSON)",
            notes = "Create resources using basic JSON description",
            response = ResourceRegistryResponse.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns resources map in a form <internalId, Resource>", response = String.class, responseContainer = "Map"),
            @ApiResponse(code = 500, message = "Error on server side")})
    @RequestMapping(method = RequestMethod.POST,
            value = URI_PREFIX + "/platforms/{platformId}/resources")
    public ResponseEntity createResources(@ApiParam(value = "ID of a platform that resources belong to", required = true) @PathVariable("platformId") String platformId,
                                          @ApiParam(value = "Request body, containing JSON description of resources to create", required = true) @RequestBody ResourceRegistryRequest resourceRegistryRequest,
                                          @ApiParam(value = "A valid token issued by a member of the SymbIoTe Security Roaming", required = true) @RequestHeader("X-Auth-Token") String token) {
        log.debug("Request for creation of basic resources");

        CoreResourceRegistryRequest coreRequest = prepareBasicRequest(platformId, resourceRegistryRequest, token);
        return handleCoreResourceRequest(coreRequest, CoreOperationType.CREATE);
    }

    /**
     * Endpoint for modifying resource using JSON description.
     *
     * @param platformId              ID of a platform that resource belongs to; if platform ID is specified in Resource body object,
     *                                it will be overwritten by path parameter
     * @param resourceRegistryRequest request containing resources to be modified
     * @param token                   authorization token
     * @return modified resource or null along with appropriate error HTTP status code
     */
    @ApiOperation(value = "Modify resources (JSON)",
            notes = "Modify resources using basic JSON description",
            response = ResourceRegistryResponse.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns resources map in a form <internalId, Resource>", response = String.class, responseContainer = "Map"),
            @ApiResponse(code = 500, message = "Error on server side")})
    @RequestMapping(method = RequestMethod.PUT,
            value = URI_PREFIX + "/platforms/{platformId}/resources")
    public ResponseEntity modifyResource(@ApiParam(value = "ID of a platform that resources belong to", required = true) @PathVariable("platformId") String platformId,
                                         @ApiParam(value = "Request body, containing JSON description of resources to modify", required = true) @RequestBody ResourceRegistryRequest resourceRegistryRequest,
                                         @ApiParam(value = "A valid token issued by a member of the SymbIoTe Security Roaming", required = true) @RequestHeader("X-Auth-Token") String token) {
        log.debug("Request for modification of basic resources");

        CoreResourceRegistryRequest coreRequest = prepareBasicRequest(platformId, resourceRegistryRequest, token);
        return handleCoreResourceRequest(coreRequest, CoreOperationType.MODIFY);
    }

    /**
     * Endpoint for removing resource using JSON description.
     *
     * @param platformId              ID of a platform that resource belongs to
     * @param resourceRegistryRequest request containing resources to be removed
     * @param token                   authorization token
     * @return empty body with appropriate operation HTTP status code
     */
    @ApiOperation(value = "Delete resources (JSON)",
            notes = "Delete resources using basic JSON description",
            response = ResourceRegistryResponse.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns resources map in a form <internalId, Resource>", response = String.class, responseContainer = "Map"),
            @ApiResponse(code = 500, message = "Error on server side")})
    @RequestMapping(method = RequestMethod.DELETE,
            value = URI_PREFIX + "/platforms/{platformId}/resources")
    public ResponseEntity deleteResource(@ApiParam(value = "ID of a platform that resources belong to", required = true) @PathVariable("platformId") String platformId,
                                         @ApiParam(value = "Request body, containing JSON description of resources to delete", required = true) @RequestBody ResourceRegistryRequest resourceRegistryRequest,
                                         @ApiParam(value = "A valid token issued by a member of the SymbIoTe Security Roaming", required = true) @RequestHeader("X-Auth-Token") String token) {
        log.debug("Request for removal of basic resources");

        CoreResourceRegistryRequest coreRequest = prepareBasicRequest(platformId, resourceRegistryRequest, token);
        return handleCoreResourceRequest(coreRequest, CoreOperationType.DELETE);
    }

    /**
     * Endpoint for notifying Core Resource Monitor of platform status.
     *
     * @param platformId              ID of a platform
     * @param cloudMonitoringPlatform status of platform to be sent to CRM
     * @param token                   autohrization token
     * @return empty response with apropriate HTTP status code
     */
    @ApiOperation(value = "Device status update",
            notes = "Notify Core Resource Monitor of platform status",
            response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Error on server side")})
    @RequestMapping(method = RequestMethod.POST,
            value = URI_PREFIX + "/crm/Monitoring/{platformId}/devices/status")
    public ResponseEntity monitoring(@ApiParam(value = "ID of a platform that the device belongs to", required = true) @PathVariable("platformId") String platformId,
                                     @ApiParam(value = "Current status information that CRM should be notified of", required = true) @RequestBody CloudMonitoringPlatform cloudMonitoringPlatform,
                                     @ApiParam(value = "A valid token issued by a member of the SymbIoTe Security Roaming", required = true) @RequestHeader("X-Auth-Token") String token) {
        log.debug("Cloud monitoring platform received");

        boolean result = this.rabbitManager.sendMonitoringMessage(cloudMonitoringPlatform);

        if (result)
            return new ResponseEntity<>(null, HttpStatus.OK);
        else
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);

    }


}
