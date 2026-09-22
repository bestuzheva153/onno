package ru.bestuzheva153.subscriptions.config;

import java.math.BigDecimal;

import org.jdbi.v3.core.Jdbi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ru.bestuzheva153.subscriptions.model.Client;
import ru.bestuzheva153.subscriptions.model.SubscriptionRuntime;
import su.onno.model.MovementType;
import su.onno.types.Ref;
import su.onno.types.RefResolver;

@Configuration
public class RuntimeConfiguration {

    @Bean
    Object subscriptionRuntimeConfigurer(RefResolver refResolver, Jdbi jdbi) {
        SubscriptionRuntime.configure(refResolver::resolve, client -> balance(jdbi, client));
        return new Object();
    }

    private static BigDecimal balance(Jdbi jdbi, Ref<Client> client) {
        return jdbi.withHandle(handle -> handle.createQuery("""
                        select coalesce(sum(
                            case when movement_type = :receipt then amount else -amount end
                        ), 0)
                        from personal_accounts
                        where active = true and client = :client
                        """)
                .bind("receipt", MovementType.RECEIPT.name())
                .bind("client", client.id())
                .mapTo(BigDecimal.class)
                .one());
    }
}
