package ru.bestuzheva153.subscriptions.ui;

import org.springframework.stereotype.Component;

import ru.bestuzheva153.subscriptions.model.Payment;
import ru.bestuzheva153.subscriptions.model.PersonalAccount;
import ru.bestuzheva153.subscriptions.model.RevenueByTariff;
import ru.bestuzheva153.subscriptions.model.Subscription;
import su.onno.ui.Page;
import su.onno.ui.PageBuilder;

@Component
public class DashboardPage implements Page {

    @Override
    public String route() {
        return "/dashboard";
    }

    @Override
    public void compose(PageBuilder page) {
        page.title("Дашборд")
                .subtitle("Остатки, выручка и последние документы");
        page.row(row -> row
                .col(col -> col.list(PersonalAccount.class))
                .col(col -> col.list(RevenueByTariff.class)));
        page.row(row -> row
                .col(col -> col.list(Payment.class))
                .col(col -> col.list(Subscription.class)));
    }
}
