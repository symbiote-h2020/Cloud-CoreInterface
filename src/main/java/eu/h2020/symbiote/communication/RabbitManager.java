package eu.h2020.symbiote.communication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringPlatformRequest;
import eu.h2020.symbiote.core.internal.*;
import eu.h2020.symbiote.core.internal.cram.NotificationMessageResponseSecured;
import eu.h2020.symbiote.core.internal.cram.NotificationMessageSecured;
import eu.h2020.symbiote.core.internal.crm.MonitoringResponseSecured;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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

    @Value("${spring.rabbitmq.template.reply-timeout}")
    private Integer rabbitMessageTimeout;

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

    @Value("${rabbit.routingKey.resource.clearDataRequested}")
    private String resourceClearDataRequestedRoutingKey;

    @Value("${rabbit.exchange.crm.name}")
    private String crmExchangeName;

    @Value("${rabbit.exchange.crm.type}")
    private String crmExchangeType;

    @Value("${rabbit.exchange.crm.durable}")
    private boolean crmExchangeDurable;

    @Value("${rabbit.exchange.crm.autodelete}")
    private boolean crmExchangeAutodelete;

    @Value("${rabbit.exchange.crm.internal}")
    private boolean crmExchangeInternal;

    @Value("${rabbit.routingKey.crm.monitoring}")
    private String crmMonitoringRoutingKey;

    @Value("${rabbit.exchange.cram.name}")
    private String cramExchangeName;

    @Value("${rabbit.exchange.cram.type}")
    private String cramExchangeType;

    @Value("${rabbit.exchange.cram.durable}")
    private boolean cramExchangeDurable;

    @Value("${rabbit.exchange.cram.autodelete}")
    private boolean cramExchangeAutodelete;

    @Value("${rabbit.exchange.cram.internal}")
    private boolean cramExchangeInternal;

    @Value("${rabbit.routingKey.cram.accessNotifications}")
    private String cramAccessNotificationRoutingKey;

    @Value("${rabbit.exchange.ssp.name}")
    private String sspExchangeName;

    @Value("${rabbit.exchange.ssp.type}")
    private String sspExchangeType;

    @Value("${rabbit.exchange.ssp.durable}")
    private boolean sspExchangeDurable;

    @Value("${rabbit.exchange.ssp.autodelete}")
    private boolean sspExchangeAutodelete;

    @Value("${rabbit.exchange.ssp.internal}")
    private boolean sspExchangeInternal;

    @Value("${rabbit.routingKey.ssp.sdev.creationRequested}")
    private String sdevCreationRequestedRoutingKey;

    @Value("${rabbit.routingKey.ssp.sdev.removalRequested}")
    private String sdevRemovalRequestedRoutingKey;

    @Value("${rabbit.routingKey.ssp.sdev.modificationRequested}")
    private String sdevModificationRequestedRoutingKey;

    @Value("${rabbit.routingKey.ssp.sdev.resource.creationRequested}")
    private String sspResourceCreationRequestedRoutingKey;

    @Value("${rabbit.routingKey.ssp.sdev.resource.removalRequested}")
    private String sspResourceRemovalRequestedRoutingKey;

    @Value("${rabbit.routingKey.ssp.sdev.resource.modificationRequested}")
    private String sspResourceModificationRequestedRoutingKey;

    private Connection connection;
    private Channel channel;

    private Map<String, Object> queueArgs;

    /**
     * Method used to override connection parameters.
     * Used ONLY for unit testing.
     *
     * @param rabbitHost
     * @param rabbitUsername
     * @param rabbitPassword
     * @param exchangeName
     * @param exchangeType
     * @param exchangeDurable
     * @param exchangeAutodelete
     * @param exchangeInternal
     */
    public void setTestParameters(String rabbitHost, String rabbitUsername, String rabbitPassword, String exchangeName, String exchangeType, boolean exchangeDurable, boolean exchangeAutodelete, boolean exchangeInternal) {
        this.rabbitHost = rabbitHost;
        this.rabbitUsername = rabbitUsername;
        this.rabbitPassword = rabbitPassword;
        this.rabbitMessageTimeout = 30000;

        this.crmExchangeName = exchangeName;
        this.crmExchangeType = exchangeType;
        this.crmExchangeDurable = exchangeDurable;
        this.crmExchangeAutodelete = exchangeAutodelete;
        this.crmExchangeInternal = exchangeInternal;

        this.resourceExchangeName = exchangeName;
        this.resourceExchangeType = exchangeType;
        this.resourceExchangeDurable = exchangeDurable;
        this.resourceExchangeAutodelete = exchangeAutodelete;
        this.resourceExchangeInternal = exchangeInternal;

        this.cramExchangeName = exchangeName;
        this.cramExchangeType = exchangeType;
        this.cramExchangeDurable = exchangeDurable;
        this.cramExchangeAutodelete = exchangeAutodelete;
        this.cramExchangeInternal = exchangeInternal;

        this.sspExchangeName = exchangeName;
        this.sspExchangeType = exchangeType;
        this.sspExchangeDurable = exchangeDurable;
        this.sspExchangeAutodelete = exchangeAutodelete;
        this.sspExchangeInternal = exchangeInternal;
    }

    /**
     * Method used to initialise RabbitMQ connection and declare all required exchanges.
     * This method should be called once, after bean initialization (so that properties from CoreConfigServer are obtained),
     * but before using RabbitManager to send any message.
     */
    public void initCommunication() {
        try {

            queueArgs = new HashMap<>();
            queueArgs.put("x-message-ttl", rabbitMessageTimeout);

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

            this.channel.exchangeDeclare(this.crmExchangeName,
                    this.crmExchangeType,
                    this.crmExchangeDurable,
                    this.crmExchangeAutodelete,
                    this.crmExchangeInternal,
                    null);

            this.channel.exchangeDeclare(this.cramExchangeName,
                    this.cramExchangeType,
                    this.cramExchangeDurable,
                    this.cramExchangeAutodelete,
                    this.cramExchangeInternal,
                    null);

            this.channel.exchangeDeclare(this.sspExchangeName,
                    this.sspExchangeType,
                    this.sspExchangeDurable,
                    this.sspExchangeAutodelete,
                    this.sspExchangeInternal,
                    null);

        } catch (IOException | TimeoutException e) {
            log.error("Error while initiating communication via RabbitMQ", e);
        }
    }

    /**
     * Cleanup method, used to close RabbitMQ channel and connection.
     */
    @PreDestroy
    public void cleanup() {
        try {
            if (this.channel != null && this.channel.isOpen())
                this.channel.close();
            if (this.connection != null && this.connection.isOpen())
                this.connection.close();
        } catch (IOException | TimeoutException e) {
            log.error("Error while closing connection with RabbitMQ", e);
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
        QueueingConsumer consumer = new QueueingConsumer(channel);

        try {
            log.debug("Sending message...");

            String replyQueueName = UUID.randomUUID().toString();
            this.channel.queueDeclare(replyQueueName, false, true, true, queueArgs);
//            this.channel.queueDeclare(replyQueueName, false, true, true, null);


            String correlationId = UUID.randomUUID().toString();
            AMQP.BasicProperties props = new AMQP.BasicProperties()
                    .builder()
                    .correlationId(correlationId)
                    .replyTo(replyQueueName)
                    .contentType("application/json")
                    .build();

            channel.basicConsume(replyQueueName, true, consumer);

            String responseMsg = null;

            this.channel.basicPublish(exchangeName, routingKey, props, message.getBytes());
            while (true) {
                QueueingConsumer.Delivery delivery = consumer.nextDelivery(20000);
                if (delivery == null) {
                    log.info("Timeout in response retrieval");
                    return null;
                }

                if (delivery.getProperties().getCorrelationId().equals(correlationId)) {
                    log.debug("Got reply with correlationId: " + correlationId + " and delivery: " + delivery);
                    responseMsg = new String(delivery.getBody());
                    break;
                } else {
                    log.debug("Got answer with wrong correlationId... should be " + correlationId + " but got " + delivery.getProperties().getCorrelationId());
                }
            }
            log.debug("Finished rpc loop");

            return responseMsg;
        } catch (IOException | InterruptedException e) {
            log.error("Error while sending RPC Message via RabbitMQ", e);
        } finally {
            try {
                this.channel.basicCancel(consumer.getConsumerTag());
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        return null;
    }


    /**
     * Method used to send an asynchronous message, without expecting any returning result.
     * Exchange should be declared before sending the message.
     *
     * @param exchangeName name of the exchange to send message to
     * @param routingKey   routing key to send message to
     * @param message      message to be sent
     * @return true if publish went ok, false otherwise
     */
    public boolean sendAsyncMessage(String exchangeName, String routingKey, String message) {
        try {
            AMQP.BasicProperties props = new AMQP.BasicProperties()
                    .builder()
                    .contentType("application/json")
                    .build();
            this.channel.basicPublish(exchangeName, routingKey, props, message.getBytes());
            return true;
        } catch (IOException e) {
            log.error("Error while sending async message via RabbitMQ", e);
            return false;
        }
    }

    /**
     * Helper method that provides JSON marshalling and unmarshalling for the sake of Rabbit communication.
     *
     * @param exchangeName        name of the exchange to send message to
     * @param routingKey          routing key to send message to
     * @param coreResourceRequest resource to be sent
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
            return mapper.readValue(responseMsg, CoreResourceRegistryResponse.class);
        } catch (IOException e) {
            log.error("Failed (un)marshalling of rpc resource message", e);
        }
        return null;
    }

    /**
     * Helper method that provides JSON marshalling and unmarshalling for the sake of Rabbit communication.
     *
     * @param exchangeName    name of the exchange to send message to
     * @param routingKey      routing key to send message to
     * @param coreSdevRequest resource to be sent
     * @return response from the consumer or null if timeout occurs
     */
    public CoreSdevRegistryResponse sendRpcSdevMessage(String exchangeName, String routingKey, CoreSdevRegistryRequest coreSdevRequest) {
        try {
            String message;
            ObjectMapper mapper = new ObjectMapper();
            message = mapper.writeValueAsString(coreSdevRequest);

            String responseMsg = this.sendRpcMessage(exchangeName, routingKey, message);

            if (responseMsg == null)
                return null;

            mapper = new ObjectMapper();
            return mapper.readValue(responseMsg, CoreSdevRegistryResponse.class);
        } catch (IOException e) {
            log.error("Failed (un)marshalling of rpc sdev message", e);
        }
        return null;
    }

    /**
     * Helper method that provides JSON marshalling and unmarshalling for the sake of Rabbit communication.
     *
     * @param exchangeName           name of the exchange to send message to
     * @param routingKey             routing key to send message to
     * @param coreSspResourceRequest resource to be sent
     * @return response from the consumer or null if timeout occurs
     */
    public CoreSspResourceRegistryResponse sendRpcSspResourceMessage(String exchangeName, String routingKey, CoreSspResourceRegistryRequest coreSspResourceRequest) {
        try {
            String message;
            ObjectMapper mapper = new ObjectMapper();
            message = mapper.writeValueAsString(coreSspResourceRequest);

            String responseMsg = this.sendRpcMessage(exchangeName, routingKey, message);

            if (responseMsg == null)
                return null;

            mapper = new ObjectMapper();
            return mapper.readValue(responseMsg, CoreSspResourceRegistryResponse.class);
        } catch (IOException e) {
            log.error("Failed (un)marshalling of rpc sdev message", e);
        }
        return null;
    }

    /**
     * Helper method that provides JSON marshalling and unmarshalling for the sake of Rabbit communication.
     *
     * @param exchangeName name of the exchange to send message to
     * @param routingKey   routing key to send message to
     * @param request      resource to be sent
     * @return response from the consumer or null if timeout occurs
     */
    public ClearDataResponse sendRpcClearDataMessage(String exchangeName, String routingKey, ClearDataRequest request) {
        try {
            String message;
            ObjectMapper mapper = new ObjectMapper();
            message = mapper.writeValueAsString(request);

            String responseMsg = this.sendRpcMessage(exchangeName, routingKey, message);

            if (responseMsg == null)
                return null;

            mapper = new ObjectMapper();
            return mapper.readValue(responseMsg, ClearDataResponse.class);
        } catch (IOException e) {
            log.error("Failed (un)marshalling of rpc resource message for clearData", e);
        }
        return null;
    }

    /**
     * Helper method that provides JSON marshalling and unmarshalling for the sake of Rabbit communication.
     *
     * @param exchangeName name of the exchange to send message to
     * @param routingKey   routing key to send message to
     * @param request      resource to be sent
     * @return response from the consumer or null if timeout occurs
     */
    public NotificationMessageResponseSecured sendRpcAccessNotificationMessage(String exchangeName, String routingKey, NotificationMessageSecured request) {
        try {
            String message;
            ObjectMapper mapper = new ObjectMapper();
            message = mapper.writeValueAsString(request);

            String responseMsg = this.sendRpcMessage(exchangeName, routingKey, message);

            if (responseMsg == null)
                return null;

            mapper = new ObjectMapper();
            return mapper.readValue(responseMsg, NotificationMessageResponseSecured.class);
        } catch (IOException e) {
            log.error("Failed (un)marshalling of rpc resource message for sendRpcAccessNotificationMessage", e);
        }
        return null;
    }

    /**
     * Helper method that provides JSON marshalling and unmarshalling for the sake of Rabbit communication.
     *
     * @param exchangeName name of the exchange to send message to
     * @param routingKey   routing key to send message to
     * @param request      resource to be sent
     * @return response from the consumer or null if timeout occurs
     */
    public MonitoringResponseSecured sendRpcMonitoringMessage(String exchangeName, String routingKey,
                                                              CloudMonitoringPlatformRequest request) {
        try {
            String message;
            ObjectMapper mapper = new ObjectMapper();
            message = mapper.writeValueAsString(request);

            log.debug("sendRpcMonitoringMessage: Sending " + message);
            String responseMsg = this.sendRpcMessage(exchangeName, routingKey, message);
            log.debug("sendRpcMonitoringMessage: Received " + responseMsg);

            if (responseMsg == null)
                return null;

            mapper = new ObjectMapper();
            return mapper.readValue(responseMsg, MonitoringResponseSecured.class);
        } catch (IOException e) {
            log.error("Failed (un)marshalling of rpc resource message for sendRpcMonitoringMessage", e);
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

    /**
     * Method used to send RPC request to clear resource data for a platform
     *
     * @param clearDataRequest request containing platform id
     * @return response containing status of requested operation
     */
    public ClearDataResponse sendClearDataRequest(ClearDataRequest clearDataRequest) {
        return sendRpcClearDataMessage(this.resourceExchangeName, this.resourceClearDataRequestedRoutingKey, clearDataRequest);
    }

    /**
     * Method used to send asynchronous, monitoring message to Core Resource Monitor.
     *
     * @param cloudMonitoringPlatformRequest message from platform
     * @return true if message is sent ok, false otherwise
     */
    public MonitoringResponseSecured sendMonitoringMessage(CloudMonitoringPlatformRequest cloudMonitoringPlatformRequest) {
        log.debug("SendMonitoringMessage, exchange = " + this.crmExchangeName + ", routingKey = " + this.crmMonitoringRoutingKey);
        return sendRpcMonitoringMessage(this.crmExchangeName, this.crmMonitoringRoutingKey, cloudMonitoringPlatformRequest);
    }

    /**
     * Method used to send asynchronous, access notification message to Core Resource Access Monitor.
     *
     * @param notificationMessage access notification message
     * @return the response of CRAM
     */
    public NotificationMessageResponseSecured sendAccessNotificationMessage(NotificationMessageSecured notificationMessage) {
        log.debug("sendAccessNotificationMessage, exchange = " + this.cramExchangeName + ", routingKey = " + this.cramAccessNotificationRoutingKey);

        return sendRpcAccessNotificationMessage(this.cramExchangeName,
                this.cramAccessNotificationRoutingKey, notificationMessage);

    }

    /**
     * Method used to send RPC request to create a smart device.
     *
     * @param coreSdevRequest smart device to be created
     * @return object containing status of requested operation and, if successful, a smart device object containing assigned ID
     */
    public CoreSdevRegistryResponse sendSdevCreationRequest(CoreSdevRegistryRequest coreSdevRequest) {
        return sendRpcSdevMessage(this.sspExchangeName, this.sdevCreationRequestedRoutingKey, coreSdevRequest);
    }

    /**
     * Method used to send RPC request to remove a smart device.
     *
     * @param coreSdevRequest smart device to be removed
     * @return object containing status of requested operation and, if successful, a smart device object
     */
    public CoreSdevRegistryResponse sendSdevRemovalRequest(CoreSdevRegistryRequest coreSdevRequest) {
        return sendRpcSdevMessage(this.sspExchangeName, this.sdevRemovalRequestedRoutingKey, coreSdevRequest);
    }

    /**
     * Method used to send RPC request to modify a smart device.
     *
     * @param coreSdevRequest smart device to be modified
     * @return object containing status of requested operation and, if successful, a smart device object
     */
    public CoreSdevRegistryResponse sendSdevModificationRequest(CoreSdevRegistryRequest coreSdevRequest) {
        return sendRpcSdevMessage(this.sspExchangeName, this.sdevModificationRequestedRoutingKey, coreSdevRequest);
    }

    /**
     * Method used to send RPC request to create an SSP resource.
     *
     * @param coreSspResourceRequest SSP resource to be created
     * @return object containing status of requested operation and, if successful, an SSP Resource object containing assigned ID
     */
    public CoreSspResourceRegistryResponse sendSspResourceCreationRequest(CoreSspResourceRegistryRequest coreSspResourceRequest) {
        return sendRpcSspResourceMessage(this.resourceExchangeName, this.sspResourceCreationRequestedRoutingKey, coreSspResourceRequest);
    }

    /**
     * Method used to send RPC request to modify an SSP resource.
     *
     * @param coreSspResourceRequest SSP resource to be modified
     * @return object containing status of requested operation and, if successful, an SSP Resource object
     */
    public CoreSspResourceRegistryResponse sendSspResourceModificationRequest(CoreSspResourceRegistryRequest coreSspResourceRequest) {
        return sendRpcSspResourceMessage(this.resourceExchangeName, this.sspResourceModificationRequestedRoutingKey, coreSspResourceRequest);
    }

    /**
     * Method used to send RPC request to delete an SSP resource.
     *
     * @param coreSspResourceRequest SSP resource to be deleted
     * @return object containing status of requested operation and, if successful, an SSP Resource object
     */
    public CoreSspResourceRegistryResponse sendSspResourceRemovalRequest(CoreSspResourceRegistryRequest coreSspResourceRequest) {
        return sendRpcSspResourceMessage(this.resourceExchangeName, this.sspResourceRemovalRequestedRoutingKey, coreSspResourceRequest);
    }

    /**
     * Get current RabbitMQ channel.
     * Used ONLY dor unit testing.
     *
     * @return current RabbitMQ channel
     */
    public Channel getChannel() {
        return this.channel;
    }
}
