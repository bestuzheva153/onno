package ru.bestuzheva153.subscriptions.model;

import java.math.BigDecimal;

import su.onno.annotations.Attribute;
import su.onno.model.TabularSectionRow;
import su.onno.types.Ref;

public class SubscriptionLine extends TabularSectionRow {

    @Attribute(displayName = "Тариф", required = true)
    private Ref<Tariff> tariff;

    @Attribute(displayName = "Число периодов", required = true, min = 1)
    private Integer periods = 1;

    @Attribute(displayName = "Цена", required = true, precision = 15, scale = 2, min = 0)
    private BigDecimal price = BigDecimal.ZERO;

    @Attribute(displayName = "Сумма", required = true, precision = 15, scale = 2, min = 0)
    private BigDecimal amount = BigDecimal.ZERO;

    public Ref<Tariff> getTariff() {
        return tariff;
    }

    public void setTariff(Ref<Tariff> tariff) {
        this.tariff = tariff;
    }

    public Integer getPeriods() {
        return periods;
    }

    public void setPeriods(Integer periods) {
        this.periods = periods;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
