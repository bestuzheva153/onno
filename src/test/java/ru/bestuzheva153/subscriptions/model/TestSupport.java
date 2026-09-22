package ru.bestuzheva153.subscriptions.model;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import org.mockito.ArgumentCaptor;

import su.onno.posting.PostingContext;
import su.onno.repository.RegisterRepository;
import su.onno.rules.BusinessRule;
import su.onno.types.Ref;

final class TestSupport {

    private TestSupport() {
    }

    static Subscription subscription(Ref<Tariff> tariffRef, int periods) {
        Subscription subscription = new Subscription();
        subscription.setClient(Ref.of(Client.class, UUID.randomUUID()));
        subscription.getLines().add(line(tariffRef, periods));
        return subscription;
    }

    static SubscriptionLine line(Ref<Tariff> tariffRef, int periods) {
        SubscriptionLine line = new SubscriptionLine();
        line.setTariff(tariffRef);
        line.setPeriods(periods);
        return line;
    }

    static Tariff tariff(String price, int days, boolean available) {
        Tariff tariff = new Tariff();
        tariff.setPricePerPeriod(new BigDecimal(price));
        tariff.setPeriodDays(days);
        tariff.setAvailableForConnection(available);
        return tariff;
    }

    static void configureResolver(Map<Ref<Tariff>, Tariff> tariffs, BigDecimal balance) {
        SubscriptionRuntime.configure(tariff -> java.util.Optional.ofNullable(tariffs.get(tariff)), client -> balance);
    }

    static BusinessRule rule(Subscription subscription, String name) {
        return subscription.rules().stream()
                .filter(rule -> rule.name().equals(name))
                .findFirst()
                .orElseThrow();
    }

    static BusinessRule rule(Payment payment, String name) {
        return payment.rules().stream()
                .filter(rule -> rule.name().equals(name))
                .findFirst()
                .orElseThrow();
    }

    @SuppressWarnings("unchecked")
    static PostingMocks postingMocks() {
        PostingContext context = mock(PostingContext.class);
        RegisterRepository<PersonalAccount> personalAccounts = mock(RegisterRepository.class);
        RegisterRepository<RevenueByTariff> revenues = mock(RegisterRepository.class);
        when(context.movements(PersonalAccount.class)).thenReturn(personalAccounts);
        when(context.movements(RevenueByTariff.class)).thenReturn(revenues);
        return new PostingMocks(
                context,
                personalAccounts,
                revenues,
                ArgumentCaptor.forClass(Consumer.class),
                ArgumentCaptor.forClass(Consumer.class)
        );
    }

    record PostingMocks(
            PostingContext context,
            RegisterRepository<PersonalAccount> personalAccounts,
            RegisterRepository<RevenueByTariff> revenues,
            ArgumentCaptor<Consumer<PersonalAccount>> personalAccountCaptor,
            ArgumentCaptor<Consumer<RevenueByTariff>> revenueCaptor) {
    }
}
