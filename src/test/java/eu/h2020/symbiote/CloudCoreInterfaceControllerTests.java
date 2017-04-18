package eu.h2020.symbiote;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CloudCoreInterfaceControllerTests {

    @Test
    public void testCreateResourceRdf() {
//        RabbitManager rabbitManager = Mockito.mock(RabbitManager.class);
//        CloudCoreInterfaceController controller = new CloudCoreInterfaceController(rabbitManager);
//
//        ResponseEntity response = controller.createRdfResources(null, null, null);
//
//        assertEquals(response.getStatusCode(), HttpStatus.NOT_IMPLEMENTED);
    }

    @Test
    public void testModifyResourceRdf() {
//        RabbitManager rabbitManager = Mockito.mock(RabbitManager.class);
//        CloudCoreInterfaceController controller = new CloudCoreInterfaceController(rabbitManager);
//
//        ResponseEntity response = controller.modifyRdfResource(null, null, null);
//
//        assertEquals(response.getStatusCode(), HttpStatus.NOT_IMPLEMENTED);
    }

    @Test
    public void testDeleteResourceRdf() {
//        RabbitManager rabbitManager = Mockito.mock(RabbitManager.class);
//        CloudCoreInterfaceController controller = new CloudCoreInterfaceController(rabbitManager);
//
//        ResponseEntity response = controller.deleteRdfResource(null, null, null);
//
//        assertEquals(response.getStatusCode(), HttpStatus.NOT_IMPLEMENTED);
    }

    @Test
    public void testCreateResource_timeout(){
//        RabbitManager rabbitManager = Mockito.mock(RabbitManager.class);
//        when(rabbitManager.sendResourceCreationRequest(any())).thenReturn(null);
//
//        CloudCoreInterfaceController controller = new CloudCoreInterfaceController(rabbitManager);
//        ResponseEntity<?> response = controller.createResources(null, new Resource());
//
//        assertEquals(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void testCreateResource_badRequest(){
//        RpcResourceResponse rabbitResponse = new RpcResourceResponse();
//        rabbitResponse.setStatus(400);
//
//        RabbitManager rabbitManager = Mockito.mock(RabbitManager.class);
//        when(rabbitManager.sendResourceCreationRequest(any())).thenReturn(rabbitResponse);
//
//        CloudCoreInterfaceController controller = new CloudCoreInterfaceController(rabbitManager);
//        ResponseEntity<?> response = controller.createResources(null, new Resource());
//
//        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
//        assertNull(response.getBody());
    }

    @Test
    public void testCreateResource_success(){
//        Resource resource = new Resource();
//        resource.setId("testId");
//
//        RpcResourceResponse rabbitResponse = new RpcResourceResponse();
//        rabbitResponse.setStatus(200);
//        rabbitResponse.setResource(resource);
//
//
//        RabbitManager rabbitManager = Mockito.mock(RabbitManager.class);
//        when(rabbitManager.sendResourceCreationRequest(any())).thenReturn(rabbitResponse);
//
//        CloudCoreInterfaceController controller = new CloudCoreInterfaceController(rabbitManager);
//        ResponseEntity<?> response = controller.createResources(null, new Resource());
//
//        assertEquals(response.getStatusCode(), HttpStatus.OK);
//        assertNotNull(response.getBody());
//        assertTrue(response.getBody() instanceof Resource);
//        assertEquals("testId", ((Resource) response.getBody()).getId());
    }

    @Test
    public void testModifyResource_timeout(){
//        RabbitManager rabbitManager = Mockito.mock(RabbitManager.class);
//        when(rabbitManager.sendResourceModificationRequest(any())).thenReturn(null);
//
//        Resource resource = new Resource();
//
//        CloudCoreInterfaceController controller = new CloudCoreInterfaceController(rabbitManager);
//        ResponseEntity<?> response = controller.modifyResource(null, null, resource);
//
//        assertEquals(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void testModifyResource_badRequest(){
//        RpcResourceResponse rabbitResponse = new RpcResourceResponse();
//        rabbitResponse.setStatus(400);
//
//        RabbitManager rabbitManager = Mockito.mock(RabbitManager.class);
//        when(rabbitManager.sendResourceModificationRequest(any())).thenReturn(rabbitResponse);
//
//        Resource resource = new Resource();
//
//        CloudCoreInterfaceController controller = new CloudCoreInterfaceController(rabbitManager);
//        ResponseEntity<?> response = controller.modifyResource(null, null, resource);
//
//        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
//        assertNull(response.getBody());
    }

    @Test
    public void testModifyResource_success(){
//        Resource resource = new Resource();
//        resource.setId("testId");
//
//        RpcResourceResponse rabbitResponse = new RpcResourceResponse();
//        rabbitResponse.setStatus(200);
//        rabbitResponse.setResource(resource);
//
//        RabbitManager rabbitManager = Mockito.mock(RabbitManager.class);
//        when(rabbitManager.sendResourceModificationRequest(any())).thenReturn(rabbitResponse);
//
//        CloudCoreInterfaceController controller = new CloudCoreInterfaceController(rabbitManager);
//        ResponseEntity<?> response = controller.modifyResource(null, null, new Resource());
//
//        assertEquals(response.getStatusCode(), HttpStatus.OK);
//        assertNotNull(response.getBody());
//        assertTrue(response.getBody() instanceof Resource);
    }

    @Test
    public void testDeleteResource_timeout(){
//        RabbitManager rabbitManager = Mockito.mock(RabbitManager.class);
//        when(rabbitManager.sendResourceRemovalRequest(any())).thenReturn(null);
//
//        Resource resource = new Resource();
//
//        CloudCoreInterfaceController controller = new CloudCoreInterfaceController(rabbitManager);
//        ResponseEntity<?> response = controller.deleteResource(null, null);
//
//        assertEquals(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void testDeleteResource_badRequest(){
//        RpcResourceResponse rabbitResponse = new RpcResourceResponse();
//        rabbitResponse.setStatus(400);
//
//        RabbitManager rabbitManager = Mockito.mock(RabbitManager.class);
//        when(rabbitManager.sendResourceRemovalRequest(any())).thenReturn(rabbitResponse);
//
//        Resource resource = new Resource();
//
//        CloudCoreInterfaceController controller = new CloudCoreInterfaceController(rabbitManager);
//        ResponseEntity<?> response = controller.deleteResource(null, null);
//
//        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
//        assertNull(response.getBody());
    }

    @Test
    public void testDeleteResource_success(){
//        Resource resource = new Resource();
//        resource.setId("testId");
//
//        RpcResourceResponse rabbitResponse = new RpcResourceResponse();
//        rabbitResponse.setStatus(200);
//        rabbitResponse.setResource(resource);
//
//        RabbitManager rabbitManager = Mockito.mock(RabbitManager.class);
//        when(rabbitManager.sendResourceRemovalRequest(any())).thenReturn(rabbitResponse);
//
//        CloudCoreInterfaceController controller = new CloudCoreInterfaceController(rabbitManager);
//        ResponseEntity<?> response = controller.deleteResource(null, null);
//
//        assertEquals(response.getStatusCode(), HttpStatus.OK);
//        assertNull(response.getBody());
    }


}