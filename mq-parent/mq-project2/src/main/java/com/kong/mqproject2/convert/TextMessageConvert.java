package com.kong.mqproject2.convert;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;

public class TextMessageConvert implements MessageConverter {
    @Override
    public Message toMessage(Object o, MessageProperties messageProperties) throws MessageConversionException {
        //java msg to mq msg
        Message message = new Message(o.toString().getBytes(), messageProperties);
        return message;
    }

    @Override
    public Object fromMessage(Message message) throws MessageConversionException {
        //rabbitmq msg to java
        String contentType = message.getMessageProperties().getContentType();
        if(null!=contentType && contentType.contains("text")){
            return new String(message.getBody());
        }
        return message.getBody();
    }
}
