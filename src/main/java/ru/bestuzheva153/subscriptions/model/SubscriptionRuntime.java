package ru.bestuzheva153.subscriptions.model;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Function;

import su.onno.types.Ref;

public final class SubscriptionRuntime {

    private static Function<Ref<Tariff>, Optional<Tariff>> tariffResolver = ref -> Optional.empty();
    private static Function<Ref<Client>, BigDecimal> accountBalanceProvider = ref -> BigDecimal.ZERO;

    private SubscriptionRuntime() {
    }

    public static void configure(
            Function<Ref<Tariff>, Optional<Tariff>> tariffProvider,
            Function<Ref<Client>, BigDecimal> balanceProvider) {
        tariffResolver = tariffProvider == null ? ref -> Optional.empty() : tariffProvider;
        accountBalanceProvider = balanceProvider == null ? ref -> BigDecimal.ZERO : balanceProvider;
    }

    public static Optional<Tariff> resolveTariff(Ref<Tariff> tariff) {
        return tariff == null ? Optional.empty() : tariffResolver.apply(tariff);
    }

    public static BigDecimal accountBalance(Ref<Client> client) {
        return client == null ? BigDecimal.ZERO : accountBalanceProvider.apply(client);
    }
}
