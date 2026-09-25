package ru.bestuzheva153.subscriptions;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:subscriptions-context;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "onno.schema.mode=apply"
})
class SubscriptionsApplicationContextTest {

    @Test
    void contextLoads() {
    }
}
