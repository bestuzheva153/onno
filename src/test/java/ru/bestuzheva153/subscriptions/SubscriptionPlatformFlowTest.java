package ru.bestuzheva153.subscriptions;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import ru.bestuzheva153.subscriptions.model.Client;
import ru.bestuzheva153.subscriptions.model.ClientStatus;
import ru.bestuzheva153.subscriptions.model.Payment;
import ru.bestuzheva153.subscriptions.model.PaymentMethod;
import ru.bestuzheva153.subscriptions.model.PersonalAccount;
import ru.bestuzheva153.subscriptions.model.RevenueByTariff;
import ru.bestuzheva153.subscriptions.model.Subscription;
import ru.bestuzheva153.subscriptions.model.SubscriptionRuntime;
import ru.bestuzheva153.subscriptions.model.SubscriptionStatus;
import ru.bestuzheva153.subscriptions.model.Tariff;
import su.onno.metadata.MetadataRegistry;
import su.onno.ui.ActionController;
import su.onno.ui.DocumentCommandService;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:subscriptions-flow;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "onno.schema.mode=apply"
})
class SubscriptionPlatformFlowTest {

    private static final Authentication ADMIN = new UsernamePasswordAuthenticationToken(
            "admin",
            "admin",
            List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

    @Autowired
    private Jdbi jdbi;

    @Autowired
    private MetadataRegistry metadataRegistry;

    @Autowired
    private DocumentCommandService documents;

    @Autowired
    private ActionController actions;

    @Test
    void subscriptionCanBeSavedPostedAndCancelledThroughPlatformServices() {
        UUID clientId = UUID.randomUUID();
        UUID tariffId = UUID.randomUUID();
        createCatalogData(clientId, tariffId);

        UUID paymentId = createPayment(clientId);
        documents.post(metadataRegistry.getDocumentDescriptor(Payment.class), paymentId, ADMIN);
        assertThat(accountBalance(clientId)).isEqualByComparingTo("500.00");

        UUID subscriptionId = createSubscription(clientId, tariffId);
        assertThat(subscriptionTotal(subscriptionId)).isEqualByComparingTo("240.00");

        documents.post(metadataRegistry.getDocumentDescriptor(Subscription.class), subscriptionId, ADMIN);
        assertThat(activeMovements(PersonalAccount.class, subscriptionId)).isEqualTo(1);
        assertThat(activeMovements(RevenueByTariff.class, subscriptionId)).isEqualTo(1);
        assertThat(accountBalance(clientId)).isEqualByComparingTo("260.00");

        actions.run(
                "documents",
                "Subscriptions",
                "cancelSubscription",
                subscriptionId,
                Map.of("inputs", Map.of("reason", "integration test")),
                ADMIN);

        assertThat(subscriptionPosted(subscriptionId)).isFalse();
        assertThat(subscriptionStatus(subscriptionId)).isEqualTo(enumId(SubscriptionStatus.class, SubscriptionStatus.CANCELLED.name()));
        assertThat(activeMovements(PersonalAccount.class, subscriptionId)).isZero();
        assertThat(activeMovements(RevenueByTariff.class, subscriptionId)).isZero();
        assertThat(accountBalance(clientId)).isEqualByComparingTo("500.00");
    }

    private void createCatalogData(UUID clientId, UUID tariffId) {
        String clientTable = metadataRegistry.getCatalogDescriptor(Client.class).tableName();
        String tariffTable = metadataRegistry.getCatalogDescriptor(Tariff.class).tableName();
        jdbi.useHandle(handle -> {
            handle.createUpdate("""
                            insert into %s (
                                _id, _code, _description, _deletion_mark, _is_folder, _version,
                                status, email, phone, registration_date
                            )
                            values (
                                :id, 'CL-TEST', 'Test client', false, false, 0,
                                :status, 'client@example.com', '+70000000000', :registrationDate
                            )
                            """.formatted(clientTable))
                    .bind("id", clientId)
                    .bind("status", enumId(ClientStatus.class, ClientStatus.ACTIVE.name()))
                    .bind("registrationDate", LocalDate.now())
                    .execute();

            handle.createUpdate("""
                            insert into %s (
                                _id, _code, _description, _deletion_mark, _is_folder, _version,
                                price_per_period, period_days, available_for_connection
                            )
                            values (
                                :id, 'TR-TEST', 'Test tariff', false, false, 0,
                                :price, 30, true
                            )
                            """.formatted(tariffTable))
                    .bind("id", tariffId)
                    .bind("price", new BigDecimal("120.00"))
                    .execute();
        });
    }

    private UUID createPayment(UUID clientId) {
        Map<String, Object> values = new HashMap<>();
        values.put("client", clientId);
        values.put("amount", new BigDecimal("500.00"));
        values.put("paymentMethod", enumId(PaymentMethod.class, PaymentMethod.CARD.name()));

        Map<String, Object> created = documents.create(
                metadataRegistry.getDocumentDescriptor(Payment.class),
                values,
                ADMIN);
        return id(created);
    }

    private UUID createSubscription(UUID clientId, UUID tariffId) {
        LocalDate today = LocalDate.now();
        Map<String, Object> values = new HashMap<>();
        values.put("client", clientId);
        values.put("status", enumId(SubscriptionStatus.class, SubscriptionStatus.DRAFT.name()));
        values.put("startDate", today);
        values.put("endDate", today);
        values.put("totalAmount", BigDecimal.ZERO);
        values.put("lines", List.of(Map.of(
                "tariff", tariffId,
                "periods", 2,
                "price", BigDecimal.ZERO,
                "amount", BigDecimal.ZERO
        )));

        Map<String, Object> created = documents.create(
                metadataRegistry.getDocumentDescriptor(Subscription.class),
                values,
                ADMIN);
        return id(created);
    }

    private UUID enumId(Class<? extends Enum<?>> type, String name) {
        return metadataRegistry.getEnumerationDescriptor(type).values().stream()
                .filter(value -> value.name().equals(name))
                .findFirst()
                .orElseThrow()
                .id();
    }

    private UUID id(Map<String, Object> entity) {
        Object id = entity.getOrDefault("id", entity.get("_id"));
        return id instanceof UUID uuid ? uuid : UUID.fromString(id.toString());
    }

    private BigDecimal accountBalance(UUID clientId) {
        return SubscriptionRuntime.accountBalance(su.onno.types.Ref.of(Client.class, clientId));
    }

    private BigDecimal subscriptionTotal(UUID subscriptionId) {
        return jdbi.withHandle(handle -> handle.createQuery("""
                        select total_amount
                        from %s
                        where _id = :id
                        """.formatted(metadataRegistry.getDocumentDescriptor(Subscription.class).tableName()))
                .bind("id", subscriptionId)
                .mapTo(BigDecimal.class)
                .one());
    }

    private UUID subscriptionStatus(UUID subscriptionId) {
        return jdbi.withHandle(handle -> handle.createQuery("""
                        select status
                        from %s
                        where _id = :id
                        """.formatted(metadataRegistry.getDocumentDescriptor(Subscription.class).tableName()))
                .bind("id", subscriptionId)
                .mapTo(UUID.class)
                .one());
    }

    private boolean subscriptionPosted(UUID subscriptionId) {
        return jdbi.withHandle(handle -> handle.createQuery("""
                        select _posted
                        from %s
                        where _id = :id
                        """.formatted(metadataRegistry.getDocumentDescriptor(Subscription.class).tableName()))
                .bind("id", subscriptionId)
                .mapTo(Boolean.class)
                .one());
    }

    private int activeMovements(Class<?> registerType, UUID documentId) {
        return jdbi.withHandle(handle -> handle.createQuery("""
                        select count(*)
                        from %s
                        where _document_ref = :documentId and _active = true
                        """.formatted(metadataRegistry.getRegisterDescriptor(registerType).tableName()))
                .bind("documentId", documentId)
                .mapTo(Integer.class)
                .one());
    }
}
