package com.kong.mqproject2.config;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareBatchMessageListener;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.amqp.support.ConsumerTagStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.UUID;


/**
 * @author gedachao
 * @description
 * @date 2020/5/15 9:27
 */
@Configuration
@ComponentScan("com.kong.mqproject2.*")
public class RabbitMQConfig {

    @Bean
    public ConnectionFactory connectionFactory() throws IOException {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        InputStream inputStream = RabbitMQConfig.class.getResourceAsStream("/mq/mq.properties");
        Properties properties = new Properties();
        properties.load(inputStream);
        connectionFactory.setAddresses(properties.getProperty("mq.host"));
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        connectionFactory.setVirtualHost("/");
        return connectionFactory;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory){
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        rabbitAdmin.setAutoStartup(true);
        return rabbitAdmin;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory){
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        return rabbitTemplate;

    }

    @Bean
    public TopicExchange exchange001(){
        return new TopicExchange("exchange001",false,false);
    }

    @Bean
    public DirectExchange exchange002(){
        return new DirectExchange("exchange002",false,false);
    }

    @Bean
    public Queue queue001(){
        return new Queue("queue001",false);
    }

    @Bean
    public Queue queue002(){
        return new Queue("queue002",false);
    }

    @Bean
    public Binding binding001(){
        return BindingBuilder.bind(queue001()).to(exchange001()).with("spring.#");
    }
    @Bean
    public Binding binding002(){
        return BindingBuilder.bind(queue002()).to(exchange001()).with("rabbitmq.#");
    }

    @Bean
    public Binding binding003(){
        return BindingBuilder.bind(queue001()).to(exchange002()).with("bing3.#");
    }

    @Bean
    public SimpleMessageListenerContainer messageListenerContainer(ConnectionFactory connectionFactory){
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
        container.setQueues(queue001(),queue002());
        container.setConcurrentConsumers(1);
        container.setMaxConcurrentConsumers(5);
        container.setDefaultRequeueRejected(false);
        container.setAcknowledgeMode(AcknowledgeMode.AUTO);
        container.setConsumerTagStrategy(new ConsumerTagStrategy() {
            @Override
            public String createConsumerTag(String s) {
                return s+"_"+ UUID.randomUUID().toString();
            }
        });
        /*container.setMessageListener(new ChannelAwareMessageListener() {
            @Override
            public void onMessage(Message message, Channel channel) throws Exception {
                String str  = new String(message.getBody());
                System.out.println("消费者："+str);
            }
        });*/

        MessageListenerAdapter adapter = new MessageListenerAdapter(new MessageDelegate());
//        adapter.setDefaultListenerMethod("sef");
        container.setMessageListener(adapter);
        return container;
    }



}
