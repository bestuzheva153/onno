package ru.bestuzheva153.subscriptions.ui;

import org.springframework.stereotype.Component;

import ru.bestuzheva153.subscriptions.model.Tariff;
import su.onno.ui.EntityConfigBuilder;
import su.onno.ui.EntityView;
import su.onno.ui.ListSpec;

@Component
public class TariffView implements EntityView<Tariff> {

    @Override
    public Class<Tariff> entity() {
        return Tariff.class;
    }

    @Override
    public void list(ListSpec<Tariff> list) {
        list.title("Тарифы");
        list.columns("code", "description", "pricePerPeriod", "periodDays", "availableForConnection");
        list.sortBy("code");
    }

    @Override
    public void fields(EntityConfigBuilder<Tariff> fields) {
        fields.icon("badge-dollar-sign");
        fields.field(Tariff::getDescription).label("Название").order(10).width("half");
        fields.field(Tariff::getPricePerPeriod).order(20).width("half").format("money");
        fields.field(Tariff::getPeriodDays).order(30).width("half").hint("Продолжительность одного оплаченного периода");
        fields.field(Tariff::getAvailableForConnection).order(40).width("half");
    }
}
