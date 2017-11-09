package eu.h2020.symbiote.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringPlatform;
import eu.h2020.symbiote.communication.RabbitManager;
import eu.h2020.symbiote.core.cci.AbstractResponseSecured;
import eu.h2020.symbiote.core.cci.RDFResourceRegistryRequest;
import eu.h2020.symbiote.core.cci.ResourceRegistryRequest;
import eu.h2020.symbiote.core.cci.ResourceRegistryResponse;
import eu.h2020.symbiote.core.cci.accessNotificationMessages.NotificationMessage;
import eu.h2020.symbiote.core.internal.*;
import eu.h2020.symbiote.core.internal.cram.NotificationMessageSecured;
import eu.h2020.symbiote.model.cim.Resource;
import eu.h2020.symbiote.security.commons.SecurityConstants;
import eu.h2020.symbiote.security.commons.exceptions.custom.InvalidArgumentsException;
import eu.h2020.symbiote.security.communication.payloads.SecurityRequest;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Arrays;
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

    private CoreResourceRegistryRequest prepareRdfRequest(String platformId, RDFResourceRegistryRequest resourceRegistryRequest, SecurityRequest securityRequest) {
        CoreResourceRegistryRequest coreRequest = new CoreResourceRegistryRequest();
        coreRequest.setSecurityRequest(securityRequest);
        coreRequest.setDescriptionType(DescriptionType.RDF);
        coreRequest.setPlatformId(platformId);
        coreRequest.setFilteringPolicies(resourceRegistryRequest.getFilteringPolicies());

        try {
            ObjectMapper mapper = new ObjectMapper();

            String resourcesJson = mapper.writeValueAsString(resourceRegistryRequest);
            coreRequest.setBody(resourcesJson);

            log.debug("Request for Core Services prepared");

        } catch (JsonProcessingException e) {
            log.error("Error while handling resource creation request", e);
            return null;
        }

        return coreRequest;
    }

    private CoreResourceRegistryRequest prepareBasicRequest(String platformId, ResourceRegistryRequest resourceRegistryRequest, SecurityRequest securityRequest) {
        if (resourceRegistryRequest == null)
            return null;

        CoreResourceRegistryRequest coreRequest = new CoreResourceRegistryRequest();
        coreRequest.setSecurityRequest(securityRequest);
        coreRequest.setDescriptionType(DescriptionType.BASIC);
        coreRequest.setPlatformId(platformId);
        coreRequest.setFilteringPolicies(resourceRegistryRequest.getFilteringPolicies());

        try {
            ObjectMapper mapper = new ObjectMapper();

            String resourcesJson = mapper.writerFor(new TypeReference<Map<String, Resource>>() {
            }).writeValueAsString(resourceRegistryRequest.getBody());
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
            response.setBody(null);
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
            response.setBody(null);
            return new ResponseEntity<>(response, getHeadersForCoreResponse(coreResponse), HttpStatus.INTERNAL_SERVER_ERROR);
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
                response.setBody(null);
                return new ResponseEntity<>(response, getHeadersForCoreResponse(coreResponse), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        response.setMessage(coreResponse.getMessage());
        response.setBody(responseMapOfResources);

        log.debug("ResourceRegistryResponse created and returned to endpoint");

        return new ResponseEntity<>(response, getHeadersForCoreResponse(coreResponse), HttpStatus.valueOf(coreResponse.getStatus()));
    }

    private HttpHeaders getHeadersForCoreResponse(AbstractResponseSecured response) {
        HttpHeaders headers = new HttpHeaders();
        if (response != null && response.getServiceResponse() != null) {
            headers.put(SecurityConstants.SECURITY_RESPONSE_HEADER, Arrays.asList(response.getServiceResponse()));
        }
        return headers;
    }

    private ResponseEntity handleBadSecurityHeaders(InvalidArgumentsException e) {
        log.error("No proper security headers passed", e);
        ResourceRegistryResponse response = new ResourceRegistryResponse();
        response.setStatus(401);
        response.setMessage("Invalid security headers");
        response.setBody(null);

        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getStatus()));
    }

    /**
     * Endpoint for creating resources using RDF description.
     *
     * @param platformId              ID of a platform that resources belong to
     * @param resourceRegistryRequest request containing resources to be registered
     * @param httpHeaders             request headers
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
                                             @ApiParam(value = "Headers, containing X-Auth-Timestamp, X-Auth-Size and X-Auth-{1..n} fields", required = true) @RequestHeader HttpHeaders httpHeaders) {
        try {
            log.debug("Request for creation of RDF resources for platform " + platformId);
            if (httpHeaders == null)
                throw new InvalidArgumentsException();
            SecurityRequest securityRequest = new SecurityRequest(httpHeaders.toSingleValueMap());
            CoreResourceRegistryRequest coreRequest = prepareRdfRequest(platformId, resourceRegistryRequest, securityRequest);
            return handleCoreResourceRequest(coreRequest, CoreOperationType.CREATE);
        } catch (InvalidArgumentsException e) {
            return handleBadSecurityHeaders(e);
        }
    }

    /**
     * Endpoint for modifying resources using RDF description.
     *
     * @param platformId              ID of a platform that resources belong to
     * @param resourceRegistryRequest request containing resources to be modified
     * @param httpHeaders             request headers
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
                                            @ApiParam(value = "Headers, containing X-Auth-Timestamp, X-Auth-Size and X-Auth-{1..n} fields", required = true) @RequestHeader HttpHeaders httpHeaders) {
        try {
            log.debug("Request for modification of RDF resources for platform " + platformId);
            if (httpHeaders == null)
                throw new InvalidArgumentsException();
            SecurityRequest securityRequest = new SecurityRequest(httpHeaders.toSingleValueMap());

            CoreResourceRegistryRequest coreRequest = prepareRdfRequest(platformId, resourceRegistryRequest, securityRequest);
            return handleCoreResourceRequest(coreRequest, CoreOperationType.MODIFY);
        } catch (InvalidArgumentsException e) {
            return handleBadSecurityHeaders(e);
        }
    }

    /**
     * Endpoint for deleting resources using RDF description.
     *
     * @param platformId              ID of a platform that resources belong to
     * @param resourceRegistryRequest request containing resources to be deleted
     * @param httpHeaders             request headers
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
                                            @ApiParam(value = "Headers, containing X-Auth-Timestamp, X-Auth-Size and X-Auth-{1..n} fields", required = true) @RequestHeader HttpHeaders httpHeaders) {
        try {
            log.debug("Request for removal of RDF resources for platform " + platformId);
            if (httpHeaders == null)
                throw new InvalidArgumentsException();
            SecurityRequest securityRequest = new SecurityRequest(httpHeaders.toSingleValueMap());

            CoreResourceRegistryRequest coreRequest = prepareRdfRequest(platformId, resourceRegistryRequest, securityRequest);
            return handleCoreResourceRequest(coreRequest, CoreOperationType.DELETE);
        } catch (InvalidArgumentsException e) {
            return handleBadSecurityHeaders(e);
        }

    }

    /**
     * Endpoint for creating resources using JSON description.
     *
     * @param platformId              ID of a platform that resources belong to
     * @param resourceRegistryRequest request containing resources to be registered
     * @param httpHeaders             request headers
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
                                          @ApiParam(value = "Headers, containing X-Auth-Timestamp, X-Auth-Size and X-Auth-{1..n} fields", required = true) @RequestHeader HttpHeaders httpHeaders) {
        try {
            log.debug("Request for creation of basic resources for platform " + platformId);
            if (httpHeaders == null)
                throw new InvalidArgumentsException();
            SecurityRequest securityRequest = new SecurityRequest(httpHeaders.toSingleValueMap());

            CoreResourceRegistryRequest coreRequest = prepareBasicRequest(platformId, resourceRegistryRequest, securityRequest);
            return handleCoreResourceRequest(coreRequest, CoreOperationType.CREATE);
        } catch (InvalidArgumentsException e) {
            return handleBadSecurityHeaders(e);
        }
    }

    /**
     * Endpoint for modifying resource using JSON description.
     *
     * @param platformId              ID of a platform that resource belongs to; if platform ID is specified in Resource body object,
     *                                it will be overwritten by path parameter
     * @param resourceRegistryRequest request containing resources to be modified
     * @param httpHeaders             request headers
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
                                         @ApiParam(value = "Headers, containing X-Auth-Timestamp, X-Auth-Size and X-Auth-{1..n} fields", required = true) @RequestHeader HttpHeaders httpHeaders) {
        try {
            log.debug("Request for modification of basic resources for platform " + platformId);
            if (httpHeaders == null)
                throw new InvalidArgumentsException();
            SecurityRequest securityRequest = new SecurityRequest(httpHeaders.toSingleValueMap());

            CoreResourceRegistryRequest coreRequest = prepareBasicRequest(platformId, resourceRegistryRequest, securityRequest);
            return handleCoreResourceRequest(coreRequest, CoreOperationType.MODIFY);
        } catch (InvalidArgumentsException e) {
            return handleBadSecurityHeaders(e);
        }
    }

    /**
     * Endpoint for removing resource using JSON description.
     *
     * @param platformId              ID of a platform that resource belongs to
     * @param resourceRegistryRequest request containing resources to be removed
     * @param httpHeaders             request headers
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
                                         @ApiParam(value = "Headers, containing X-Auth-Timestamp, X-Auth-Size and X-Auth-{1..n} fields", required = true) @RequestHeader HttpHeaders httpHeaders) {
        try {
            log.debug("Request for removal of basic resources for platform " + platformId);
            if (httpHeaders == null)
                throw new InvalidArgumentsException();
            SecurityRequest securityRequest = new SecurityRequest(httpHeaders.toSingleValueMap());

            CoreResourceRegistryRequest coreRequest = prepareBasicRequest(platformId, resourceRegistryRequest, securityRequest);
            return handleCoreResourceRequest(coreRequest, CoreOperationType.DELETE);
        } catch (InvalidArgumentsException e) {
            return handleBadSecurityHeaders(e);
        }
    }

    /**
     * Endpoint for clearing all resources for specified platform.
     *
     * @param platformId              ID of a platform that resources belong to
     * @param httpHeaders             request headers
     * @return created resources (with resourceId filled) with appropriate HTTP status code
     */
    @ApiOperation(value = "Clear data",
            notes = "Clear all resources of the platform",
            response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns success message", response = String.class),
            @ApiResponse(code = 500, message = "Error on server side")})
    @RequestMapping(method = RequestMethod.POST,
            value = URI_PREFIX + "/platforms/{platformId}/clearData")
    public ResponseEntity clearData(@ApiParam(value = "ID of a platform for which resources should be cleared", required = true) @PathVariable("platformId") String platformId,
                                          @ApiParam(value = "Headers, containing X-Auth-Timestamp, X-Auth-Size and X-Auth-{1..n} fields", required = true) @RequestHeader HttpHeaders httpHeaders) {
        try {
            log.debug("Request for clear data for platform " + platformId);
            if (httpHeaders == null)
                throw new InvalidArgumentsException();
            SecurityRequest securityRequest = new SecurityRequest(httpHeaders.toSingleValueMap());

            ClearDataRequest request = new ClearDataRequest(securityRequest,platformId);
            ClearDataResponse response = rabbitManager.sendClearDataRequest(request);

            if( response == null ) {
                log.debug("Timeout on handling request by Core Services");
                response = new ClearDataResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),"Timeout on Core Services side. Operation might have been performed, but response did not arrive on time.",null);
                response.setServiceResponse(null);
            } else {
                log.debug("Clear data response: [" + response.getStatus() + "] " + response.getMessage());
            }

            return new ResponseEntity<>(response, getHeadersForCoreResponse(response), HttpStatus.valueOf(response.getStatus()));
        } catch (InvalidArgumentsException e) {
            return handleBadSecurityHeaders(e);
        }
    }

    /**
     * Endpoint for notifying Core Resource Monitor of platform status.
     *
     * @param platformId              ID of a platform
     * @param cloudMonitoringPlatform status of platform to be sent to CRM
     * @param httpHeaders             request headers
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
                                     @ApiParam(value = "Headers, containing X-Auth-Timestamp, X-Auth-Size and X-Auth-{1..n} fields", required = true) @RequestHeader HttpHeaders httpHeaders) {
        //TODO apply new token policies

        log.debug("Cloud monitoring platform received for platform " + platformId);

        boolean result = this.rabbitManager.sendMonitoringMessage(cloudMonitoringPlatform);

        if (result)
            return new ResponseEntity<>(null, HttpStatus.OK);
        else
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);

    }

    /**
     * Endpoint for passing access notifications.
     *
     * @param notificationMessage access notification message
     * @param httpHeaders         request headers
     */
    @ApiOperation(value = "Access notifications handler",
            notes = "Access notifications handler")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Notification received OK"),
            @ApiResponse(code = 500, message = "Error on server side")})
    @RequestMapping(method = RequestMethod.POST,
            value = URI_PREFIX + "/accessNotifications")
    public ResponseEntity accessNotifications(@ApiParam(value = "Request body, containing notification message", required = true) @RequestBody NotificationMessage notificationMessage,
                                              @ApiParam(value = "Headers, containing X-Auth-Timestamp, X-Auth-Size and X-Auth-{1..n} fields", required = true) @RequestHeader HttpHeaders httpHeaders) {
        try {
            log.debug("Access notification " + notificationMessage!=null? ReflectionToStringBuilder.toString(notificationMessage):"Notification is null");
            if (httpHeaders == null)
                throw new InvalidArgumentsException();
            SecurityRequest securityRequest = new SecurityRequest(httpHeaders.toSingleValueMap());

            NotificationMessageSecured notificationMessageSecured = new NotificationMessageSecured(securityRequest,notificationMessage);
            boolean result = this.rabbitManager.sendAccessNotificationMessage(notificationMessageSecured);

            if (result)
                return new ResponseEntity<>(null, HttpStatus.OK);
            else
                return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (InvalidArgumentsException e) {
            log.error("No proper security headers passed", e);
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
    }


}
