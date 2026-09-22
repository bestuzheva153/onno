package ru.bestuzheva153.subscriptions.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static ru.bestuzheva153.subscriptions.model.TestSupport.postingMocks;
import static ru.bestuzheva153.subscriptions.model.TestSupport.rule;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import ru.bestuzheva153.subscriptions.model.TestSupport.PostingMocks;
import su.onno.types.Ref;

class PaymentTest {

    @Test
    void paymentPostingAddsReceiptToPersonalAccountRegister() {
        Ref<Client> clientRef = Ref.of(Client.class, UUID.randomUUID());
        Payment payment = new Payment();
        payment.setClient(clientRef);
        payment.setAmount(new BigDecimal("300.00"));

        PostingMocks mocks = postingMocks();

        payment.handlePosting(mocks.context());

        verify(mocks.personalAccounts()).addReceipt(mocks.personalAccountCaptor().capture());
        PersonalAccount record = new PersonalAccount();
        mocks.personalAccountCaptor().getValue().accept(record);

        assertThat(record.getClient()).isEqualTo(clientRef);
        assertThat(record.getAmount()).isEqualByComparingTo("300.00");
    }

    @Test
    void paymentRulesRequireClientPositiveAmountAndPaymentMethod() {
        Payment payment = new Payment();
        payment.setAmount(BigDecimal.ZERO);
        payment.setPaymentMethod(null);

        assertThat(rule(payment, "paymentClientRequired").holds()).isFalse();
        assertThat(rule(payment, "paymentAmountPositive").holds()).isFalse();
        assertThat(rule(payment, "paymentMethodRequired").holds()).isFalse();
    }
}
