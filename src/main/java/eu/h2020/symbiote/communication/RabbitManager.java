package eu.h2020.symbiote.communication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import eu.h2020.symbiote.core.internal.CoreResourceRegistryRequest;
import eu.h2020.symbiote.core.internal.CoreResourceRegistryResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * Class used for all internal communication using RabbitMQ AMQP implementation.
 * It works as a Spring Bean, and should be used via autowiring.
 * <p>
 * RabbitManager uses properties taken from CoreConfigServer to set up communication (exchange parameters, routing keys etc.)
 */
@Component
public class RabbitManager {
    private static Log log = LogFactory.getLog(RabbitManager.class);

    @Value("${rabbit.host}")
    private String rabbitHost;

    @Value("${rabbit.username}")
    private String rabbitUsername;

    @Value("${rabbit.password}")
    private String rabbitPassword;

    @Value("${rabbit.exchange.resource.name}")
    private String resourceExchangeName;

    @Value("${rabbit.exchange.resource.type}")
    private String resourceExchangeType;

    @Value("${rabbit.exchange.resource.durable}")
    private boolean resourceExchangeDurable;

    @Value("${rabbit.exchange.resource.autodelete}")
    private boolean resourceExchangeAutodelete;

    @Value("${rabbit.exchange.resource.internal}")
    private boolean resourceExchangeInternal;

    @Value("${rabbit.routingKey.resource.creationRequested}")
    private String resourceCreationRequestedRoutingKey;

    @Value("${rabbit.routingKey.resource.removalRequested}")
    private String resourceRemovalRequestedRoutingKey;

    @Value("${rabbit.routingKey.resource.modificationRequested}")
    private String resourceModificationRequestedRoutingKey;

    private Connection connection;
    private Channel channel;

    /**
     * Method used to initialise RabbitMQ connection and declare all required exchanges.
     * This method should be called once, after bean initialization (so that properties from CoreConfigServer are obtained),
     * but before using RabbitManager to send any message.
     */
    public void initCommunication() {
        try {
            ConnectionFactory factory = new ConnectionFactory();

            factory.setHost(this.rabbitHost);
            factory.setUsername(this.rabbitUsername);
            factory.setPassword(this.rabbitPassword);

            this.connection = factory.newConnection();

            this.channel = this.connection.createChannel();
            this.channel.exchangeDeclare(this.resourceExchangeName,
                    this.resourceExchangeType,
                    this.resourceExchangeDurable,
                    this.resourceExchangeAutodelete,
                    this.resourceExchangeInternal,
                    null);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    /**
     * Cleanup method, used to close RabbitMQ channel and connection.
     */
    @PreDestroy
    private void cleanup() {
        try {
            if (this.channel != null && this.channel.isOpen())
                this.channel.close();
            if (this.connection != null && this.connection.isOpen())
                this.connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method used to send message via RPC (Remote Procedure Call) pattern.
     * In this implementation it covers asynchronous Rabbit communication with synchronous one, as it is used by conventional REST facade.
     * Before sending a message, a temporary response queue is declared and its name is passed along with the message.
     * When a consumer handles the message, it returns the result via the response queue.
     * Since this is a synchronous pattern, it uses timeout of 20 seconds. If the response doesn't come in that time, the method returns with null result.
     *
     * @param exchangeName name of the exchange to send message to
     * @param routingKey   routing key to send message to
     * @param message      message to be sent
     * @return response from the consumer or null if timeout occurs
     */
    public String sendRpcMessage(String exchangeName, String routingKey, String message) {
        try {
            log.debug("Sending message...");

            String replyQueueName = this.channel.queueDeclare().getQueue();

            String correlationId = UUID.randomUUID().toString();
            AMQP.BasicProperties props = new AMQP.BasicProperties()
                    .builder()
                    .correlationId(correlationId)
                    .replyTo(replyQueueName)
                    .build();

            QueueingConsumer consumer = new QueueingConsumer(channel);
            this.channel.basicConsume(replyQueueName, true, consumer);

            String responseMsg = null;

            this.channel.basicPublish(exchangeName, routingKey, props, message.getBytes());
            while (true) {
                QueueingConsumer.Delivery delivery = consumer.nextDelivery(20000);
                if (delivery == null)
                    return null;

                if (delivery.getProperties().getCorrelationId().equals(correlationId)) {
                    responseMsg = new String(delivery.getBody());
                    break;
                }
            }

            return responseMsg;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Helper method that provides JSON marshalling and unmarshalling for the sake of Rabbit communication.
     *
     * @param exchangeName name of the exchange to send message to
     * @param routingKey   routing key to send message to
     * @param coreResourceRequest     resource to be sent
     * @return response from the consumer or null if timeout occurs
     */
    public CoreResourceRegistryResponse sendRpcResourceMessage(String exchangeName, String routingKey, CoreResourceRegistryRequest coreResourceRequest) {
        try {
            String message;
            ObjectMapper mapper = new ObjectMapper();
            message = mapper.writeValueAsString(coreResourceRequest);

            String responseMsg = this.sendRpcMessage(exchangeName, routingKey, message);

            if (responseMsg == null)
                return null;

            mapper = new ObjectMapper();
            CoreResourceRegistryResponse response = mapper.readValue(responseMsg, CoreResourceRegistryResponse.class);
            return response;
        } catch (IOException e) {
            log.error("Failed (un)marshalling of rpc resource message", e);
        }
        return null;
    }

    /**
     * Method used to send RPC request to create resource.
     *
     * @param coreResourceRequest resource to be created
     * @return object containing status of requested operation and, if successful, a resource object containing assigned ID
     */
    public CoreResourceRegistryResponse sendResourceCreationRequest(CoreResourceRegistryRequest coreResourceRequest) {
        return sendRpcResourceMessage(this.resourceExchangeName, this.resourceCreationRequestedRoutingKey, coreResourceRequest);
    }

    /**
     * Method used to send RPC request to remove resource.
     *
     * @param coreResourceRequest resource to be removed
     * @return object containing status of requested operation and, if successful, a resource object
     */
    public CoreResourceRegistryResponse sendResourceRemovalRequest(CoreResourceRegistryRequest coreResourceRequest) {
        return sendRpcResourceMessage(this.resourceExchangeName, this.resourceRemovalRequestedRoutingKey, coreResourceRequest);
    }

    /**
     * Method used to send RPC request to modify resource.
     *
     * @param coreResourceRequest resource to be modified
     * @return object containing status of requested operation and, if successful, a resource object
     */
    public CoreResourceRegistryResponse sendResourceModificationRequest(CoreResourceRegistryRequest coreResourceRequest) {
        return sendRpcResourceMessage(this.resourceExchangeName, this.resourceModificationRequestedRoutingKey, coreResourceRequest);
    }
}
