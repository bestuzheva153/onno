package ru.bestuzheva153.subscriptions.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import su.onno.annotations.Attribute;
import su.onno.annotations.Document;
import su.onno.annotations.TabularSection;
import su.onno.lifecycle.BeforePostHandler;
import su.onno.lifecycle.BeforeWriteHandler;
import su.onno.lifecycle.OnFillingHandler;
import su.onno.lifecycle.Postable;
import su.onno.model.DocumentObject;
import su.onno.posting.PostingContext;
import su.onno.rules.BusinessRule;
import su.onno.rules.Validated;
import su.onno.types.Ref;
import su.onno.validation.ValidationException;

@Document(name = "Subscriptions", title = "Подписки", tableName = "subscriptions", numberPrefix = "SUB-", context = "Subscriptions")
public class Subscription extends DocumentObject implements Postable, OnFillingHandler, BeforeWriteHandler, BeforePostHandler, Validated {

    @Attribute(displayName = "Клиент", required = true)
    private Ref<Client> client;

    @Attribute(displayName = "Статус", required = true)
    private SubscriptionStatus status = SubscriptionStatus.DRAFT;

    @Attribute(displayName = "Дата начала", required = true)
    private LocalDate startDate;

    @Attribute(displayName = "Дата окончания", required = true)
    private LocalDate endDate;

    @Attribute(displayName = "Итого", required = true, precision = 15, scale = 2, min = 0)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Attribute(displayName = "Причина отмены", length = 500)
    private String cancellationReason;

    @TabularSection(name = "lines")
    private List<SubscriptionLine> lines = new ArrayList<>();

    @Override
    public void onFilling() {
        fillDefaults();
    }

    @Override
    public void beforeWrite() {
        fillDefaults();
        recalculate();
    }

    @Override
    public void beforePost() {
        recalculate();
        if (status == SubscriptionStatus.CANCELLED) {
            return;
        }
        BigDecimal balance = SubscriptionRuntime.accountBalance(client);
        if (balance.compareTo(totalAmount) < 0) {
            throw new ValidationException("На лицевом счете клиента недостаточно денег для проведения подписки");
        }
    }

    @Override
    public void handlePosting(PostingContext context) {
        recalculate();
        if (status == SubscriptionStatus.CANCELLED) {
            return;
        }

        context.movements(PersonalAccount.class).addExpense(movement -> {
            movement.setClient(client);
            movement.setAmount(totalAmount);
        });

        for (SubscriptionLine line : lines) {
            context.movements(RevenueByTariff.class).addReceipt(movement -> {
                movement.setTariff(line.getTariff());
                movement.setClient(client);
                movement.setAmount(line.getAmount());
                movement.setPeriods(line.getPeriods());
            });
        }

    }

    @Override
    public List<BusinessRule> rules() {
        return List.of(
                new BusinessRule("subscriptionClientRequired", "client", "У подписки должен быть клиент", () -> client != null),
                new BusinessRule("subscriptionHasLines", "В подписке должна быть хотя бы одна строка", () -> lines != null && !lines.isEmpty()),
                new BusinessRule("subscriptionPeriodsPositive", "Количество периодов в каждой строке должно быть больше нуля",
                        () -> lines != null && lines.stream().allMatch(line -> line.getPeriods() != null && line.getPeriods() > 0)),
                new BusinessRule("subscriptionTariffsAvailable", "Все тарифы должны быть доступны для подключения",
                        () -> lines != null && lines.stream()
                                .allMatch(line -> line.getTariff() != null
                                        && SubscriptionRuntime.resolveTariff(line.getTariff())
                                        .map(tariff -> Boolean.TRUE.equals(tariff.getAvailableForConnection()))
                                        .orElse(false)))
        );
    }

    public void recalculate() {
        BigDecimal total = BigDecimal.ZERO;
        int maxDays = 0;

        if (lines != null) {
            for (SubscriptionLine line : lines) {
                Tariff tariff = line.getTariff() == null
                        ? null
                        : SubscriptionRuntime.resolveTariff(line.getTariff()).orElse(null);
                BigDecimal price = tariff == null ? nvl(line.getPrice()) : nvl(tariff.getPricePerPeriod());
                int periods = line.getPeriods() == null ? 0 : line.getPeriods();
                BigDecimal amount = price.multiply(BigDecimal.valueOf(periods));

                line.setPrice(price);
                line.setAmount(amount);
                total = total.add(amount);

                if (tariff != null && tariff.getPeriodDays() != null) {
                    maxDays = Math.max(maxDays, tariff.getPeriodDays() * periods);
                }
            }
        }

        totalAmount = total;
        if (startDate != null && maxDays > 0) {
            endDate = startDate.plusDays(maxDays);
        }
    }

    private void fillDefaults() {
        if (getDate() == null) {
            setDate(LocalDateTime.now());
        }
        if (startDate == null) {
            startDate = getDate().toLocalDate();
        }
        if (status == null) {
            status = SubscriptionStatus.DRAFT;
        }
    }

    private static BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    public Ref<Client> getClient() {
        return client;
    }

    public void setClient(Ref<Client> client) {
        this.client = client;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public void setStatus(SubscriptionStatus status) {
        this.status = status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public List<SubscriptionLine> getLines() {
        return lines;
    }

    public void setLines(List<SubscriptionLine> lines) {
        this.lines = lines;
    }
}
