package ru.bestuzheva153.subscriptions.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static ru.bestuzheva153.subscriptions.model.TestSupport.configureResolver;
import static ru.bestuzheva153.subscriptions.model.TestSupport.line;
import static ru.bestuzheva153.subscriptions.model.TestSupport.postingMocks;
import static ru.bestuzheva153.subscriptions.model.TestSupport.subscription;
import static ru.bestuzheva153.subscriptions.model.TestSupport.tariff;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import ru.bestuzheva153.subscriptions.model.TestSupport.PostingMocks;
import su.onno.types.Ref;

class SubscriptionPostingTest {

    @AfterEach
    void resetRuntime() {
        SubscriptionRuntime.configure(null, null);
    }

    @Test
    void subscriptionPostingAddsExpenseAndRevenueWithoutChangingDocumentStatus() {
        Tariff tariff = tariff("120.00", 30, true);
        Ref<Tariff> tariffRef = Ref.of(Tariff.class, UUID.randomUUID());
        Ref<Client> clientRef = Ref.of(Client.class, UUID.randomUUID());
        configureResolver(Map.of(tariffRef, tariff), new BigDecimal("500.00"));

        Subscription subscription = new Subscription();
        subscription.setClient(clientRef);
        subscription.getLines().add(line(tariffRef, 2));
        PostingMocks mocks = postingMocks();

        subscription.handlePosting(mocks.context());

        verify(mocks.personalAccounts()).addExpense(mocks.personalAccountCaptor().capture());
        verify(mocks.revenues()).addReceipt(mocks.revenueCaptor().capture());

        PersonalAccount accountRecord = new PersonalAccount();
        mocks.personalAccountCaptor().getValue().accept(accountRecord);
        RevenueByTariff revenueRecord = new RevenueByTariff();
        mocks.revenueCaptor().getValue().accept(revenueRecord);

        assertThat(accountRecord.getClient()).isEqualTo(clientRef);
        assertThat(accountRecord.getAmount()).isEqualByComparingTo("240.00");
        assertThat(revenueRecord.getClient()).isEqualTo(clientRef);
        assertThat(revenueRecord.getTariff()).isEqualTo(tariffRef);
        assertThat(revenueRecord.getAmount()).isEqualByComparingTo("240.00");
        assertThat(revenueRecord.getPeriods()).isEqualTo(2);
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.DRAFT);
    }

    @Test
    void cancelledSubscriptionPostingDoesNotCreateMovements() {
        Tariff tariff = tariff("120.00", 30, true);
        Ref<Tariff> tariffRef = Ref.of(Tariff.class, UUID.randomUUID());
        configureResolver(Map.of(tariffRef, tariff), BigDecimal.ZERO);

        Subscription subscription = subscription(tariffRef, 1);
        subscription.setStatus(SubscriptionStatus.CANCELLED);
        PostingMocks mocks = postingMocks();

        subscription.handlePosting(mocks.context());

        verify(mocks.personalAccounts(), never()).addExpense(any());
        verify(mocks.revenues(), never()).addReceipt(any());
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.CANCELLED);
    }
}
