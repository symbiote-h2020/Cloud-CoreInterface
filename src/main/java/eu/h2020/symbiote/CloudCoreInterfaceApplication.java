package eu.h2020.symbiote;

import eu.h2020.symbiote.communication.RabbitManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.sleuth.sampler.AlwaysSampler;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;


/**
 * Cloud-Core Interface module's entry point.
 * <p>
 * Cloud-Core Interface is a southbound interface for accessing symbIoTe platform.
 * It allows platform owners to register their resources.
 */
@EnableDiscoveryClient
@SpringBootApplication
public class CloudCoreInterfaceApplication {

    public static void main(String[] args) {

        WaitForPort.waitForServices(WaitForPort.findProperty("SPRING_BOOT_WAIT_FOR_SERVICES"));
        SpringApplication.run(CloudCoreInterfaceApplication.class, args);
    }

    @Component
    public static class CLR implements CommandLineRunner {

        private final RabbitManager rabbitManager;

        @Autowired
        public CLR(RabbitManager rabbitManager) {
            this.rabbitManager = rabbitManager;
        }

        @Override
        public void run(String... args) throws Exception {
            this.rabbitManager.initCommunication();
        }
    }

    @Bean
    public AlwaysSampler defaultSampler() {
        return new AlwaysSampler();
    }

}
