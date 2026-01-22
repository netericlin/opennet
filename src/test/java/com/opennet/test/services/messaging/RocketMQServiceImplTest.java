
package com.opennet.test.services.messaging;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RocketMQServiceImplTest {

    @Mock
    private DefaultMQProducer defaultMQProducer;

    @InjectMocks
    private RocketMQServiceImpl rocketMQService;

    @Test
    void testSendMessage() throws Exception {
        String topic = "test-topic";
        String message = "Hello, RocketMQ!";
        when(defaultMQProducer.send(any(Message.class))).thenReturn(new SendResult());

        rocketMQService.sendMessage(topic, message);

        verify(defaultMQProducer).send(any(Message.class));
    }
}
