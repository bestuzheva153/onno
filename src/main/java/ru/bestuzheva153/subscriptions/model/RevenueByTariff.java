package ru.bestuzheva153.subscriptions.model;

import java.math.BigDecimal;

import su.onno.annotations.AccumulationRegister;
import su.onno.annotations.Dimension;
import su.onno.annotations.Resource;
import su.onno.model.AccumulationRecord;
import su.onno.model.AccumulationType;
import su.onno.types.Ref;

@AccumulationRegister(
        name = "RevenueByTariffs",
        title = "Выручка по тарифам",
        tableName = "revenue_by_tariffs",
        type = AccumulationType.TURNOVER,
        context = "Subscriptions")
public class RevenueByTariff extends AccumulationRecord {

    @Dimension(displayName = "Тариф")
    private Ref<Tariff> tariff;

    @Dimension(displayName = "Клиент")
    private Ref<Client> client;

    @Resource(displayName = "Сумма", precision = 15, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @Resource(displayName = "Периоды", precision = 15, scale = 0)
    private Integer periods = 0;

    public Ref<Tariff> getTariff() {
        return tariff;
    }

    public void setTariff(Ref<Tariff> tariff) {
        this.tariff = tariff;
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

    public Integer getPeriods() {
        return periods;
    }

    public void setPeriods(Integer periods) {
        this.periods = periods;
    }
}
