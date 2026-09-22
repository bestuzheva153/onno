package ru.bestuzheva153.subscriptions.model;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.bestuzheva153.subscriptions.model.TestSupport.configureResolver;
import static ru.bestuzheva153.subscriptions.model.TestSupport.tariff;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import su.onno.types.Ref;

class SubscriptionRuntimeTest {

    @AfterEach
    void resetRuntime() {
        SubscriptionRuntime.configure(null, null);
    }

    @Test
    void resetsRuntimeResolverAndBalanceProvider() {
        Tariff tariff = tariff("100.00", 30, true);
        Ref<Tariff> tariffRef = Ref.of(Tariff.class, UUID.randomUUID());
        Ref<Client> clientRef = Ref.of(Client.class, UUID.randomUUID());
        configureResolver(Map.of(tariffRef, tariff), new BigDecimal("50.00"));

        assertThat(SubscriptionRuntime.resolveTariff(tariffRef)).contains(tariff);
        assertThat(SubscriptionRuntime.accountBalance(clientRef)).isEqualByComparingTo("50.00");

        SubscriptionRuntime.configure(null, null);

        assertThat(SubscriptionRuntime.resolveTariff(tariffRef)).isEmpty();
        assertThat(SubscriptionRuntime.accountBalance(clientRef)).isEqualByComparingTo("0.00");
    }
}
