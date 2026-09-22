package ru.bestuzheva153.subscriptions.model;

import java.math.BigDecimal;

import su.onno.annotations.AccumulationRegister;
import su.onno.annotations.Dimension;
import su.onno.annotations.Resource;
import su.onno.model.AccumulationRecord;
import su.onno.model.AccumulationType;
import su.onno.model.PostingOrder;
import su.onno.types.Ref;

@AccumulationRegister(
        name = "PersonalAccounts",
        title = "Лицевые счета",
        tableName = "personal_accounts",
        type = AccumulationType.BALANCE,
        postingOrder = PostingOrder.CHRONOLOGICAL,
        context = "Subscriptions")
public class PersonalAccount extends AccumulationRecord {

    @Dimension(displayName = "Клиент")
    private Ref<Client> client;

    @Resource(displayName = "Сумма", precision = 15, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

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
}
