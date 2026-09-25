package ru.bestuzheva153.subscriptions.ui;

import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

import ru.bestuzheva153.subscriptions.model.Client;
import ru.bestuzheva153.subscriptions.model.Subscription;
import ru.bestuzheva153.subscriptions.model.SubscriptionLine;
import ru.bestuzheva153.subscriptions.model.SubscriptionStatus;
import ru.bestuzheva153.subscriptions.model.Tariff;
import su.onno.metadata.MetadataRegistry;
import su.onno.posting.PostingService;
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
    private final PostingService postingService;
    private final String subscriptionTable;
    private final UUID cancelledStatusId;

    public SubscriptionView(
            Jdbi jdbi,
            PostingService postingService,
            MetadataRegistry metadataRegistry) {
        this.jdbi = jdbi;
        this.postingService = postingService;
        this.subscriptionTable = metadataRegistry.getDocumentDescriptor(Subscription.class).tableName();
        this.cancelledStatusId = metadataRegistry.getEnumerationDescriptor(SubscriptionStatus.class).values().stream()
                .filter(value -> value.name().equals(SubscriptionStatus.CANCELLED.name()))
                .findFirst()
                .orElseThrow()
                .id();
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
        fields.field(Subscription::getCancellationReason).order(70).width("wide").hideInForm();

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
                .visibleWhen(row -> row.enumValue("status", SubscriptionStatus.class) != SubscriptionStatus.CANCELLED)
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

        SubscriptionState subscription = loadSubscription(context.id());
        if (subscription == null) {
            return ActionResult.toast(ActionToast.warning("Подписка не найдена"));
        }
        if (cancelledStatusId.equals(subscription.statusId())) {
            return ActionResult.toast(ActionToast.warning("Подписка уже отменена"));
        }

        if (subscription.posted()) {
            Subscription document = new Subscription();
            document.setId(context.id());
            document.setDate(subscription.date());
            document.setPosted(true);
            postingService.unpost(document);
        }

        int updated = jdbi.withHandle(handle -> handle.createUpdate("""
                        update %s
                        set status = :status,
                            cancellation_reason = :reason
                        where _id = :id
                          and status <> :status
                        """.formatted(subscriptionTable))
                .bind("status", cancelledStatusId)
                .bind("reason", reason.trim())
                .bind("id", context.id())
                .execute());

        if (updated == 0) {
            return ActionResult.toast(ActionToast.warning("Проведенную или уже отмененную подписку отменить нельзя"));
        }

        return ActionResult.refresh(ActionToast.success("Подписка отменена"));
    }

    private SubscriptionState loadSubscription(UUID id) {
        return jdbi.withHandle(handle -> handle.createQuery("""
                        select _date, _posted, status
                        from %s
                        where _id = :id and _deletion_mark = false
                        """.formatted(subscriptionTable))
                .bind("id", id)
                .map((rs, ctx) -> new SubscriptionState(
                        rs.getObject("_date", LocalDateTime.class),
                        rs.getBoolean("_posted"),
                        rs.getObject("status", UUID.class)))
                .findOne()
                .orElse(null));
    }

    private record SubscriptionState(LocalDateTime date, boolean posted, UUID statusId) {
    }
}
