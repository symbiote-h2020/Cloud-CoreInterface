package eu.h2020.symbiote;

import eu.h2020.symbiote.communication.RabbitManager;
import eu.h2020.symbiote.model.Resource;
import eu.h2020.symbiote.model.RpcResourceResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RabbitManagerTests {
    @Test
    public void testSendResourceCreationRequest_timeout() {
        RabbitManager rabbitManager = spy(new RabbitManager());
        doReturn(null).when(rabbitManager).sendRpcMessage(any(), any(), any());

        Resource resource = new Resource();
        RpcResourceResponse response = rabbitManager.sendResourceCreationRequest(resource);

        assertNull(response);
    }

    @Test
    public void testSendResourceCreationRequest_success() {
        String jsonResponse = "{" +
                "\"status\" : \"200\"," +
                "\"resource\" : {" +
                "\"id\" : \"testId\"" +
                "}" +
                "}";

        RabbitManager rabbitManager = spy(new RabbitManager());
        doReturn(jsonResponse).when(rabbitManager).sendRpcMessage(any(), any(), any());

        Resource resource = new Resource();
        RpcResourceResponse response = rabbitManager.sendResourceCreationRequest(resource);

        assertNotNull(response);
        assertNotNull(response.getResource());
        assertNotNull(response.getResource().getId());
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testSendResourceModificationRequest_success() {
        String jsonResponse = "{" +
                "\"status\" : \"200\"," +
                "\"resource\" : {" +
                "\"id\" : \"testId\"" +
                "}" +
                "}";

        RabbitManager rabbitManager = spy(new RabbitManager());
        doReturn(jsonResponse).when(rabbitManager).sendRpcMessage(any(), any(), any());

        Resource resource = new Resource();
        RpcResourceResponse response = rabbitManager.sendResourceModificationRequest(resource);

        assertNotNull(response);
        assertNotNull(response.getResource());
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testSendResourceRemovalRequest_success() {
        String jsonResponse = "{" +
                "\"status\" : \"200\"" +
                "}";

        RabbitManager rabbitManager = spy(new RabbitManager());
        doReturn(jsonResponse).when(rabbitManager).sendRpcMessage(any(), any(), any());

        Resource resource = new Resource();
        RpcResourceResponse response = rabbitManager.sendResourceRemovalRequest(resource);

        assertNotNull(response);
        assertNull(response.getResource());
        assertEquals(200, response.getStatus());
    }

}
