package com.ticketti.ms_mensajeria;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
		"spring.main.allow-bean-definition-overriding=true"
})
class MsMensajeriaApplicationTests {

	@MockitoBean
	private ConnectionFactory connectionFactory;

	@Test
	void contextLoads() {
	}

	@TestConfiguration
	static class RabbitTestConfig {
		@Bean
		@Primary
		SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
				ConnectionFactory connectionFactory) {
			SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
			factory.setConnectionFactory(connectionFactory);
			factory.setAutoStartup(false);
			return factory;
		}
	}

}
