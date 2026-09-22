package ru.bestuzheva153.subscriptions.model;

import su.onno.annotations.EnumLabel;
import su.onno.annotations.Enumeration;

@Enumeration(name = "SubscriptionStatuses", title = "Статусы подписок")
public enum SubscriptionStatus {
    @EnumLabel(value = "Черновик", color = "#64748b")
    DRAFT,

    @EnumLabel(value = "Активна", color = "#16a34a")
    ACTIVE,

    @EnumLabel(value = "Истекла", color = "#9333ea")
    EXPIRED,

    @EnumLabel(value = "Отменена", color = "#dc2626")
    CANCELLED
}
