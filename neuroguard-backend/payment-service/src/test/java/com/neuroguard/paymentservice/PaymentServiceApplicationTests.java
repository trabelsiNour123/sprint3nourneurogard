package com.neuroguard.paymentservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "eureka.client.enabled=false",
        "eureka.instance.enabled=false",
        "spring.cloud.discovery.enabled=false"
})
@AutoConfigureTestDatabase(replace = Replace.ANY)
class PaymentServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
