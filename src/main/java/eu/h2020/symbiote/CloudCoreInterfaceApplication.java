package eu.h2020.symbiote;

import eu.h2020.symbiote.communication.RabbitManager;
import eu.h2020.symbiote.model.Location;
import eu.h2020.symbiote.model.Resource;
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
//            location.setName("Test location");
//            location.setAltitude(100);
//            location.setLongitude(20.3);
//            location.setLatitude(55.9);
//
//            List<String> observedProperties = new ArrayList<>();
//            observedProperties.add("Property 1");
//            observedProperties.add("Property 2");
//            observedProperties.add("Property 3");
//
//            Resource resource = new Resource();
//            resource.setName("Test resource");
//            resource.setDescription("This is a test resource");
//            resource.setFeatureOfInterest("Test FOI");
//            resource.setOwner("Test owner");
//            resource.setResourceURL("http://foo.bar");
//            resource.setLocation(location);
//            resource.setObservedProperties(observedProperties);
//
//            this.rabbitManager.sendResourceCreationRequest(resource);


        }
    }

    @Bean
    public AlwaysSampler defaultSampler() {
        return new AlwaysSampler();
    }

}
