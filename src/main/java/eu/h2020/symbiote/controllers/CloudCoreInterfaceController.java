package eu.h2020.symbiote.controllers;

import eu.h2020.symbiote.communication.RabbitManager;
import eu.h2020.symbiote.model.Resource;
import eu.h2020.symbiote.model.RpcResourceResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    /**
     * Endpoint for creating resource using RDF description.
     * <p>
     * Currently not implemented.
     */
    @RequestMapping(method = RequestMethod.POST,
            value = URI_PREFIX + "/platforms/{platformId}/rdfResources")
    public ResponseEntity<?> createRdfResources(@PathVariable("platformId") String platformId,
                                                @RequestBody String rdfResources) {
        return new ResponseEntity<>("RDF Resource create: NYI", HttpStatus.NOT_IMPLEMENTED);
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
     * Endpoint for creating resource using JSON description.
     *
     * @param platformId ID of a platform that resource belongs to; if platform ID is specified in Resource body object,
     *                   it will be overwritten by path parameter
     * @param resource   resource that is to be registered
     * @return created resource (with resourceId filled) or null along with appropriate error HTTP status code
     */
    @RequestMapping(method = RequestMethod.POST,
            value = URI_PREFIX + "/platforms/{platformId}/resources")
    public ResponseEntity<?> createResources(@PathVariable("platformId") String platformId,
                                             @RequestBody Resource resource) {
        resource.setPlatformId(platformId);
        RpcResourceResponse response = rabbitManager.sendResourceCreationRequest(resource);

        System.out.println(response);

        //Timeout or exception on our side
        if (response == null)
            return new ResponseEntity<>("", HttpStatus.INTERNAL_SERVER_ERROR);

        if (response.getStatus() != org.apache.http.HttpStatus.SC_OK)
            return new ResponseEntity<>("{}", HttpStatus.valueOf(response.getStatus()));
        return new ResponseEntity<>(response.getResource(), HttpStatus.OK);
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
    @RequestMapping(method = RequestMethod.PUT,
            value = URI_PREFIX + "/platforms/{platformId}/resources/{resourceId}")
    public ResponseEntity<?> modifyResource(@PathVariable("platformId") String platformId,
                                            @PathVariable("resourceId") String resourceId,
                                            @RequestBody Resource resource) {
        resource.setPlatformId(platformId);
        resource.setId(resourceId);
        RpcResourceResponse response = rabbitManager.sendResourceModificationRequest(resource);

        System.out.println(response);

        //Timeout or exception on our side
        if (response == null)
            return new ResponseEntity<>("", HttpStatus.INTERNAL_SERVER_ERROR);

        if (response.getStatus() != org.apache.http.HttpStatus.SC_OK)
            return new ResponseEntity<>("{}", HttpStatus.valueOf(response.getStatus()));
        return new ResponseEntity<>(response.getResource(), HttpStatus.OK);
    }

    /**
     * Endpoint for removing resource using JSON description.
     *
     * @param platformId ID of a platform that resource belongs to
     * @param resourceId ID of a resource to remove
     * @return empty body with appropriate operation HTTP status code
     */
    @RequestMapping(method = RequestMethod.DELETE,
            value = URI_PREFIX + "/platforms/{platformId}/resources/{resourceId}")
    public ResponseEntity<?> deleteResource(@PathVariable("platformId") String platformId,
                                            @PathVariable("resourceId") String resourceId) {
        Resource resource = new Resource();
        resource.setId(resourceId);
        resource.setPlatformId(platformId);
        RpcResourceResponse response = rabbitManager.sendResourceRemovalRequest(resource);

        System.out.println(response);

        //Timeout or exception on our side
        if (response == null)
            return new ResponseEntity<>("", HttpStatus.INTERNAL_SERVER_ERROR);

        if (response.getStatus() != org.apache.http.HttpStatus.SC_OK)
            return new ResponseEntity<>("", HttpStatus.valueOf(response.getStatus()));
        return new ResponseEntity<>("", HttpStatus.OK);
    }


}
