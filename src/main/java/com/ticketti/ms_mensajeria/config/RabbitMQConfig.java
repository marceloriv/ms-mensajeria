package com.ticketti.ms_mensajeria.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Mismo exchange que MSCarrito — NO cambiar este valor
    public static final String EXCHANGE         = "ticketti.exchange";

    // Queue PROPIA de MS-Mensajería
    public static final String QUEUE_MENSAJERIA = "mensajeria.queue";

    // Routing key que publica MSCarrito
    public static final String ROUTING_KEY      = "pago.aprobado";

    @Bean
    public TopicExchange exchange() {
        // durable=true: sobrevive reinicios de RabbitMQ
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue queueMensajeria() {
        return QueueBuilder.durable(QUEUE_MENSAJERIA).build();
    }

    /**
     * Bindea la queue de mensajería al exchange con la
     * misma routing key que usa MSCarrito al publicar.
     * Así ambos microservicios reciben el mismo evento.
     */
    @Bean
    public Binding bindingMensajeria(Queue queueMensajeria,
                                     TopicExchange exchange) {
        return BindingBuilder
                .bind(queueMensajeria)
                .to(exchange)
                .with(ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf) {
        RabbitTemplate template = new RabbitTemplate(cf);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
