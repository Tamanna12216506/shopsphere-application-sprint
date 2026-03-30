package com.capgemini.orderservice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("Temporarily disabled for local/Sonar runs because it depends on external config-server startup")
@SpringBootTest(properties = {
		"spring.config.import=optional:file:./",
		"spring.cloud.config.enabled=false",
		"spring.cloud.config.fail-fast=false",
		"eureka.client.enabled=false",
		"spring.datasource.url=jdbc:h2:mem:orderservice-test;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"spring.data.redis.repositories.enabled=false"
})
class OrderserviceApplicationTests {

	@Test
	void contextLoads() {
	}

}
