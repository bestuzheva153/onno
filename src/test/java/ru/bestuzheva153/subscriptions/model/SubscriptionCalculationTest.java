package ru.bestuzheva153.subscriptions.model;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.bestuzheva153.subscriptions.model.TestSupport.configureResolver;
import static ru.bestuzheva153.subscriptions.model.TestSupport.line;
import static ru.bestuzheva153.subscriptions.model.TestSupport.subscription;
import static ru.bestuzheva153.subscriptions.model.TestSupport.tariff;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import su.onno.types.Ref;

class SubscriptionCalculationTest {

    @AfterEach
    void resetRuntime() {
        SubscriptionRuntime.configure(null, null);
    }

    @Test
    void fillsDefaultDocumentDateStartDateAndDraftStatus() {
        Subscription subscription = new Subscription();
        subscription.setStatus(null);

        subscription.onFilling();

        assertThat(subscription.getDate()).isNotNull();
        assertThat(subscription.getStartDate()).isEqualTo(subscription.getDate().toLocalDate());
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.DRAFT);
    }

    @Test
    void recalculatesLinePriceAmountTotalAndEndDateFromTariff() {
        Tariff tariff = tariff("100.00", 30, true);
        Ref<Tariff> tariffRef = Ref.of(Tariff.class, UUID.randomUUID());
        configureResolver(Map.of(tariffRef, tariff), new BigDecimal("1000.00"));

        Subscription subscription = subscription(tariffRef, 3);
        subscription.setDate(LocalDateTime.of(2026, 1, 1, 10, 0));
        subscription.setStartDate(LocalDate.of(2026, 1, 5));

        subscription.beforeWrite();

        SubscriptionLine line = subscription.getLines().getFirst();
        assertThat(line.getPrice()).isEqualByComparingTo("100.00");
        assertThat(line.getAmount()).isEqualByComparingTo("300.00");
        assertThat(subscription.getTotalAmount()).isEqualByComparingTo("300.00");
        assertThat(subscription.getEndDate()).isEqualTo(LocalDate.of(2026, 4, 5));
    }

    @Test
    void recalculatesMultipleLinesAndUsesLongestTariffDurationForEndDate() {
        Tariff monthly = tariff("100.00", 30, true);
        Tariff yearly = tariff("500.00", 365, true);
        Ref<Tariff> monthlyRef = Ref.of(Tariff.class, UUID.randomUUID());
        Ref<Tariff> yearlyRef = Ref.of(Tariff.class, UUID.randomUUID());
        configureResolver(Map.of(monthlyRef, monthly, yearlyRef, yearly), new BigDecimal("2000.00"));

        Subscription subscription = new Subscription();
        subscription.setClient(Ref.of(Client.class, UUID.randomUUID()));
        subscription.setStartDate(LocalDate.of(2026, 2, 1));
        subscription.getLines().add(line(monthlyRef, 2));
        subscription.getLines().add(line(yearlyRef, 1));

        subscription.beforeWrite();

        assertThat(subscription.getTotalAmount()).isEqualByComparingTo("700.00");
        assertThat(subscription.getLines().get(0).getAmount()).isEqualByComparingTo("200.00");
        assertThat(subscription.getLines().get(1).getAmount()).isEqualByComparingTo("500.00");
        assertThat(subscription.getEndDate()).isEqualTo(LocalDate.of(2027, 2, 1));
    }
}
