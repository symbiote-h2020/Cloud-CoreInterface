package eu.h2020.symbiote.controllers;

import eu.h2020.symbiote.model.Resource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Created by jawora on 21.12.16.
 */

@RestController
public class CloudCoreInterfaceController {
    private static final String URI_PREFIX = "/cloudCoreInterface/v1";

    public static Log log = LogFactory.getLog(CloudCoreInterfaceController.class);

    @RequestMapping(method = RequestMethod.GET,
            value = URI_PREFIX + "/platforms/{platformId}/rdfResources")
    public ResponseEntity<?> getRdfResources(@PathVariable("platformId") String platformId) {
        return new ResponseEntity<String>("RDF Resources listing: NYI", HttpStatus.NOT_IMPLEMENTED);
    }

    @RequestMapping(method = RequestMethod.POST,
            value = URI_PREFIX + "/platforms/{platformId}/rdfResources")
    public ResponseEntity<?> createRdfResources(@PathVariable("platformId") String platformId,
                                                     @RequestBody String rdfResources) {
        return new ResponseEntity<String>("RDF Resource create: NYI", HttpStatus.NOT_IMPLEMENTED);
    }

    @RequestMapping(method = RequestMethod.GET,
            value = URI_PREFIX + "/platforms/{platformId}/rdfResources/{resourceId}")
    public ResponseEntity<?> getRdfResource(@PathVariable("platformId") String platformId,
                                                 @PathVariable("resourceId") String resourceId) {
        return new ResponseEntity<String>("RDF Resource listing: NYI", HttpStatus.NOT_IMPLEMENTED);
    }

    @RequestMapping(method = RequestMethod.PUT,
            value = URI_PREFIX + "/platforms/{platformId}/rdfResources/{resourceId}")
    public ResponseEntity<?> modifyRdfResource(@PathVariable("platformId") String platformId,
                                                    @PathVariable("resourceId") String resourceId,
                                                    @RequestBody String rdfResources) {
        return new ResponseEntity<String>("RDF Resource modify: NYI", HttpStatus.NOT_IMPLEMENTED);
    }

    @RequestMapping(method = RequestMethod.DELETE,
            value = URI_PREFIX + "/platforms/{platformId}/rdfResources/{resourceId}")
    public ResponseEntity<?> deleteRdfResource(@PathVariable("platformId") String platformId,
                                                    @PathVariable("resourceId") String resourceId) {
        return new ResponseEntity<String>("RDF Resource delete: NYI", HttpStatus.NOT_IMPLEMENTED);
    }

    @RequestMapping(method = RequestMethod.GET,
            value = URI_PREFIX + "/platforms/{platformId}/resources")
    public ResponseEntity<?> getResources(@PathVariable("platformId") String platformId) {
        return new ResponseEntity<String>("Resources listing: NYI", HttpStatus.NOT_IMPLEMENTED);
    }

    @RequestMapping(method = RequestMethod.POST,
            value = URI_PREFIX + "/platforms/{platformId}/resources")
    public ResponseEntity<?> createResources(@PathVariable("platformId") String platformId,
                                                     @RequestBody Resource resource) {
        return new ResponseEntity<String>("Resource create: NYI", HttpStatus.NOT_IMPLEMENTED);
    }

    @RequestMapping(method = RequestMethod.GET,
            value = URI_PREFIX + "/platforms/{platformId}/resources/{resourceId}")
    public ResponseEntity<?> getResource(@PathVariable("platformId") String platformId,
                                                 @PathVariable("resourceId") String resourceId) {
        return new ResponseEntity<String>("Resource listing: NYI", HttpStatus.NOT_IMPLEMENTED);
    }

    @RequestMapping(method = RequestMethod.PUT,
            value = URI_PREFIX + "/platforms/{platformId}/resources/{resourceId}")
    public ResponseEntity<?> modifyResource(@PathVariable("platformId") String platformId,
                                                    @PathVariable("resourceId") String resourceId,
                                                    @RequestBody Resource resource) {
        return new ResponseEntity<String>("Resource modify: NYI", HttpStatus.NOT_IMPLEMENTED);
    }

    @RequestMapping(method = RequestMethod.DELETE,
            value = URI_PREFIX + "/platforms/{platformId}/resources/{resourceId}")
    public ResponseEntity<?> deleteResource(@PathVariable("platformId") String platformId,
                                                    @PathVariable("resourceId") String resourceId) {
        return new ResponseEntity<String>("Resource delete: NYI", HttpStatus.NOT_IMPLEMENTED);
    }


}
