package com.ticketti.ms_mensajeria.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
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
    public DirectExchange tickettiExchange() {
        // durable=true: sobrevive reinicios de RabbitMQ
        return new DirectExchange(EXCHANGE, true, false);
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
                                     DirectExchange tickettiExchange) {
        return BindingBuilder
                .bind(queueMensajeria)
                .to(tickettiExchange)
                .with(ROUTING_KEY);
    }

    @Bean
    @SuppressWarnings("removal")
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    @SuppressWarnings({"null", "removal"})
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf) {
        RabbitTemplate template = new RabbitTemplate(cf);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
