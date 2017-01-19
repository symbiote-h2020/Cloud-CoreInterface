package eu.h2020.symbiote.communication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import eu.h2020.symbiote.model.Resource;
import eu.h2020.symbiote.model.ResourceCreationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * Bean used to manage internal communication using RabbitMQ.
 * It is responsible for declaring exchanges and using routing keys from centralized config server.
 */
@Component
public class RabbitManager {

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

    private Connection connection;
    private Channel channel;

    /**
     * Initialization method.
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
     * Cleanup method
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

    private void sendMessage(String exchange, String routingKey, String message) {
        try {
            this.channel.basicPublish(exchange, routingKey, null, message.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ResourceCreationResponse sendRpcMessage(String message) {
        try {
            System.out.println("Sending message...");

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

            this.channel.basicPublish(this.resourceExchangeName, this.resourceCreationRequestedRoutingKey, props, message.getBytes());
            while (true){
                QueueingConsumer.Delivery delivery = consumer.nextDelivery(30000);
                if (delivery == null)
                    return null;

                if (delivery.getProperties().getCorrelationId().equals(correlationId)) {
                    responseMsg = new String(delivery.getBody());
                    break;
                }
            }

            ObjectMapper mapper = new ObjectMapper();
            ResourceCreationResponse response =  mapper.readValue(responseMsg, ResourceCreationResponse.class);
            System.out.println(response);
            return response;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Method used to send RPC request to create platform.
     *
     * @param resource resource to be created
     * @return object containing status of requested operation and, if successful, a resource object containing assigned ID
     */
    public ResourceCreationResponse sendResourceCreationRequest(Resource resource) {
        try {
            String message = null;
            ObjectMapper mapper = new ObjectMapper();
            message = mapper.writeValueAsString(resource);

            return sendRpcMessage(message);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
