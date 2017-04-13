package eu.h2020.symbiote.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.h2020.symbiote.communication.RabbitManager;
import eu.h2020.symbiote.core.cci.RDFResourceRegistryRequest;
import eu.h2020.symbiote.core.cci.ResourceRegistryRequest;
import eu.h2020.symbiote.core.cci.ResourceRegistryResponse;
import eu.h2020.symbiote.core.internal.CoreResourceRegistryRequest;
import eu.h2020.symbiote.core.internal.CoreResourceRegistryResponse;
import eu.h2020.symbiote.core.internal.DescriptionType;
import eu.h2020.symbiote.core.model.resources.Resource;
import eu.h2020.symbiote.model.ResourceRegistryResponseWithStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * Class defining all REST endpoints.
 * <p>
 * CloudCoreInterface, as the name suggests, is just an interface, therefore it forwards all requests to modules responsible
 * for handling them via RabbitMQ.
 */
@RestController
public class CloudCoreInterfaceController {
    private static final String URI_PREFIX = "/cloudCoreInterface/v1";

    public static Log log = LogFactory.getLog(CloudCoreInterfaceController.class);

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

    private ResourceRegistryResponseWithStatus handleResourceCreationRequest(CoreResourceRegistryRequest coreResourceRegistryRequest) {
        log.debug("Sending request to Core Services: " + coreResourceRegistryRequest.getBody());
        CoreResourceRegistryResponse coreResponse = rabbitManager.sendResourceCreationRequest(coreResourceRegistryRequest);

        //Timeout or exception on our side
        if (coreResponse == null) {
            log.debug("Timeout on handling request by Core Services");
            ResourceRegistryResponseWithStatus responseWithStatus = new ResourceRegistryResponseWithStatus();
            responseWithStatus.setStatus(500);
            responseWithStatus.getResourceRegistryResponse().setMessage("Timeout on Core Services side. Resources might have been created, but response did not arrive on time.");
            return responseWithStatus;
        }

        log.debug("Response from Core Services received: " + coreResponse.getStatus() + ", " + coreResponse.getMessage() + ", " + coreResponse.getBody());

        List<Resource> responseListOfResources = null;

        if (coreResponse.getBody() != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                responseListOfResources = mapper.readValue(coreResponse.getBody(), new TypeReference<List<Resource>>() {
                });
            } catch (IOException e) {
                log.error("Error while parsing response body from Core Services", e);
                ResourceRegistryResponseWithStatus responseWithStatus = new ResourceRegistryResponseWithStatus();
                responseWithStatus.setStatus(500);
                responseWithStatus.getResourceRegistryResponse().setMessage("Error while parsing response body from Core Service. Resources might have been vreated, but response was malformed.");
                return responseWithStatus;
            }
        }

        ResourceRegistryResponse response = new ResourceRegistryResponse();
        response.setMessage(coreResponse.getMessage());
        response.setResources(responseListOfResources);

        log.debug("ResourceRegistryResponse created and returned to endpoint");

        return new ResourceRegistryResponseWithStatus(coreResponse.getStatus(), response);
    }

    /**
     * Endpoint for creating resources using RDF description.
     *
     * @param platformId              ID of a platform that resources belong to
     * @param resourceRegistryRequest request containing resources to be registered
     * @param token                   authorization token
     * @return created resources (with resourceId filled) with appropriate HTTP status code
     */
    @RequestMapping(method = RequestMethod.POST,
            value = URI_PREFIX + "/platforms/{platformId}/rdfResources")
    public ResponseEntity<?> createRdfResources(@PathVariable("platformId") String platformId,
                                                @RequestBody RDFResourceRegistryRequest resourceRegistryRequest,
                                                @RequestHeader("Authorization") String token) {
        log.debug("Request for creation of RDF resources");

        CoreResourceRegistryRequest coreRequest = new CoreResourceRegistryRequest();
        coreRequest.setToken(token);
        coreRequest.setDescriptionType(DescriptionType.RDF);
        coreRequest.setPlatformId(platformId);

        ResourceRegistryResponseWithStatus response;

        try {
            ObjectMapper mapper = new ObjectMapper();

            String resourcesJson = mapper.writeValueAsString(resourceRegistryRequest.getRdfInfo());
            coreRequest.setBody(resourcesJson);

            log.debug("Request for Core Services prepared");

            response = handleResourceCreationRequest(coreRequest);

        } catch (JsonProcessingException e) {
            log.error("Error while handling resource creation request", e);
            response = new ResourceRegistryResponseWithStatus();
            response.getResourceRegistryResponse().setMessage("Error while parsing message body. No data has been processed by Core Services.");
            response.setStatus(400);
        }

        log.debug("Returning response to client");
        return new ResponseEntity<>(response.getResourceRegistryResponse(), HttpStatus.valueOf(response.getStatus()));
    }

    /**
     * Endpoint for modifying resource using RDF description.
     * <p>
     * Currently not implemented.
     */
    @RequestMapping(method = RequestMethod.PUT,
            value = URI_PREFIX + "/platforms/{platformId}/rdfResources/{resourceId}")
    public ResponseEntity<?> modifyRdfResource(@PathVariable("platformId") String platformId,
                                               @PathVariable("resourceId") String resourceId,
                                               @RequestBody String rdfResources) {
        return new ResponseEntity<>("RDF Resource modify: NYI", HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * Endpoint for deleting resource using RDF description.
     * <p>
     * Currently not implemented.
     */
    @RequestMapping(method = RequestMethod.DELETE,
            value = URI_PREFIX + "/platforms/{platformId}/rdfResources/{resourceId}")
    public ResponseEntity<?> deleteRdfResource(@PathVariable("platformId") String platformId,
                                               @PathVariable("resourceId") String resourceId) {
        return new ResponseEntity<>("RDF Resource delete: NYI", HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * Endpoint for creating resources using JSON description.
     *
     * @param platformId              ID of a platform that resources belong to
     * @param resourceRegistryRequest request containing resources to be registered
     * @param token                   authorization token
     * @return created resources (with resourceId filled) with appropriate HTTP status code
     */
    @RequestMapping(method = RequestMethod.POST,
            value = URI_PREFIX + "/platforms/{platformId}/resources")
    public ResponseEntity<?> createResources(@PathVariable("platformId") String platformId,
                                             @RequestBody ResourceRegistryRequest resourceRegistryRequest,
                                             @RequestHeader("Authorization") String token) {
        log.debug("Request for creation of basic resources");

        CoreResourceRegistryRequest coreRequest = new CoreResourceRegistryRequest();
        coreRequest.setToken(token);
        coreRequest.setDescriptionType(DescriptionType.BASIC);
        coreRequest.setPlatformId(platformId);

        ResourceRegistryResponseWithStatus response;

        try {
            ObjectMapper mapper = new ObjectMapper();

            String resourcesJson = mapper.writerFor(new TypeReference<List<Resource>>() {
            }).writeValueAsString(resourceRegistryRequest.getResources());
            coreRequest.setBody(resourcesJson);

            log.debug("Request for Core Services prepared");

            response = handleResourceCreationRequest(coreRequest);

        } catch (JsonProcessingException e) {
            log.error("Error while handling resource creation request", e);
            response = new ResourceRegistryResponseWithStatus();
            response.getResourceRegistryResponse().setMessage("Error while parsing message body. No data has been processed by Core Services.");
            response.setStatus(400);
        }

        log.debug("Returning response to client");
        return new ResponseEntity<>(response.getResourceRegistryResponse(), HttpStatus.valueOf(response.getStatus()));
    }

    /**
     * Endpoint for modifying resource using JSON description.
     *
     * @param platformId ID of a platform that resource belongs to; if platform ID is specified in Resource body object,
     *                   it will be overwritten by path parameter
     * @param resourceId ID of a resource to modify; if resource ID is specified in Resource body object,
     *                   it will be overwritten by path parameter
     * @param resource   resource that is to be modified
     * @return modified resource or null along with appropriate error HTTP status code
     */
//    @RequestMapping(method = RequestMethod.PUT,
//            value = URI_PREFIX + "/platforms/{platformId}/resources/{resourceId}")
//    public ResponseEntity<?> modifyResource(@PathVariable("platformId") String platformId,
//                                            @PathVariable("resourceId") String resourceId,
//                                            @RequestBody Resource resource) {
//        resource.setPlatformId(platformId);
//        resource.setId(resourceId);
//        RpcResourceResponse response = rabbitManager.sendResourceModificationRequest(resource);
//
//        log.debug(response);
//
//        //Timeout or exception on our side
//        if (response == null)
//            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
//
//        return new ResponseEntity<>(response.getResource(), HttpStatus.valueOf(response.getStatus()));
//    }

    /**
     * Endpoint for removing resource using JSON description.
     *
     * @param platformId ID of a platform that resource belongs to
     * @param resourceId ID of a resource to remove
     * @return empty body with appropriate operation HTTP status code
     */
//    @RequestMapping(method = RequestMethod.DELETE,
//            value = URI_PREFIX + "/platforms/{platformId}/resources/{resourceId}")
//    public ResponseEntity<?> deleteResource(@PathVariable("platformId") String platformId,
//                                            @PathVariable("resourceId") String resourceId) {
//        Resource resource = new Resource();
//        resource.setId(resourceId);
//        resource.setPlatformId(platformId);
//        RpcResourceResponse response = rabbitManager.sendResourceRemovalRequest(resource);
//
//        log.debug(response);
//
//        //Timeout or exception on our side
//        if (response == null)
//            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
//
//        return new ResponseEntity<>(null, HttpStatus.valueOf(response.getStatus()));
//    }


}
