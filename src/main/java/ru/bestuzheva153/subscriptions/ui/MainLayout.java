package ru.bestuzheva153.subscriptions.ui;

import org.springframework.stereotype.Component;

import ru.bestuzheva153.subscriptions.model.Client;
import ru.bestuzheva153.subscriptions.model.Payment;
import ru.bestuzheva153.subscriptions.model.PersonalAccount;
import ru.bestuzheva153.subscriptions.model.RevenueByTariff;
import ru.bestuzheva153.subscriptions.model.Subscription;
import ru.bestuzheva153.subscriptions.model.Tariff;
import su.onno.ui.Layout;
import su.onno.ui.LayoutSpec;
import su.onno.ui.NavStyle;

@Component
public class MainLayout implements Layout {

    @Override
    public void configure(LayoutSpec spec) {
        spec.title("Сервис подписок")
                .theme("light");
        spec.shell()
                .brand("Сервис подписок")
                .nav(NavStyle.SIDEBAR);

        spec.section("Справочники")
                .order(10)
                .icon("folder")
                .catalog(Client.class)
                .catalog(Tariff.class);

        spec.section("Документы")
                .order(20)
                .icon("file-text")
                .document(Payment.class)
                .document(Subscription.class);

        spec.section("Отчеты")
                .order(30)
                .icon("bar-chart-3")
                .register(PersonalAccount.class)
                .register(RevenueByTariff.class)
                .page("/dashboard", "Дашборд", "layout-dashboard");
    }
}
