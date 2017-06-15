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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
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

        CloudCoreInterfaceController controller = new CloudCoreInterfaceController(rabbitManager);
        ResponseEntity<?> response = controller.createResources(null, new ResourceRegistryRequest(), null);

        assertEquals(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void testCreateResource_badRequest() {
        CoreResourceRegistryResponse coreResourceRegistryResponse = new CoreResourceRegistryResponse();
        coreResourceRegistryResponse.setStatus(400);

        RabbitManager rabbitManager = Mockito.mock(RabbitManager.class);
        when(rabbitManager.sendResourceCreationRequest(any())).thenReturn(coreResourceRegistryResponse);

        CloudCoreInterfaceController controller = new CloudCoreInterfaceController(rabbitManager);
        ResponseEntity<?> response = controller.createResources(null, null, null);

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


        CoreResourceRegistryResponse rabbitResponse = new CoreResourceRegistryResponse();
        rabbitResponse.setStatus(200);
        rabbitResponse.setBody(
                "[" +
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
                        "\"name\": \"Location\"," +
                        "\"description\": \"This is location\"" +
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
                        "]");

        RabbitManager rabbitManager = Mockito.mock(RabbitManager.class);
        when(rabbitManager.sendResourceCreationRequest(any())).thenReturn(rabbitResponse);

        CloudCoreInterfaceController controller = new CloudCoreInterfaceController(rabbitManager);
        ResponseEntity response = controller.createResources("platformId", request, "token");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ResourceRegistryResponse);
        assertEquals("testId", ((ResourceRegistryResponse) response.getBody()).getResources().get(0).getId());
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


        CoreResourceRegistryResponse rabbitResponse = new CoreResourceRegistryResponse();
        rabbitResponse.setStatus(200);
        rabbitResponse.setBody(
                "[" +
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
                        "\"name\": \"Location\"," +
                        "\"description\": \"This is location\"" +
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
                        "]");

        RabbitManager rabbitManager = Mockito.mock(RabbitManager.class);
        when(rabbitManager.sendResourceModificationRequest(any())).thenReturn(rabbitResponse);

        CloudCoreInterfaceController controller = new CloudCoreInterfaceController(rabbitManager);
        ResponseEntity response = controller.modifyResource("platformId", request, "token");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ResourceRegistryResponse);
        assertEquals("testId", ((ResourceRegistryResponse) response.getBody()).getResources().get(0).getId());
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


        CoreResourceRegistryResponse rabbitResponse = new CoreResourceRegistryResponse();
        rabbitResponse.setStatus(200);
        rabbitResponse.setBody(
                "[" +
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
                        "\"name\": \"Location\"," +
                        "\"description\": \"This is location\"" +
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
                        "]");

        RabbitManager rabbitManager = Mockito.mock(RabbitManager.class);
        when(rabbitManager.sendResourceRemovalRequest(any())).thenReturn(rabbitResponse);

        CloudCoreInterfaceController controller = new CloudCoreInterfaceController(rabbitManager);
        ResponseEntity response = controller.deleteResource("platformId", request, "token");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ResourceRegistryResponse);
        assertEquals("testId", ((ResourceRegistryResponse) response.getBody()).getResources().get(0).getId());
    }

    @Test
    public void testCreateRdfResource_success() {
        RDFInfo rdfInfo = new RDFInfo();
        rdfInfo.setRdf("sample RDF");
        rdfInfo.setRdfFormat(RDFFormat.Turtle);

        RDFResourceRegistryRequest request = new RDFResourceRegistryRequest();
        request.setRdfInfo(rdfInfo);

        CoreResourceRegistryResponse rabbitResponse = new CoreResourceRegistryResponse();
        rabbitResponse.setStatus(200);
        rabbitResponse.setBody(
                "[" +
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
                        "\"name\": \"Location\"," +
                        "\"description\": \"This is location\"" +
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
                        "]");

        RabbitManager rabbitManager = Mockito.mock(RabbitManager.class);
        when(rabbitManager.sendResourceCreationRequest(any())).thenReturn(rabbitResponse);

        CloudCoreInterfaceController controller = new CloudCoreInterfaceController(rabbitManager);
        ResponseEntity response = controller.createRdfResources("platformId", request, "token");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ResourceRegistryResponse);
        assertEquals("testId", ((ResourceRegistryResponse) response.getBody()).getResources().get(0).getId());
    }

    @Test
    public void testModifyRdfResource_success() {
        RDFInfo rdfInfo = new RDFInfo();
        rdfInfo.setRdf("sample RDF");
        rdfInfo.setRdfFormat(RDFFormat.Turtle);

        RDFResourceRegistryRequest request = new RDFResourceRegistryRequest();
        request.setRdfInfo(rdfInfo);

        CoreResourceRegistryResponse rabbitResponse = new CoreResourceRegistryResponse();
        rabbitResponse.setStatus(200);
        rabbitResponse.setBody(
                "[" +
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
                        "\"name\": \"Location\"," +
                        "\"description\": \"This is location\"" +
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
                        "]");

        RabbitManager rabbitManager = Mockito.mock(RabbitManager.class);
        when(rabbitManager.sendResourceModificationRequest(any())).thenReturn(rabbitResponse);

        CloudCoreInterfaceController controller = new CloudCoreInterfaceController(rabbitManager);
        ResponseEntity response = controller.modifyRdfResource("platformId", request, "token");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ResourceRegistryResponse);
        assertEquals("testId", ((ResourceRegistryResponse) response.getBody()).getResources().get(0).getId());
    }

    @Test
    public void testDeleteRdfResource_success() {
        RDFInfo rdfInfo = new RDFInfo();
        rdfInfo.setRdf("sample RDF");
        rdfInfo.setRdfFormat(RDFFormat.Turtle);

        RDFResourceRegistryRequest request = new RDFResourceRegistryRequest();
        request.setRdfInfo(rdfInfo);

        CoreResourceRegistryResponse rabbitResponse = new CoreResourceRegistryResponse();
        rabbitResponse.setStatus(200);
        rabbitResponse.setBody(
                "[" +
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
                        "\"name\": \"Location\"," +
                        "\"description\": \"This is location\"" +
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
                        "]");

        RabbitManager rabbitManager = Mockito.mock(RabbitManager.class);
        when(rabbitManager.sendResourceRemovalRequest(any())).thenReturn(rabbitResponse);

        CloudCoreInterfaceController controller = new CloudCoreInterfaceController(rabbitManager);
        ResponseEntity response = controller.deleteRdfResource("platformId", request, "token");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ResourceRegistryResponse);
        assertEquals("testId", ((ResourceRegistryResponse) response.getBody()).getResources().get(0).getId());
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

        RabbitManager rabbitManager = Mockito.mock(RabbitManager.class);
        when(rabbitManager.sendMonitoringMessage(any())).thenReturn(false);

        CloudCoreInterfaceController controller = new CloudCoreInterfaceController(rabbitManager);
        ResponseEntity response = controller.monitoring("platformId", cloudMonitoringPlatform, "token");

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

        RabbitManager rabbitManager = Mockito.mock(RabbitManager.class);
        when(rabbitManager.sendMonitoringMessage(any())).thenReturn(true);

        CloudCoreInterfaceController controller = new CloudCoreInterfaceController(rabbitManager);
        ResponseEntity response = controller.monitoring("platformId", cloudMonitoringPlatform, "token");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
    }


}