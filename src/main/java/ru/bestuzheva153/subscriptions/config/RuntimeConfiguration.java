package ru.bestuzheva153.subscriptions.config;

import java.math.BigDecimal;
import java.util.Optional;

import org.jdbi.v3.core.Jdbi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ru.bestuzheva153.subscriptions.model.Client;
import ru.bestuzheva153.subscriptions.model.PersonalAccount;
import ru.bestuzheva153.subscriptions.model.SubscriptionRuntime;
import ru.bestuzheva153.subscriptions.model.Tariff;
import su.onno.metadata.MetadataRegistry;
import su.onno.model.MovementType;
import su.onno.types.Ref;

@Configuration
public class RuntimeConfiguration {

    @Bean
    Object subscriptionRuntimeConfigurer(Jdbi jdbi, MetadataRegistry metadataRegistry) {
        String personalAccountsTable = metadataRegistry.getRegisterDescriptor(PersonalAccount.class).tableName();
        String tariffTable = metadataRegistry.getCatalogDescriptor(Tariff.class).tableName();
        SubscriptionRuntime.configure(
                tariff -> resolveTariff(jdbi, tariffTable, tariff),
                client -> balance(jdbi, personalAccountsTable, client));
        return new Object();
    }

    private static Optional<Tariff> resolveTariff(Jdbi jdbi, String tariffTable, Ref<Tariff> tariff) {
        return jdbi.withHandle(handle -> handle.createQuery("""
                        select price_per_period, period_days, available_for_connection
                        from %s
                        where _id = :id and _deletion_mark = false
                        """.formatted(tariffTable))
                .bind("id", tariff.id())
                .map((rs, ctx) -> {
                    Tariff resolved = new Tariff();
                    resolved.setPricePerPeriod(rs.getBigDecimal("price_per_period"));
                    resolved.setPeriodDays(rs.getInt("period_days"));
                    resolved.setAvailableForConnection(rs.getBoolean("available_for_connection"));
                    return resolved;
                })
                .findOne());
    }

    private static BigDecimal balance(Jdbi jdbi, String personalAccountsTable, Ref<Client> client) {
        return jdbi.withHandle(handle -> handle.createQuery("""
                        select coalesce(sum(
                            case when _movement_type = :receipt then amount else -amount end
                        ), 0)
                        from %s
                        where _active = true and client = :client
                        """.formatted(personalAccountsTable))
                .bind("receipt", MovementType.RECEIPT.name())
                .bind("client", client.id())
                .mapTo(BigDecimal.class)
                .one());
    }
}
