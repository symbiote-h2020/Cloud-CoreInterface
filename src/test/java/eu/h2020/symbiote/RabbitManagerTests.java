package eu.h2020.symbiote;

import eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringPlatform;
import eu.h2020.symbiote.communication.RabbitManager;
import eu.h2020.symbiote.core.cci.accessNotificationMessages.NotificationMessage;
import eu.h2020.symbiote.core.internal.CoreResourceRegistryRequest;
import eu.h2020.symbiote.core.internal.CoreResourceRegistryResponse;
import eu.h2020.symbiote.core.internal.DescriptionType;
import eu.h2020.symbiote.core.internal.cram.NotificationMessageSecured;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
        RabbitManager rabbitManager = spy(new RabbitManager());
        doReturn(true).when(rabbitManager).sendAsyncMessage(any(), any(), any());

        boolean response = rabbitManager.sendMonitoringMessage(new CloudMonitoringPlatform());

        assertEquals(true, response);
    }

    @Test
    public void testSendMonitoringMessage_fail() {
        RabbitManager rabbitManager = spy(new RabbitManager());
        doReturn(false).when(rabbitManager).sendAsyncMessage(any(), any(), any());

        boolean response = rabbitManager.sendMonitoringMessage(new CloudMonitoringPlatform());

        assertEquals(false, response);
    }

    @Test
    public void testSendAccessNotificationMessage_success() {
        RabbitManager rabbitManager = spy(new RabbitManager());
        doReturn(true).when(rabbitManager).sendAsyncMessage(any(), any(), any());

        boolean response = rabbitManager.sendAccessNotificationMessage(new NotificationMessageSecured());

        assertEquals(true, response);
    }

    @Test
    public void testSendAccessNotificationMessage_fail() {
        RabbitManager rabbitManager = spy(new RabbitManager());
        doReturn(false).when(rabbitManager).sendAsyncMessage(any(), any(), any());

        boolean response = rabbitManager.sendAccessNotificationMessage(new NotificationMessageSecured());

        assertEquals(false, response);
    }

}
