package com.capgemini.adminservice;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Temporarily disabled for local and Sonar runs because it depends on external config-server startup")
class AdminserviceApplicationTests {

	@Test
	void contextLoads() {
	}

}
