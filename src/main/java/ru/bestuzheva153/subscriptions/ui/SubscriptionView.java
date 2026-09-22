package ru.bestuzheva153.subscriptions.ui;

import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import ru.bestuzheva153.subscriptions.model.Client;
import ru.bestuzheva153.subscriptions.model.Subscription;
import ru.bestuzheva153.subscriptions.model.SubscriptionLine;
import ru.bestuzheva153.subscriptions.model.SubscriptionStatus;
import ru.bestuzheva153.subscriptions.model.Tariff;
import su.onno.ui.ActionContext;
import su.onno.ui.ActionResult;
import su.onno.ui.ActionScope;
import su.onno.ui.ActionSpec;
import su.onno.ui.ActionToast;
import su.onno.ui.EntityConfigBuilder;
import su.onno.ui.EntityView;
import su.onno.ui.InputType;
import su.onno.ui.ListSpec;

@Component
public class SubscriptionView implements EntityView<Subscription> {

    private final Jdbi jdbi;

    public SubscriptionView(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public Class<Subscription> entity() {
        return Subscription.class;
    }

    @Override
    public void list(ListSpec<Subscription> list) {
        list.title("Подписки");
        list.columns("number", "date", "client", "status", "startDate", "endDate", "totalAmount", "posted");
        list.sortBy("date", true);
    }

    @Override
    public void fields(EntityConfigBuilder<Subscription> fields) {
        fields.icon("badge-check");
        fields.field(Subscription::getDate).order(10).width("half").format("datetime");
        fields.refField(Subscription::getClient).order(20).width("half").refSecondary(Client::getEmail);
        fields.field(Subscription::getStatus).order(30).width("third");
        fields.field(Subscription::getStartDate).order(40).width("third").format("date");
        fields.field(Subscription::getEndDate).order(50).width("third").format("date");
        fields.field(Subscription::getTotalAmount).order(60).width("half").format("money");
        fields.field(Subscription::getCancellationReason).order(70).width("wide");

        fields.rowRefField(Subscription::getLines, SubscriptionLine::getTariff)
                .order(10)
                .width("wide")
                .refSecondary(Tariff::getPricePerPeriod);
        fields.rowField(Subscription::getLines, SubscriptionLine::getPeriods).order(20).width("narrow");
        fields.rowField(Subscription::getLines, SubscriptionLine::getPrice).order(30).width("narrow").format("money");
        fields.rowField(Subscription::getLines, SubscriptionLine::getAmount).order(40).width("narrow").format("money");
    }

    @Override
    public void actions(ActionSpec actions) {
        actions.action("cancelSubscription")
                .label("Отменить")
                .icon("x-circle")
                .color("#dc2626")
                .scope(ActionScope.DETAIL)
                .form(form -> form
                        .title("Отмена подписки")
                        .submitLabel("Отменить")
                        .input("reason")
                        .label("Причина")
                        .type(InputType.TEXTAREA)
                        .required())
                .handler(this::cancelSubscription);

        actions.action("cancelSubscriptionFromList")
                .label("Отменить")
                .icon("x-circle")
                .color("#dc2626")
                .scope(ActionScope.ROW)
                .visibleWhen(row -> row.enumValue("status", SubscriptionStatus.class) != SubscriptionStatus.CANCELLED)
                .enabledWhen(row -> !row.bool("posted"))
                .form(form -> form
                        .title("Отмена подписки")
                        .submitLabel("Отменить")
                        .input("reason")
                        .label("Причина")
                        .type(InputType.TEXTAREA)
                        .required())
                .handler(this::cancelSubscription);
    }

    private ActionResult cancelSubscription(ActionContext context) {
        String reason = context.input("reason");
        if (reason == null || reason.isBlank()) {
            return ActionResult.toast(ActionToast.warning("Укажите причину отмены"));
        }

        int updated = jdbi.withHandle(handle -> handle.createUpdate("""
                        update subscriptions
                        set status = :status,
                            cancellation_reason = :reason
                        where id = :id
                          and posted = false
                          and status <> :status
                        """)
                .bind("status", SubscriptionStatus.CANCELLED.name())
                .bind("reason", reason.trim())
                .bind("id", context.id())
                .execute());

        if (updated == 0) {
            return ActionResult.toast(ActionToast.warning("Проведенную или уже отмененную подписку отменить нельзя"));
        }

        return ActionResult.refresh(ActionToast.success("Подписка отменена"));
    }
}
