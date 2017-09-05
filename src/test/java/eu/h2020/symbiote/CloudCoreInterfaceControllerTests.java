package eu.h2020.symbiote;

import eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringDevice;
import eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringPlatform;
import eu.h2020.symbiote.communication.RabbitManager;
import eu.h2020.symbiote.controllers.CloudCoreInterfaceController;
import eu.h2020.symbiote.core.cci.RDFResourceRegistryRequest;
import eu.h2020.symbiote.core.cci.ResourceRegistryRequest;
import eu.h2020.symbiote.core.cci.ResourceRegistryResponse;
import eu.h2020.symbiote.core.internal.CoreResourceRegistryResponse;
import eu.h2020.symbiote.core.model.RDFFormat;
import eu.h2020.symbiote.core.model.RDFInfo;
import eu.h2020.symbiote.core.model.SymbolicLocation;
import eu.h2020.symbiote.core.model.resources.FeatureOfInterest;
import eu.h2020.symbiote.core.model.resources.StationarySensor;
import eu.h2020.symbiote.security.commons.SecurityConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class CloudCoreInterfaceControllerTests {

    @Test
    public void testCreateResource_timeout() {
        RabbitManager rabbitManager = Mockito.mock(RabbitManager.class);
        when(rabbitManager.sendResourceCreationRequest(any())).thenReturn(null);

        HttpHeaders headers = new HttpHeaders();
        headers.add(SecurityConstants.SECURITY_CREDENTIALS_TIMESTAMP_HEADER, "1500000000");
        headers.add(SecurityConstants.SECURITY_CREDENTIALS_SIZE_HEADER, "1");
        headers.add(SecurityConstants.SECURITY_CREDENTIALS_HEADER_PREFIX + "1", "{\"token\":\"token\"," +
                "\"authenticationChallenge\":\"authenticationChallenge\"," +
                "\"clientCertificate\":\"clientCertificate\"," +
                "\"clientCertificateSigningAAMCertificate\":\"clientCertificateSigningAAMCertificate\"," +
                "\"foreignTokenIssuingAAMCertificate\":\"foreignTokenIssuingAAMCertificate\"}");

        CloudCoreInterfaceController controller = new CloudCoreInterfaceController(rabbitManager);
        ResponseEntity<?> response = controller.createResources(null, new ResourceRegistryRequest(), headers);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testCreateResource_nullHeaders() {
        RabbitManager rabbitManager = Mockito.mock(RabbitManager.class);

        CloudCoreInterfaceController controller = new CloudCoreInterfaceController(rabbitManager);
        ResponseEntity<?> response = controller.createResources(null, null, null);

        assertEquals(response.getStatusCode(), HttpStatus.UNAUTHORIZED);

        ResourceRegistryResponse regResponse = (ResourceRegistryResponse) response.getBody();
        assertNull(regResponse.getResources());
    }

    @Test
    public void testCreateResource_noSecurityHeaders() {
        RabbitManager rabbitManager = Mockito.mock(RabbitManager.class);

        CloudCoreInterfaceController controller = new CloudCoreInterfaceController(rabbitManager);
        ResponseEntity<?> response = controller.createResources(null, null, new HttpHeaders());

        assertEquals(response.getStatusCode(), HttpStatus.UNAUTHORIZED);

        ResourceRegistryResponse regResponse = (ResourceRegistryResponse) response.getBody();
        assertNull(regResponse.getResources());
    }

    @Test
    public void testModifyResource_nullHeaders() {
        RabbitManager rabbitManager = Mockito.mock(RabbitManager.class);

        CloudCoreInterfaceController controller = new CloudCoreInterfaceController(rabbitManager);
        ResponseEntity<?> response = controller.modifyResource(null, null, null);

        assertEquals(response.getStatusCode(), HttpStatus.UNAUTHORIZED);

        ResourceRegistryResponse regResponse = (ResourceRegistryResponse) response.getBody();
        assertNull(regResponse.getResources());
    }

    @Test
    public void testModifyResource_noSecurityHeaders() {
        RabbitManager rabbitManager = Mockito.mock(RabbitManager.class);

        CloudCoreInterfaceController controller = new CloudCoreInterfaceController(rabbitManager);
        ResponseEntity<?> response = controller.modifyResource(null, null, new HttpHeaders());

        assertEquals(response.getStatusCode(), HttpStatus.UNAUTHORIZED);

        ResourceRegistryResponse regResponse = (ResourceRegistryResponse) response.getBody();
        assertNull(regResponse.getResources());
    }

    @Test
    public void testDeleteResource_nullHeaders() {
        RabbitManager rabbitManager = Mockito.mock(RabbitManager.class);

        CloudCoreInterfaceController controller = new CloudCoreInterfaceController(rabbitManager);
        ResponseEntity<?> response = controller.deleteResource(null, null, null);

        assertEquals(response.getStatusCode(), HttpStatus.UNAUTHORIZED);

        ResourceRegistryResponse regResponse = (ResourceRegistryResponse) response.getBody();
        assertNull(regResponse.getResources());
    }

    @Test
    public void testDeleteResource_noSecurityHeaders() {
        RabbitManager rabbitManager = Mockito.mock(RabbitManager.class);

        CloudCoreInterfaceController controller = new CloudCoreInterfaceController(rabbitManager);
        ResponseEntity<?> response = controller.deleteResource(null, null, new HttpHeaders());

        assertEquals(response.getStatusCode(), HttpStatus.UNAUTHORIZED);

        ResourceRegistryResponse regResponse = (ResourceRegistryResponse) response.getBody();
        assertNull(regResponse.getResources());
    }

    @Test
    public void testCreateResource_badRequest() {
        RabbitManager rabbitManager = Mockito.mock(RabbitManager.class);

        HttpHeaders headers = new HttpHeaders();
        headers.add(SecurityConstants.SECURITY_CREDENTIALS_TIMESTAMP_HEADER, "1500000000");
        headers.add(SecurityConstants.SECURITY_CREDENTIALS_SIZE_HEADER, "1");
        headers.add(SecurityConstants.SECURITY_CREDENTIALS_HEADER_PREFIX + "1", "{\"token\":\"token\"," +
                "\"authenticationChallenge\":\"authenticationChallenge\"," +
                "\"clientCertificate\":\"clientCertificate\"," +
                "\"clientCertificateSigningAAMCertificate\":\"clientCertificateSigningAAMCertificate\"," +
                "\"foreignTokenIssuingAAMCertificate\":\"foreignTokenIssuingAAMCertificate\"}");

        CloudCoreInterfaceController controller = new CloudCoreInterfaceController(rabbitManager);
        ResponseEntity<?> response = controller.createResources(null, null, headers);

        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);

        ResourceRegistryResponse regResponse = (ResourceRegistryResponse) response.getBody();
        assertNull(regResponse.getResources());
    }

    @Test
    public void testCreateResource_success() {
        StationarySensor stationarySensor = new StationarySensor();
        stationarySensor.setLabels(Arrays.asList(new String[]{"Stationary 1"}));
        stationarySensor.setComments(Arrays.asList(new String[]{"This is stationary 1"}));
        stationarySensor.setInterworkingServiceURL("http://example.com");
        stationarySensor.setLocatedAt(new SymbolicLocation());
        stationarySensor.setFeatureOfInterest(new FeatureOfInterest());
        stationarySensor.setObservesProperty(Arrays.asList(new String[]{"Temperature", "Humidity"}));

        ResourceRegistryRequest request = new ResourceRegistryRequest();

        HttpHeaders headers = new HttpHeaders();
        headers.add(SecurityConstants.SECURITY_CREDENTIALS_TIMESTAMP_HEADER, "1500000000");
        headers.add(SecurityConstants.SECURITY_CREDENTIALS_SIZE_HEADER, "1");
        headers.add(SecurityConstants.SECURITY_CREDENTIALS_HEADER_PREFIX + "1", "{\"token\":\"token\"," +
                "\"authenticationChallenge\":\"authenticationChallenge\"," +
                "\"clientCertificate\":\"clientCertificate\"," +
                "\"clientCertificateSigningAAMCertificate\":\"clientCertificateSigningAAMCertificate\"," +
                "\"foreignTokenIssuingAAMCertificate\":\"foreignTokenIssuingAAMCertificate\"}");

        CoreResourceRegistryResponse rabbitResponse = new CoreResourceRegistryResponse();
        rabbitResponse.setStatus(200);
        rabbitResponse.setBody(
                "{\"id1\":" +
                        "{" +
                        "\"@c\":\".StationarySensor\"," +
                        "\"labels\":[\"Stationary 1\"]," +
                        "\"id\":\"testId\"," +
                        "\"comments\":[\"This is stationary 1\"]," +
                        "\"interworkingServiceURL\":\"http://example.com\"," +
                        "\"locatedAt\": {" +
                        "\"@c\": \".WGS84Location\"," +
                        "\"longitude\": 2.345," +
                        "\"latitude\": 15.1617," +
                        "\"altitude\": 100," +
                        "\"name\": [\"Location\"]," +
                        "\"description\": [\"This is location\"]" +
                        "}," +
                        "\"featureOfInterest\": {" +
                        "\"labels\": [" +
                        "\"Room1\"" +
                        "]," +
                        "\"comments\": [" +
                        "\"This is room 1\"" +
                        "]," +
                        "\"hasProperty\": [" +
                        "\"temperature\"" +
                        "]" +
                        "}," +
                        "\"observesProperty\": [" +
                        "\"Temperature\"," +
                        "\"Humidity\"" +
                        "]" +
                        "}" +
                        "}");

        RabbitManager rabbitManager = Mockito.mock(RabbitManager.class);
        when(rabbitManager.sendResourceCreationRequest(any())).thenReturn(rabbitResponse);

        CloudCoreInterfaceController controller = new CloudCoreInterfaceController(rabbitManager);
        ResponseEntity response = controller.createResources("platformId", request, headers);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ResourceRegistryResponse);
        assertEquals("testId", ((ResourceRegistryResponse) response.getBody()).getResources().get("id1").getId());
    }

    @Test
    public void testMalformedResponseFromCore() {
        StationarySensor stationarySensor = new StationarySensor();
        stationarySensor.setLabels(Arrays.asList(new String[]{"Stationary 1"}));
        stationarySensor.setComments(Arrays.asList(new String[]{"This is stationary 1"}));
        stationarySensor.setInterworkingServiceURL("http://example.com");
        stationarySensor.setLocatedAt(new SymbolicLocation());
        stationarySensor.setFeatureOfInterest(new FeatureOfInterest());
        stationarySensor.setObservesProperty(Arrays.asList(new String[]{"Temperature", "Humidity"}));

        ResourceRegistryRequest request = new ResourceRegistryRequest();

        HttpHeaders headers = new HttpHeaders();
        headers.add(SecurityConstants.SECURITY_CREDENTIALS_TIMESTAMP_HEADER, "1500000000");
        headers.add(SecurityConstants.SECURITY_CREDENTIALS_SIZE_HEADER, "1");
        headers.add(SecurityConstants.SECURITY_CREDENTIALS_HEADER_PREFIX + "1", "{\"token\":\"token\"," +
                "\"authenticationChallenge\":\"authenticationChallenge\"," +
                "\"clientCertificate\":\"clientCertificate\"," +
                "\"clientCertificateSigningAAMCertificate\":\"clientCertificateSigningAAMCertificate\"," +
                "\"foreignTokenIssuingAAMCertificate\":\"foreignTokenIssuingAAMCertificate\"}");

        CoreResourceRegistryResponse rabbitResponse = new CoreResourceRegistryResponse();
        rabbitResponse.setStatus(200);
        rabbitResponse.setBody("<Malformed response body>");

        RabbitManager rabbitManager = Mockito.mock(RabbitManager.class);
        when(rabbitManager.sendResourceCreationRequest(any())).thenReturn(rabbitResponse);

        CloudCoreInterfaceController controller = new CloudCoreInterfaceController(rabbitManager);
        ResponseEntity response = controller.createResources("platformId", request, headers);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ResourceRegistryResponse);
    }

    @Test
    public void testModifyResource_success() {
        StationarySensor stationarySensor = new StationarySensor();
        stationarySensor.setId("testId");
        stationarySensor.setLabels(Arrays.asList(new String[]{"Stationary 1"}));
        stationarySensor.setComments(Arrays.asList(new String[]{"This is stationary 1"}));
        stationarySensor.setInterworkingServiceURL("http://example.com");
        stationarySensor.setLocatedAt(new SymbolicLocation());
        stationarySensor.setFeatureOfInterest(new FeatureOfInterest());
        stationarySensor.setObservesProperty(Arrays.asList(new String[]{"Temperature", "Humidity"}));

        ResourceRegistryRequest request = new ResourceRegistryRequest();

        HttpHeaders headers = new HttpHeaders();
        headers.add(SecurityConstants.SECURITY_CREDENTIALS_TIMESTAMP_HEADER, "1500000000");
        headers.add(SecurityConstants.SECURITY_CREDENTIALS_SIZE_HEADER, "1");
        headers.add(SecurityConstants.SECURITY_CREDENTIALS_HEADER_PREFIX + "1", "{\"token\":\"token\"," +
                "\"authenticationChallenge\":\"authenticationChallenge\"," +
                "\"clientCertificate\":\"clientCertificate\"," +
                "\"clientCertificateSigningAAMCertificate\":\"clientCertificateSigningAAMCertificate\"," +
                "\"foreignTokenIssuingAAMCertificate\":\"foreignTokenIssuingAAMCertificate\"}");



        CoreResourceRegistryResponse rabbitResponse = new CoreResourceRegistryResponse();
        rabbitResponse.setStatus(200);
        rabbitResponse.setBody(
                "{\"id1\":" +
                        "{" +
                        "\"@c\":\".StationarySensor\"," +
                        "\"labels\":[\"Stationary 1\"]," +
                        "\"id\":\"testId\"," +
                        "\"comments\":[\"This is stationary 1\"]," +
                        "\"interworkingServiceURL\":\"http://example.com\"," +
                        "\"locatedAt\": {" +
                        "\"@c\": \".WGS84Location\"," +
                        "\"longitude\": 2.345," +
                        "\"latitude\": 15.1617," +
                        "\"altitude\": 100," +
                        "\"name\": [\"Location\"]," +
                        "\"description\": [\"This is location\"]" +
                        "}," +
                        "\"featureOfInterest\": {" +
                        "\"labels\": [" +
                        "\"Room1\"" +
                        "]," +
                        "\"comments\": [" +
                        "\"This is room 1\"" +
                        "]," +
                        "\"hasProperty\": [" +
                        "\"temperature\"" +
                        "]" +
                        "}," +
                        "\"observesProperty\": [" +
                        "\"Temperature\"," +
                        "\"Humidity\"" +
                        "]" +
                        "}" +
                        "}");

        RabbitManager rabbitManager = Mockito.mock(RabbitManager.class);
        when(rabbitManager.sendResourceModificationRequest(any())).thenReturn(rabbitResponse);

        CloudCoreInterfaceController controller = new CloudCoreInterfaceController(rabbitManager);
        ResponseEntity response = controller.modifyResource("platformId", request, headers);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ResourceRegistryResponse);
        assertEquals("testId", ((ResourceRegistryResponse) response.getBody()).getResources().get("id1").getId());
    }

    @Test
    public void testDeleteResource_success() {
        StationarySensor stationarySensor = new StationarySensor();
        stationarySensor.setId("testId");
        stationarySensor.setLabels(Arrays.asList(new String[]{"Stationary 1"}));
        stationarySensor.setComments(Arrays.asList(new String[]{"This is stationary 1"}));
        stationarySensor.setInterworkingServiceURL("http://example.com");
        stationarySensor.setLocatedAt(new SymbolicLocation());
        stationarySensor.setFeatureOfInterest(new FeatureOfInterest());
        stationarySensor.setObservesProperty(Arrays.asList(new String[]{"Temperature", "Humidity"}));

        ResourceRegistryRequest request = new ResourceRegistryRequest();

        HttpHeaders headers = new HttpHeaders();
        headers.add(SecurityConstants.SECURITY_CREDENTIALS_TIMESTAMP_HEADER, "1500000000");
        headers.add(SecurityConstants.SECURITY_CREDENTIALS_SIZE_HEADER, "1");
        headers.add(SecurityConstants.SECURITY_CREDENTIALS_HEADER_PREFIX + "1", "{\"token\":\"token\"," +
                "\"authenticationChallenge\":\"authenticationChallenge\"," +
                "\"clientCertificate\":\"clientCertificate\"," +
                "\"clientCertificateSigningAAMCertificate\":\"clientCertificateSigningAAMCertificate\"," +
                "\"foreignTokenIssuingAAMCertificate\":\"foreignTokenIssuingAAMCertificate\"}");

        CoreResourceRegistryResponse rabbitResponse = new CoreResourceRegistryResponse();
        rabbitResponse.setStatus(200);
        rabbitResponse.setBody(
                "{\"id1\":" +
                        "{" +
                        "\"@c\":\".StationarySensor\"," +
                        "\"labels\":[\"Stationary 1\"]," +
                        "\"id\":\"testId\"," +
                        "\"comments\":[\"This is stationary 1\"]," +
                        "\"interworkingServiceURL\":\"http://example.com\"," +
                        "\"locatedAt\": {" +
                        "\"@c\": \".WGS84Location\"," +
                        "\"longitude\": 2.345," +
                        "\"latitude\": 15.1617," +
                        "\"altitude\": 100," +
                        "\"name\": [\"Location\"]," +
                        "\"description\": [\"This is location\"]" +
                        "}," +
                        "\"featureOfInterest\": {" +
                        "\"labels\": [" +
                        "\"Room1\"" +
                        "]," +
                        "\"comments\": [" +
                        "\"This is room 1\"" +
                        "]," +
                        "\"hasProperty\": [" +
                        "\"temperature\"" +
                        "]" +
                        "}," +
                        "\"observesProperty\": [" +
                        "\"Temperature\"," +
                        "\"Humidity\"" +
                        "]" +
                        "}" +
                        "}");

        RabbitManager rabbitManager = Mockito.mock(RabbitManager.class);
        when(rabbitManager.sendResourceRemovalRequest(any())).thenReturn(rabbitResponse);

        CloudCoreInterfaceController controller = new CloudCoreInterfaceController(rabbitManager);
        ResponseEntity response = controller.deleteResource("platformId", request, headers);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ResourceRegistryResponse);
        assertEquals("testId", ((ResourceRegistryResponse) response.getBody()).getResources().get("id1").getId());
    }

    @Test
    public void testCreateRdfResource_nullHeaders() {
        RabbitManager rabbitManager = Mockito.mock(RabbitManager.class);

        CloudCoreInterfaceController controller = new CloudCoreInterfaceController(rabbitManager);
        ResponseEntity<?> response = controller.createRdfResources(null, null, null);

        assertEquals(response.getStatusCode(), HttpStatus.UNAUTHORIZED);

        ResourceRegistryResponse regResponse = (ResourceRegistryResponse) response.getBody();
        assertNull(regResponse.getResources());
    }

    @Test
    public void testCreateRdfResource_noSecurityHeaders() {
        RabbitManager rabbitManager = Mockito.mock(RabbitManager.class);

        CloudCoreInterfaceController controller = new CloudCoreInterfaceController(rabbitManager);
        ResponseEntity<?> response = controller.createRdfResources(null, null, new HttpHeaders());

        assertEquals(response.getStatusCode(), HttpStatus.UNAUTHORIZED);

        ResourceRegistryResponse regResponse = (ResourceRegistryResponse) response.getBody();
        assertNull(regResponse.getResources());
    }

    @Test
    public void testCreateRdfResource_success() {
        RDFInfo rdfInfo = new RDFInfo();
        rdfInfo.setRdf("sample RDF");
        rdfInfo.setRdfFormat(RDFFormat.Turtle);

        RDFResourceRegistryRequest request = new RDFResourceRegistryRequest();
        request.setRdfInfo(rdfInfo);

        HttpHeaders headers = new HttpHeaders();
        headers.add(SecurityConstants.SECURITY_CREDENTIALS_TIMESTAMP_HEADER, "1500000000");
        headers.add(SecurityConstants.SECURITY_CREDENTIALS_SIZE_HEADER, "1");
        headers.add(SecurityConstants.SECURITY_CREDENTIALS_HEADER_PREFIX + "1", "{\"token\":\"token\"," +
                "\"authenticationChallenge\":\"authenticationChallenge\"," +
                "\"clientCertificate\":\"clientCertificate\"," +
                "\"clientCertificateSigningAAMCertificate\":\"clientCertificateSigningAAMCertificate\"," +
                "\"foreignTokenIssuingAAMCertificate\":\"foreignTokenIssuingAAMCertificate\"}");

        CoreResourceRegistryResponse rabbitResponse = new CoreResourceRegistryResponse();
        rabbitResponse.setStatus(200);
        rabbitResponse.setBody(
                "{\"id1\":" +
                        "{" +
                        "\"@c\":\".StationarySensor\"," +
                        "\"labels\":[\"Stationary 1\"]," +
                        "\"id\":\"testId\"," +
                        "\"comments\":[\"This is stationary 1\"]," +
                        "\"interworkingServiceURL\":\"http://example.com\"," +
                        "\"locatedAt\": {" +
                        "\"@c\": \".WGS84Location\"," +
                        "\"longitude\": 2.345," +
                        "\"latitude\": 15.1617," +
                        "\"altitude\": 100," +
                        "\"name\": [\"Location\"]," +
                        "\"description\": [\"This is location\"]" +
                        "}," +
                        "\"featureOfInterest\": {" +
                        "\"labels\": [" +
                        "\"Room1\"" +
                        "]," +
                        "\"comments\": [" +
                        "\"This is room 1\"" +
                        "]," +
                        "\"hasProperty\": [" +
                        "\"temperature\"" +
                        "]" +
                        "}," +
                        "\"observesProperty\": [" +
                        "\"Temperature\"," +
                        "\"Humidity\"" +
                        "]" +
                        "}" +
                        "}");

        RabbitManager rabbitManager = Mockito.mock(RabbitManager.class);
        when(rabbitManager.sendResourceCreationRequest(any())).thenReturn(rabbitResponse);

        CloudCoreInterfaceController controller = new CloudCoreInterfaceController(rabbitManager);
        ResponseEntity response = controller.createRdfResources("platformId", request, headers);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ResourceRegistryResponse);
        assertEquals("testId", ((ResourceRegistryResponse) response.getBody()).getResources().get("id1").getId());
    }

    @Test
    public void testModifyRdfResource_nullHeaders() {
        RabbitManager rabbitManager = Mockito.mock(RabbitManager.class);

        CloudCoreInterfaceController controller = new CloudCoreInterfaceController(rabbitManager);
        ResponseEntity<?> response = controller.modifyRdfResource(null, null, null);

        assertEquals(response.getStatusCode(), HttpStatus.UNAUTHORIZED);

        ResourceRegistryResponse regResponse = (ResourceRegistryResponse) response.getBody();
        assertNull(regResponse.getResources());
    }

    @Test
    public void testModifyRdfResource_noSecurityHeaders() {
        RabbitManager rabbitManager = Mockito.mock(RabbitManager.class);

        CloudCoreInterfaceController controller = new CloudCoreInterfaceController(rabbitManager);
        ResponseEntity<?> response = controller.modifyRdfResource(null, null, new HttpHeaders());

        assertEquals(response.getStatusCode(), HttpStatus.UNAUTHORIZED);

        ResourceRegistryResponse regResponse = (ResourceRegistryResponse) response.getBody();
        assertNull(regResponse.getResources());
    }

    @Test
    public void testModifyRdfResource_success() {
        RDFInfo rdfInfo = new RDFInfo();
        rdfInfo.setRdf("sample RDF");
        rdfInfo.setRdfFormat(RDFFormat.Turtle);

        RDFResourceRegistryRequest request = new RDFResourceRegistryRequest();
        request.setRdfInfo(rdfInfo);

        HttpHeaders headers = new HttpHeaders();
        headers.add(SecurityConstants.SECURITY_CREDENTIALS_TIMESTAMP_HEADER, "1500000000");
        headers.add(SecurityConstants.SECURITY_CREDENTIALS_SIZE_HEADER, "1");
        headers.add(SecurityConstants.SECURITY_CREDENTIALS_HEADER_PREFIX + "1", "{\"token\":\"token\"," +
                "\"authenticationChallenge\":\"authenticationChallenge\"," +
                "\"clientCertificate\":\"clientCertificate\"," +
                "\"clientCertificateSigningAAMCertificate\":\"clientCertificateSigningAAMCertificate\"," +
                "\"foreignTokenIssuingAAMCertificate\":\"foreignTokenIssuingAAMCertificate\"}");

        CoreResourceRegistryResponse rabbitResponse = new CoreResourceRegistryResponse();
        rabbitResponse.setStatus(200);
        rabbitResponse.setBody(
                "{\"id1\":" +
                        "{" +
                        "\"@c\":\".StationarySensor\"," +
                        "\"labels\":[\"Stationary 1\"]," +
                        "\"id\":\"testId\"," +
                        "\"comments\":[\"This is stationary 1\"]," +
                        "\"interworkingServiceURL\":\"http://example.com\"," +
                        "\"locatedAt\": {" +
                        "\"@c\": \".WGS84Location\"," +
                        "\"longitude\": 2.345," +
                        "\"latitude\": 15.1617," +
                        "\"altitude\": 100," +
                        "\"name\": [\"Location\"]," +
                        "\"description\": [\"This is location\"]" +
                        "}," +
                        "\"featureOfInterest\": {" +
                        "\"labels\": [" +
                        "\"Room1\"" +
                        "]," +
                        "\"comments\": [" +
                        "\"This is room 1\"" +
                        "]," +
                        "\"hasProperty\": [" +
                        "\"temperature\"" +
                        "]" +
                        "}," +
                        "\"observesProperty\": [" +
                        "\"Temperature\"," +
                        "\"Humidity\"" +
                        "]" +
                        "}" +
                        "}");

        RabbitManager rabbitManager = Mockito.mock(RabbitManager.class);
        when(rabbitManager.sendResourceModificationRequest(any())).thenReturn(rabbitResponse);

        CloudCoreInterfaceController controller = new CloudCoreInterfaceController(rabbitManager);
        ResponseEntity response = controller.modifyRdfResource("platformId", request, headers);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ResourceRegistryResponse);
        assertEquals("testId", ((ResourceRegistryResponse) response.getBody()).getResources().get("id1").getId());
    }

    @Test
    public void testDeleteRdfResource_nullHeaders() {
        RabbitManager rabbitManager = Mockito.mock(RabbitManager.class);

        CloudCoreInterfaceController controller = new CloudCoreInterfaceController(rabbitManager);
        ResponseEntity<?> response = controller.deleteRdfResource(null, null, null);

        assertEquals(response.getStatusCode(), HttpStatus.UNAUTHORIZED);

        ResourceRegistryResponse regResponse = (ResourceRegistryResponse) response.getBody();
        assertNull(regResponse.getResources());
    }

    @Test
    public void testDeleteRdfResource_noSecurityHeaders() {
        RabbitManager rabbitManager = Mockito.mock(RabbitManager.class);

        CloudCoreInterfaceController controller = new CloudCoreInterfaceController(rabbitManager);
        ResponseEntity<?> response = controller.deleteRdfResource(null, null, new HttpHeaders());

        assertEquals(response.getStatusCode(), HttpStatus.UNAUTHORIZED);

        ResourceRegistryResponse regResponse = (ResourceRegistryResponse) response.getBody();
        assertNull(regResponse.getResources());
    }

    @Test
    public void testDeleteRdfResource_success() {
        RDFInfo rdfInfo = new RDFInfo();
        rdfInfo.setRdf("sample RDF");
        rdfInfo.setRdfFormat(RDFFormat.Turtle);

        RDFResourceRegistryRequest request = new RDFResourceRegistryRequest();
        request.setRdfInfo(rdfInfo);

        HttpHeaders headers = new HttpHeaders();
        headers.add(SecurityConstants.SECURITY_CREDENTIALS_TIMESTAMP_HEADER, "1500000000");
        headers.add(SecurityConstants.SECURITY_CREDENTIALS_SIZE_HEADER, "1");
        headers.add(SecurityConstants.SECURITY_CREDENTIALS_HEADER_PREFIX + "1", "{\"token\":\"token\"," +
                "\"authenticationChallenge\":\"authenticationChallenge\"," +
                "\"clientCertificate\":\"clientCertificate\"," +
                "\"clientCertificateSigningAAMCertificate\":\"clientCertificateSigningAAMCertificate\"," +
                "\"foreignTokenIssuingAAMCertificate\":\"foreignTokenIssuingAAMCertificate\"}");

        CoreResourceRegistryResponse rabbitResponse = new CoreResourceRegistryResponse();
        rabbitResponse.setStatus(200);
        rabbitResponse.setBody(
                "{\"id1\":" +
                        "{" +
                        "\"@c\":\".StationarySensor\"," +
                        "\"labels\":[\"Stationary 1\"]," +
                        "\"id\":\"testId\"," +
                        "\"comments\":[\"This is stationary 1\"]," +
                        "\"interworkingServiceURL\":\"http://example.com\"," +
                        "\"locatedAt\": {" +
                        "\"@c\": \".WGS84Location\"," +
                        "\"longitude\": 2.345," +
                        "\"latitude\": 15.1617," +
                        "\"altitude\": 100," +
                        "\"name\": [\"Location\"]," +
                        "\"description\": [\"This is location\"]" +
                        "}," +
                        "\"featureOfInterest\": {" +
                        "\"labels\": [" +
                        "\"Room1\"" +
                        "]," +
                        "\"comments\": [" +
                        "\"This is room 1\"" +
                        "]," +
                        "\"hasProperty\": [" +
                        "\"temperature\"" +
                        "]" +
                        "}," +
                        "\"observesProperty\": [" +
                        "\"Temperature\"," +
                        "\"Humidity\"" +
                        "]" +
                        "}" +
                        "}");

        RabbitManager rabbitManager = Mockito.mock(RabbitManager.class);
        when(rabbitManager.sendResourceRemovalRequest(any())).thenReturn(rabbitResponse);

        CloudCoreInterfaceController controller = new CloudCoreInterfaceController(rabbitManager);
        ResponseEntity response = controller.deleteRdfResource("platformId", request, headers);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ResourceRegistryResponse);
        assertEquals("testId", ((ResourceRegistryResponse) response.getBody()).getResources().get("id1").getId());
    }

    @Test
    public void testMonitoring_fail() {
        CloudMonitoringDevice cloudMonitoringDevice = new CloudMonitoringDevice();
        cloudMonitoringDevice.setId("deviceId");
        cloudMonitoringDevice.setAvailability(1);
        cloudMonitoringDevice.setLoad(5);
        cloudMonitoringDevice.setTimestamp("timestamp");

        CloudMonitoringPlatform cloudMonitoringPlatform = new CloudMonitoringPlatform();
        cloudMonitoringPlatform.setInternalId("platformId");
        cloudMonitoringPlatform.setDevices(new CloudMonitoringDevice[]{cloudMonitoringDevice});

        HttpHeaders headers = new HttpHeaders();
        headers.add(SecurityConstants.SECURITY_CREDENTIALS_TIMESTAMP_HEADER, "1500000000");
        headers.add(SecurityConstants.SECURITY_CREDENTIALS_SIZE_HEADER, "1");
        headers.add(SecurityConstants.SECURITY_CREDENTIALS_HEADER_PREFIX + "1", "{\"token\":\"token\"," +
                "\"authenticationChallenge\":\"authenticationChallenge\"," +
                "\"clientCertificate\":\"clientCertificate\"," +
                "\"clientCertificateSigningAAMCertificate\":\"clientCertificateSigningAAMCertificate\"," +
                "\"foreignTokenIssuingAAMCertificate\":\"foreignTokenIssuingAAMCertificate\"}");

        RabbitManager rabbitManager = Mockito.mock(RabbitManager.class);
        when(rabbitManager.sendMonitoringMessage(any())).thenReturn(false);

        CloudCoreInterfaceController controller = new CloudCoreInterfaceController(rabbitManager);
        ResponseEntity response = controller.monitoring("platformId", cloudMonitoringPlatform, headers);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void testMonitoring_success() {
        CloudMonitoringDevice cloudMonitoringDevice = new CloudMonitoringDevice();
        cloudMonitoringDevice.setId("deviceId");
        cloudMonitoringDevice.setAvailability(1);
        cloudMonitoringDevice.setLoad(5);
        cloudMonitoringDevice.setTimestamp("timestamp");

        CloudMonitoringPlatform cloudMonitoringPlatform = new CloudMonitoringPlatform();
        cloudMonitoringPlatform.setInternalId("platformId");
        cloudMonitoringPlatform.setDevices(new CloudMonitoringDevice[]{cloudMonitoringDevice});

        HttpHeaders headers = new HttpHeaders();
        headers.add(SecurityConstants.SECURITY_CREDENTIALS_TIMESTAMP_HEADER, "1500000000");
        headers.add(SecurityConstants.SECURITY_CREDENTIALS_SIZE_HEADER, "1");
        headers.add(SecurityConstants.SECURITY_CREDENTIALS_HEADER_PREFIX + "1", "{\"token\":\"token\"," +
                "\"authenticationChallenge\":\"authenticationChallenge\"," +
                "\"clientCertificate\":\"clientCertificate\"," +
                "\"clientCertificateSigningAAMCertificate\":\"clientCertificateSigningAAMCertificate\"," +
                "\"foreignTokenIssuingAAMCertificate\":\"foreignTokenIssuingAAMCertificate\"}");

        RabbitManager rabbitManager = Mockito.mock(RabbitManager.class);
        when(rabbitManager.sendMonitoringMessage(any())).thenReturn(true);

        CloudCoreInterfaceController controller = new CloudCoreInterfaceController(rabbitManager);
        ResponseEntity response = controller.monitoring("platformId", cloudMonitoringPlatform, headers);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
    }


}