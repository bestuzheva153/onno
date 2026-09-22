package ru.bestuzheva153.subscriptions.model;

import java.math.BigDecimal;
import java.util.List;

import su.onno.annotations.Attribute;
import su.onno.annotations.Catalog;
import su.onno.model.CatalogObject;
import su.onno.rules.BusinessRule;
import su.onno.rules.Validated;

@Catalog(name = "Tariffs", title = "Тарифы", tableName = "tariffs", codePrefix = "TR-", context = "Subscriptions")
public class Tariff extends CatalogObject implements Validated {

    @Attribute(displayName = "Цена за период", required = true, precision = 15, scale = 2, min = 0.01)
    private BigDecimal pricePerPeriod = BigDecimal.ZERO;

    @Attribute(displayName = "Длительность периода, дней", required = true, min = 1)
    private Integer periodDays = 30;

    @Attribute(displayName = "Доступен для подключения", required = true)
    private Boolean availableForConnection = true;

    @Override
    public List<BusinessRule> rules() {
        return List.of(
                new BusinessRule("tariffPricePositive", "pricePerPeriod", "Цена тарифа должна быть больше нуля",
                        () -> pricePerPeriod != null && pricePerPeriod.signum() > 0),
                new BusinessRule("tariffPeriodPositive", "periodDays", "Длительность периода должна быть больше нуля",
                        () -> periodDays != null && periodDays > 0)
        );
    }

    public BigDecimal getPricePerPeriod() {
        return pricePerPeriod;
    }

    public void setPricePerPeriod(BigDecimal pricePerPeriod) {
        this.pricePerPeriod = pricePerPeriod;
    }

    public Integer getPeriodDays() {
        return periodDays;
    }

    public void setPeriodDays(Integer periodDays) {
        this.periodDays = periodDays;
    }

    public Boolean getAvailableForConnection() {
        return availableForConnection;
    }

    public void setAvailableForConnection(Boolean availableForConnection) {
        this.availableForConnection = availableForConnection;
    }
}
