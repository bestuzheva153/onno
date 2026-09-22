package ru.bestuzheva153.subscriptions.model;

import su.onno.annotations.EnumLabel;
import su.onno.annotations.Enumeration;

@Enumeration(name = "PaymentMethods", title = "Способы оплаты")
public enum PaymentMethod {
    @EnumLabel(value = "Банковская карта", color = "#2563eb")
    CARD,

    @EnumLabel(value = "Банковский перевод", color = "#475569")
    BANK_TRANSFER,

    @EnumLabel(value = "Наличные", color = "#ca8a04")
    CASH
}
