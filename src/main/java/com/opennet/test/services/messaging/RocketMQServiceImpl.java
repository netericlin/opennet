
package com.opennet.test.services.messaging;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RocketMQServiceImpl implements RocketMQService {

    @Autowired
    private DefaultMQProducer defaultMQProducer;

    @Override
    public void sendMessage(String topic, String message) throws Exception {
        Message msg = new Message(topic, "*", message.getBytes());
        defaultMQProducer.send(msg);
    }
}
