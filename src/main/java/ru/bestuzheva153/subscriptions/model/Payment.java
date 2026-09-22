package ru.bestuzheva153.subscriptions.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import su.onno.annotations.Attribute;
import su.onno.annotations.Document;
import su.onno.lifecycle.OnFillingHandler;
import su.onno.lifecycle.Postable;
import su.onno.model.DocumentObject;
import su.onno.posting.PostingContext;
import su.onno.rules.BusinessRule;
import su.onno.rules.Validated;
import su.onno.types.Ref;

@Document(name = "Payments", title = "Платежи", tableName = "payments", numberPrefix = "PAY-", context = "Subscriptions")
public class Payment extends DocumentObject implements Postable, OnFillingHandler, Validated {

    @Attribute(displayName = "Клиент", required = true)
    private Ref<Client> client;

    @Attribute(displayName = "Сумма", required = true, precision = 15, scale = 2, min = 0.01)
    private BigDecimal amount = BigDecimal.ZERO;

    @Attribute(displayName = "Способ оплаты", required = true)
    private PaymentMethod paymentMethod = PaymentMethod.CARD;

    @Override
    public void onFilling() {
        if (getDate() == null) {
            setDate(LocalDateTime.now());
        }
    }

    @Override
    public void handlePosting(PostingContext context) {
        context.movements(PersonalAccount.class).addReceipt(movement -> {
            movement.setClient(client);
            movement.setAmount(amount);
        });
    }

    @Override
    public List<BusinessRule> rules() {
        return List.of(
                new BusinessRule("paymentClientRequired", "client", "У платежа должен быть клиент", () -> client != null),
                new BusinessRule("paymentAmountPositive", "amount", "Сумма платежа должна быть больше нуля",
                        () -> amount != null && amount.signum() > 0),
                new BusinessRule("paymentMethodRequired", "paymentMethod", "Нужно указать способ оплаты", () -> paymentMethod != null)
        );
    }

    public Ref<Client> getClient() {
        return client;
    }

    public void setClient(Ref<Client> client) {
        this.client = client;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
