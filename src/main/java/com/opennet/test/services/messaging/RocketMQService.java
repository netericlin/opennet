
package com.opennet.test.services.messaging;

public interface RocketMQService {
    void sendMessage(String topic, String message) throws Exception;
}
