package ru.bestuzheva153.subscriptions.ui;

import org.springframework.stereotype.Component;

import ru.bestuzheva153.subscriptions.model.Client;
import ru.bestuzheva153.subscriptions.model.Subscription;
import ru.bestuzheva153.subscriptions.model.SubscriptionLine;
import ru.bestuzheva153.subscriptions.model.Tariff;
import su.onno.ui.EntityConfigBuilder;
import su.onno.ui.EntityView;
import su.onno.ui.ListSpec;

@Component
public class SubscriptionView implements EntityView<Subscription> {

    @Override
    public Class<Subscription> entity() {
        return Subscription.class;
    }

    @Override
    public void list(ListSpec<Subscription> list) {
        list.title("Подписки");
        list.columns("number", "date", "client", "status", "startDate", "endDate", "totalAmount", "posted");
        list.sortBy("date", true);
    }

    @Override
    public void fields(EntityConfigBuilder<Subscription> fields) {
        fields.icon("badge-check");
        fields.field(Subscription::getDate).order(10).width("half").format("datetime");
        fields.refField(Subscription::getClient).order(20).width("half").refSecondary(Client::getEmail);
        fields.field(Subscription::getStatus).order(30).width("third");
        fields.field(Subscription::getStartDate).order(40).width("third").format("date");
        fields.field(Subscription::getEndDate).order(50).width("third").format("date");
        fields.field(Subscription::getTotalAmount).order(60).width("half").format("money");

        fields.rowRefField(Subscription::getLines, SubscriptionLine::getTariff)
                .order(10)
                .width("wide")
                .refSecondary(Tariff::getPricePerPeriod);
        fields.rowField(Subscription::getLines, SubscriptionLine::getPeriods).order(20).width("narrow");
        fields.rowField(Subscription::getLines, SubscriptionLine::getPrice).order(30).width("narrow").format("money");
        fields.rowField(Subscription::getLines, SubscriptionLine::getAmount).order(40).width("narrow").format("money");
    }
}
