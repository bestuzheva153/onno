package ru.bestuzheva153.subscriptions.jobs;

import java.time.LocalDate;

import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import ru.bestuzheva153.subscriptions.model.SubscriptionStatus;
import su.onno.annotations.ScheduledJob;
import su.onno.jobs.BackgroundTask;

@Component
@ScheduledJob(name = "refreshSubscriptionStatuses", cron = "0 0 * * * *")
public class SubscriptionStatusJob implements BackgroundTask {

    private final Jdbi jdbi;

    public SubscriptionStatusJob(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public void execute() {
        LocalDate today = LocalDate.now();
        jdbi.useHandle(handle -> {
            handle.createUpdate("""
                            update subscriptions
                            set status = :expired
                            where deletion_mark = false
                              and posted = true
                              and status <> :cancelled
                              and end_date < :today
                            """)
                    .bind("expired", SubscriptionStatus.EXPIRED.name())
                    .bind("cancelled", SubscriptionStatus.CANCELLED.name())
                    .bind("today", today)
                    .execute();

            handle.createUpdate("""
                            update subscriptions
                            set status = :active
                            where deletion_mark = false
                              and posted = true
                              and status <> :cancelled
                              and start_date <= :today
                              and (end_date is null or end_date >= :today)
                            """)
                    .bind("active", SubscriptionStatus.ACTIVE.name())
                    .bind("cancelled", SubscriptionStatus.CANCELLED.name())
                    .bind("today", today)
                    .execute();
        });
    }
}
