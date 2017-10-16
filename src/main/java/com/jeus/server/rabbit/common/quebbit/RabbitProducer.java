package com.jeus.server.rabbit.common.quebbit;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP.BasicProperties.Builder;
import java.io.IOException;
import java.io.Serializable;
import org.apache.commons.lang.SerializationUtils;
import org.apache.log4j.Logger;

public class RabbitProducer extends RabbitDao {

    protected static final Logger LOGGER = Logger.getLogger(RabbitDao.class);

    public RabbitProducer(RabbitConfig rabbitConfig) {
        super(rabbitConfig);
    }

    public void submitSerialize(Serializable object, Priority priority) {
        BasicProperties.Builder messageQueueProperty = new Builder();
        messageQueueProperty.priority(priority.getCode());
        try {
            channel.basicPublish("", endPointName, messageQueueProperty.build(),
                    SerializationUtils.serialize(object));
        } catch (IOException e) {
            LOGGER.error("Failed to publish message [" + endPointName + "] : " + e.getMessage());
        }
    }

    public void submitSerialize(Serializable object) {
        try {
            BasicProperties.Builder messageQueueProperty = new Builder();
            messageQueueProperty.priority(Priority.MEDIUM.getCode());
            channel.basicPublish("", endPointName, messageQueueProperty.build(),
                    SerializationUtils.serialize(object));
        } catch (IOException e) {
            LOGGER.error("Failed to publish message [" + endPointName + "] : " + e.getMessage());
        }
    }

    public void submit(byte[] data) {
        try {

            channel.basicPublish("", endPointName, null, data);
        } catch (IOException e) {
            LOGGER.error("Failed to publish message [" + endPointName + "] : " + e.getMessage());
        }
    }

    public void submit(byte[] data, Priority priority) {
        try {
            BasicProperties.Builder messageQueueProperty = new Builder();
            messageQueueProperty.priority(priority.getCode());
            channel.basicPublish("", endPointName, messageQueueProperty.build(), data);
        } catch (IOException e) {
            LOGGER.error("Failed to publish message [" + endPointName + "] : " + e.getMessage());
        }
    }
}
