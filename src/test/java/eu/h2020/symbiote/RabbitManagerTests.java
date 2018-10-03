package eu.h2020.symbiote;

import eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringPlatformRequest;
import eu.h2020.symbiote.communication.RabbitManager;
import eu.h2020.symbiote.core.internal.*;
import eu.h2020.symbiote.core.internal.cram.NotificationMessageResponseSecured;
import eu.h2020.symbiote.core.internal.cram.NotificationMessageSecured;
import eu.h2020.symbiote.core.internal.crm.MonitoringResponseSecured;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@RunWith(MockitoJUnitRunner.class)
public class RabbitManagerTests {

    @Test
    public void testSendResourceCreationRequest_timeout() {
        RabbitManager rabbitManager = spy(new RabbitManager());
        doReturn(null).when(rabbitManager).sendRpcMessage(any(), any(), any());

        CoreResourceRegistryRequest request = new CoreResourceRegistryRequest();
        CoreResourceRegistryResponse response = rabbitManager.sendResourceCreationRequest(request);

        assertNull(response);
    }

    @Test
    public void testSendResourceCreationRequest_success() {
        String jsonResponse = "{" +
                "\"status\" : 200," +
                "\"message\" : \"success\"," +
                "\"descriptionType\" : \"BASIC\"," +
                "\"body\" : \"body\"" +
                "}";

        RabbitManager rabbitManager = spy(new RabbitManager());
        doReturn(jsonResponse).when(rabbitManager).sendRpcMessage(any(), any(), any());

        CoreResourceRegistryRequest request = new CoreResourceRegistryRequest();
        CoreResourceRegistryResponse response = rabbitManager.sendResourceCreationRequest(request);

        assertNotNull(response);
        assertEquals(200, response.getStatus());
        assertEquals(DescriptionType.BASIC, response.getDescriptionType());
        assertNotNull(response.getBody());
    }

    @Test
    public void testSendResourceModificationRequest_success() {
        String jsonResponse = "{" +
                "\"status\" : 200," +
                "\"message\" : \"success\"," +
                "\"descriptionType\" : \"BASIC\"," +
                "\"body\" : \"body\"" +
                "}";

        RabbitManager rabbitManager = spy(new RabbitManager());
        doReturn(jsonResponse).when(rabbitManager).sendRpcMessage(any(), any(), any());

        CoreResourceRegistryRequest request = new CoreResourceRegistryRequest();
        CoreResourceRegistryResponse response = rabbitManager.sendResourceModificationRequest(request);

        assertNotNull(response);
        assertEquals(200, response.getStatus());
        assertEquals(DescriptionType.BASIC, response.getDescriptionType());
        assertNotNull(response.getBody());
    }

    @Test
    public void testSendResourceRemovalRequest_success() {
        String jsonResponse = "{" +
                "\"status\" : 200," +
                "\"message\" : \"success\"," +
                "\"descriptionType\" : \"BASIC\"," +
                "\"body\" : \"body\"" +
                "}";

        RabbitManager rabbitManager = spy(new RabbitManager());
        doReturn(jsonResponse).when(rabbitManager).sendRpcMessage(any(), any(), any());

        CoreResourceRegistryRequest request = new CoreResourceRegistryRequest();
        CoreResourceRegistryResponse response = rabbitManager.sendResourceRemovalRequest(request);

        assertNotNull(response);
        assertEquals(200, response.getStatus());
        assertEquals(DescriptionType.BASIC, response.getDescriptionType());
        assertNotNull(response.getBody());
    }

    @Test
    public void testSendRpcResourceMessage_failedUnmarshalling() {
        String jsonResponse = "{" +
                "\"stat\" : \"200\"" +
                "}";

        RabbitManager rabbitManager = spy(new RabbitManager());
        doReturn(jsonResponse).when(rabbitManager).sendRpcMessage(any(), any(), any());

        CoreResourceRegistryRequest request = new CoreResourceRegistryRequest();
        CoreResourceRegistryResponse response = rabbitManager.sendResourceRemovalRequest(request);

        assertNull(response);
    }

    @Test
    public void testSendMonitoringMessage_success() {
        MonitoringResponseSecured responseSecured = new MonitoringResponseSecured(200, "OK", new Object());
        String serviceResponse = "testResponse";
        responseSecured.setServiceResponse(serviceResponse);
        RabbitManager rabbitManager = spy(new RabbitManager());
        doReturn("{\"serviceResponse\":\"testResponse\",\"status\":200}").when(rabbitManager).sendRpcMessage(any(), any(), any());

        MonitoringResponseSecured response = rabbitManager.sendMonitoringMessage(new CloudMonitoringPlatformRequest());

        assertEquals(serviceResponse, response.getServiceResponse());
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testSendMonitoringMessage_timeout() {
        RabbitManager rabbitManager = spy(new RabbitManager());
        doReturn(null).when(rabbitManager).sendRpcMessage(any(), any(), any());

        MonitoringResponseSecured response = rabbitManager.sendMonitoringMessage(new CloudMonitoringPlatformRequest());

        assertNull(response);
    }

    @Test
    public void testSendMonitoringMessage_failedUnmarshalling() {
        RabbitManager rabbitManager = spy(new RabbitManager());
        doReturn("invalid json body").when(rabbitManager).sendRpcMessage(any(), any(), any());

        MonitoringResponseSecured response = rabbitManager.sendMonitoringMessage(new CloudMonitoringPlatformRequest());

        assertNull(response);
    }

    @Test
    public void testSendAccessNotificationMessage_success() {
        RabbitManager rabbitManager = spy(new RabbitManager());
        NotificationMessageResponseSecured responseSecured = new NotificationMessageResponseSecured();
        String serviceResponse = "TestResponse";
        responseSecured.setServiceResponse(serviceResponse);
        doReturn("{\"serviceResponse\":\"TestResponse\",\"status\":200}}").when(rabbitManager).sendRpcMessage(any(), any(), any());

        NotificationMessageResponseSecured response = rabbitManager.sendAccessNotificationMessage(new NotificationMessageSecured());
        assertEquals(serviceResponse, response.getServiceResponse());
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testSendAccessNotificationMessage_timeout() {
        RabbitManager rabbitManager = spy(new RabbitManager());
        doReturn(null).when(rabbitManager).sendRpcMessage(any(), any(), any());

        NotificationMessageResponseSecured response = rabbitManager.sendAccessNotificationMessage(new NotificationMessageSecured());

        assertNull(response);
    }

    @Test
    public void testSendAccessNotificationMessage_failedUnmarshalling() {
        RabbitManager rabbitManager = spy(new RabbitManager());
        doReturn("invalid json body").when(rabbitManager).sendRpcMessage(any(), any(), any());

        NotificationMessageResponseSecured response = rabbitManager.sendAccessNotificationMessage(new NotificationMessageSecured());

        assertNull(response);
    }

    @Test
    public void testSendSdevCreationRequest_timeout() {
        RabbitManager rabbitManager = spy(new RabbitManager());
        doReturn(null).when(rabbitManager).sendRpcMessage(any(), any(), any());

        CoreSdevRegistryRequest request = new CoreSdevRegistryRequest();
        CoreSdevRegistryResponse response = rabbitManager.sendSdevCreationRequest(request);

        assertNull(response);
    }

    @Test
    public void testSendSdevCreationRequest_failedUnmarshalling() {
        RabbitManager rabbitManager = spy(new RabbitManager());
        doReturn("invalid json description").when(rabbitManager).sendRpcMessage(any(), any(), any());

        CoreSdevRegistryRequest request = new CoreSdevRegistryRequest();
        CoreSdevRegistryResponse response = rabbitManager.sendSdevCreationRequest(request);

        assertNull(response);
    }

    @Test
    public void testSendSdevCreationRequest_success() {
        RabbitManager rabbitManager = spy(new RabbitManager());
        doReturn("{}").when(rabbitManager).sendRpcMessage(any(), any(), any());

        CoreSdevRegistryRequest request = new CoreSdevRegistryRequest();
        CoreSdevRegistryResponse response = rabbitManager.sendSdevCreationRequest(request);

        assertNotNull(response);
    }

    @Test
    public void testSendSdevRemovalRequest_success() {
        RabbitManager rabbitManager = spy(new RabbitManager());
        doReturn("{}").when(rabbitManager).sendRpcMessage(any(), any(), any());

        CoreSdevRegistryRequest request = new CoreSdevRegistryRequest();
        CoreSdevRegistryResponse response = rabbitManager.sendSdevRemovalRequest(request);

        assertNotNull(response);
    }

    @Test
    public void testSendSdevModificationRequest_success() {
        RabbitManager rabbitManager = spy(new RabbitManager());
        doReturn("{}").when(rabbitManager).sendRpcMessage(any(), any(), any());

        CoreSdevRegistryRequest request = new CoreSdevRegistryRequest();
        CoreSdevRegistryResponse response = rabbitManager.sendSdevModificationRequest(request);

        assertNotNull(response);
    }

    @Test
    public void testSendSspCreationRequest_timeout() {
        RabbitManager rabbitManager = spy(new RabbitManager());
        doReturn(null).when(rabbitManager).sendRpcMessage(any(), any(), any());

        CoreSspResourceRegistryRequest request = new CoreSspResourceRegistryRequest();
        CoreSspResourceRegistryResponse response = rabbitManager.sendSspResourceCreationRequest(request);

        assertNull(response);
    }

    @Test
    public void testSendSspCreationRequest_failedUnmarshalling() {
        RabbitManager rabbitManager = spy(new RabbitManager());
        doReturn("invalid json description").when(rabbitManager).sendRpcMessage(any(), any(), any());

        CoreSspResourceRegistryRequest request = new CoreSspResourceRegistryRequest();
        CoreSspResourceRegistryResponse response = rabbitManager.sendSspResourceCreationRequest(request);

        assertNull(response);
    }

    @Test
    public void testSendSspCreationRequest_success() {
        RabbitManager rabbitManager = spy(new RabbitManager());
        doReturn("{}").when(rabbitManager).sendRpcMessage(any(), any(), any());

        CoreSspResourceRegistryRequest request = new CoreSspResourceRegistryRequest();
        CoreSspResourceRegistryResponse response = rabbitManager.sendSspResourceCreationRequest(request);

        assertNotNull(response);
    }

    @Test
    public void testSendSspRemovalRequest_success() {
        RabbitManager rabbitManager = spy(new RabbitManager());
        doReturn("{}").when(rabbitManager).sendRpcMessage(any(), any(), any());

        CoreSspResourceRegistryRequest request = new CoreSspResourceRegistryRequest();
        CoreSspResourceRegistryResponse response = rabbitManager.sendSspResourceRemovalRequest(request);

        assertNotNull(response);
    }

    @Test
    public void testSendSspModificationRequest_success() {
        RabbitManager rabbitManager = spy(new RabbitManager());
        doReturn("{}").when(rabbitManager).sendRpcMessage(any(), any(), any());

        CoreSspResourceRegistryRequest request = new CoreSspResourceRegistryRequest();
        CoreSspResourceRegistryResponse response = rabbitManager.sendSspResourceModificationRequest(request);

        assertNotNull(response);
    }

    @Test
    public void testSendClearDataRequest_timeout() {
        RabbitManager rabbitManager = spy(new RabbitManager());
        doReturn(null).when(rabbitManager).sendRpcMessage(any(), any(), any());

        ClearDataRequest request = new ClearDataRequest();
        ClearDataResponse response = rabbitManager.sendClearDataRequest(request);

        assertNull(response);
    }

    @Test
    public void testSendClearDataRequest_failedUnmarshalling() {
        RabbitManager rabbitManager = spy(new RabbitManager());
        doReturn("invalid json description").when(rabbitManager).sendRpcMessage(any(), any(), any());

        ClearDataRequest request = new ClearDataRequest();
        ClearDataResponse response = rabbitManager.sendClearDataRequest(request);

        assertNull(response);
    }

    @Test
    public void testSendClearDataRequest_success() {
        RabbitManager rabbitManager = spy(new RabbitManager());
        doReturn("{}").when(rabbitManager).sendRpcMessage(any(), any(), any());

        ClearDataRequest request = new ClearDataRequest();
        ClearDataResponse response = rabbitManager.sendClearDataRequest(request);

        assertNotNull(response);
    }

}
