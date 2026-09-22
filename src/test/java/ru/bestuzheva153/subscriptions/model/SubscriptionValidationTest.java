package ru.bestuzheva153.subscriptions.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.bestuzheva153.subscriptions.model.TestSupport.configureResolver;
import static ru.bestuzheva153.subscriptions.model.TestSupport.line;
import static ru.bestuzheva153.subscriptions.model.TestSupport.rule;
import static ru.bestuzheva153.subscriptions.model.TestSupport.subscription;
import static ru.bestuzheva153.subscriptions.model.TestSupport.tariff;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import su.onno.types.Ref;
import su.onno.validation.ValidationException;

class SubscriptionValidationTest {

    @AfterEach
    void resetRuntime() {
        SubscriptionRuntime.configure(null, null);
    }

    @Test
    void refusesPostingWhenPersonalAccountBalanceIsInsufficient() {
        Tariff tariff = tariff("250.00", 30, true);
        Ref<Tariff> tariffRef = Ref.of(Tariff.class, UUID.randomUUID());
        configureResolver(Map.of(tariffRef, tariff), new BigDecimal("100.00"));

        Subscription subscription = subscription(tariffRef, 1);

        assertThatThrownBy(subscription::beforePost)
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void allowsPostingWhenPersonalAccountBalanceCoversTotalAmount() {
        Tariff tariff = tariff("250.00", 30, true);
        Ref<Tariff> tariffRef = Ref.of(Tariff.class, UUID.randomUUID());
        configureResolver(Map.of(tariffRef, tariff), new BigDecimal("250.00"));

        Subscription subscription = subscription(tariffRef, 1);

        subscription.beforePost();

        assertThat(subscription.getTotalAmount()).isEqualByComparingTo("250.00");
    }

    @Test
    void cancelledSubscriptionPassesPostingCheckWithoutBalance() {
        Tariff tariff = tariff("250.00", 30, true);
        Ref<Tariff> tariffRef = Ref.of(Tariff.class, UUID.randomUUID());
        configureResolver(Map.of(tariffRef, tariff), BigDecimal.ZERO);

        Subscription subscription = subscription(tariffRef, 1);
        subscription.setStatus(SubscriptionStatus.CANCELLED);

        subscription.beforePost();

        assertThat(subscription.getTotalAmount()).isEqualByComparingTo("250.00");
    }

    @Test
    void validatesRequiredClientRowsAndPositivePeriods() {
        Subscription empty = new Subscription();

        assertThat(rule(empty, "subscriptionClientRequired").holds()).isFalse();
        assertThat(rule(empty, "subscriptionHasLines").holds()).isFalse();

        Subscription subscription = new Subscription();
        subscription.setClient(Ref.of(Client.class, UUID.randomUUID()));
        subscription.getLines().add(line(Ref.of(Tariff.class, UUID.randomUUID()), 0));

        assertThat(rule(subscription, "subscriptionClientRequired").holds()).isTrue();
        assertThat(rule(subscription, "subscriptionHasLines").holds()).isTrue();
        assertThat(rule(subscription, "subscriptionPeriodsPositive").holds()).isFalse();
    }

    @Test
    void validatesThatResolvedTariffsAreAvailableForConnection() {
        Tariff unavailable = tariff("100.00", 30, false);
        Ref<Tariff> tariffRef = Ref.of(Tariff.class, UUID.randomUUID());
        configureResolver(Map.of(tariffRef, unavailable), new BigDecimal("1000.00"));

        Subscription subscription = subscription(tariffRef, 1);

        assertThat(rule(subscription, "subscriptionTariffsAvailable").holds()).isFalse();
    }

    @Test
    void validatesThatTariffReferenceCanBeResolved() {
        Ref<Tariff> missingTariffRef = Ref.of(Tariff.class, UUID.randomUUID());
        configureResolver(Map.of(), new BigDecimal("1000.00"));

        Subscription subscription = subscription(missingTariffRef, 1);

        assertThat(rule(subscription, "subscriptionTariffsAvailable").holds()).isFalse();
    }
}
