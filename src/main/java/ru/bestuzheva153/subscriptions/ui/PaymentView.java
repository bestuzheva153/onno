package ru.bestuzheva153.subscriptions.ui;

import org.springframework.stereotype.Component;

import ru.bestuzheva153.subscriptions.model.Client;
import ru.bestuzheva153.subscriptions.model.Payment;
import su.onno.ui.EntityConfigBuilder;
import su.onno.ui.EntityView;
import su.onno.ui.ListSpec;

@Component
public class PaymentView implements EntityView<Payment> {

    @Override
    public Class<Payment> entity() {
        return Payment.class;
    }

    @Override
    public void list(ListSpec<Payment> list) {
        list.title("Платежи");
        list.columns("number", "date", "client", "amount", "paymentMethod", "posted");
        list.sortBy("date", true);
    }

    @Override
    public void fields(EntityConfigBuilder<Payment> fields) {
        fields.icon("wallet");
        fields.field(Payment::getDate).order(10).width("half").format("datetime");
        fields.refField(Payment::getClient).order(20).width("half").refSecondary(Client::getEmail);
        fields.field(Payment::getAmount).order(30).width("half").format("money");
        fields.field(Payment::getPaymentMethod).order(40).width("half");
    }
}
