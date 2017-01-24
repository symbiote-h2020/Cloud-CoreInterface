package eu.h2020.symbiote;

import eu.h2020.symbiote.communication.RabbitManager;
import eu.h2020.symbiote.model.Location;
import eu.h2020.symbiote.model.Resource;
import eu.h2020.symbiote.model.RpcResourceResponse;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.sampler.AlwaysSampler;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by mateuszl on 22.09.2016.
 */
@EnableDiscoveryClient
@SpringBootApplication
public class CloudCoreInterfaceApplication {

	private static Log log = LogFactory.getLog(CloudCoreInterfaceApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(CloudCoreInterfaceApplication.class, args);
    }

    @Component
    public static class CLR implements CommandLineRunner {

        private final RabbitManager rabbitManager;

        @Autowired
        public CLR( RabbitManager rabbitManager) {
            this.rabbitManager = rabbitManager;
        }

        @Override
        public void run(String... args) throws Exception {
            this.rabbitManager.initCommunication();

//            Location location = new Location();
//            location.setDescription("This is a test location");
//            location.setName("Changed Test location");
//            location.setAltitude(100);
//            location.setLongitude(20.3);
//            location.setLatitude(55.9);
//            location.setId("5885df6b3999e538c88a5ba8");
//
//            List<String> observedProperties = new ArrayList<>();
//            observedProperties.add("Property 1");
//            observedProperties.add("Property 2");
//            observedProperties.add("Property 3");
//
//            Resource resource = new Resource();
//            resource.setName("Changed Resource to delete");
//            resource.setDescription("This is a test resource");
//            resource.setFeatureOfInterest("Test FOI");
//            resource.setOwner("Test owner");
//            resource.setResourceURL("http://foo.bar");
//            resource.setLocation(location);
//            resource.setObservedProperties(observedProperties);
//            resource.setPlatformId("TestPlatformId");
//            resource.setId("5885df6b3999e538c88a5ba9");
//
//            RpcResourceResponse response = this.rabbitManager.sendResourceCreationRequest(resource);
////            RpcResourceResponse response = this.rabbitManager.sendResourceRemovalRequest(resource);
////            RpcResourceResponse response = this.rabbitManager.sendResourceModificationRequest(resource);
//            System.out.println(response);
        }
    }

    @Bean
    public AlwaysSampler defaultSampler() {
        return new AlwaysSampler();
    }

}
