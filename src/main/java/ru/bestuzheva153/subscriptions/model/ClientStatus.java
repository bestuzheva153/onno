package ru.bestuzheva153.subscriptions.model;

import su.onno.annotations.EnumLabel;
import su.onno.annotations.Enumeration;

@Enumeration(name = "ClientStatuses", title = "Статусы клиентов")
public enum ClientStatus {
    @EnumLabel(value = "Новый", color = "#64748b")
    NEW,

    @EnumLabel(value = "Активный", color = "#16a34a")
    ACTIVE,

    @EnumLabel(value = "Заблокирован", color = "#dc2626")
    BLOCKED
}
