package ru.bestuzheva153.subscriptions.jobs;

import java.time.LocalDate;

import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import ru.bestuzheva153.subscriptions.model.Subscription;
import ru.bestuzheva153.subscriptions.model.SubscriptionStatus;
import su.onno.annotations.ScheduledJob;
import su.onno.jobs.BackgroundTask;
import su.onno.metadata.MetadataRegistry;

@Component
@ScheduledJob(name = "refreshSubscriptionStatuses", cron = "0 0 * * * *")
public class SubscriptionStatusJob implements BackgroundTask {

    private final Jdbi jdbi;
    private final String subscriptionTable;
    private final String subscriptionStatusTable;

    public SubscriptionStatusJob(Jdbi jdbi, MetadataRegistry metadataRegistry) {
        this.jdbi = jdbi;
        this.subscriptionTable = metadataRegistry.getDocumentDescriptor(Subscription.class).tableName();
        this.subscriptionStatusTable = metadataRegistry.getEnumerationDescriptor(SubscriptionStatus.class).tableName();
    }

    @Override
    public void execute() {
        LocalDate today = LocalDate.now();
        jdbi.useHandle(handle -> {
            handle.createUpdate("""
                            update %s
                            set status = (
                                    select _id
                                    from %s
                                    where _name = :expired
                                )
                            where _deletion_mark = false
                              and _posted = true
                              and status <> (
                                    select _id
                                    from %s
                                    where _name = :cancelled
                                )
                              and end_date < :today
                            """.formatted(subscriptionTable, subscriptionStatusTable, subscriptionStatusTable))
                    .bind("expired", SubscriptionStatus.EXPIRED.name())
                    .bind("cancelled", SubscriptionStatus.CANCELLED.name())
                    .bind("today", today)
                    .execute();

            handle.createUpdate("""
                            update %s
                            set status = (
                                    select _id
                                    from %s
                                    where _name = :active
                                )
                            where _deletion_mark = false
                              and _posted = true
                              and status <> (
                                    select _id
                                    from %s
                                    where _name = :cancelled
                                )
                              and start_date <= :today
                              and (end_date is null or end_date >= :today)
                            """.formatted(subscriptionTable, subscriptionStatusTable, subscriptionStatusTable))
                    .bind("active", SubscriptionStatus.ACTIVE.name())
                    .bind("cancelled", SubscriptionStatus.CANCELLED.name())
                    .bind("today", today)
                    .execute();
        });
    }
}
