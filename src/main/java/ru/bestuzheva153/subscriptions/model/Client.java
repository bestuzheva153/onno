package ru.bestuzheva153.subscriptions.model;

import java.time.LocalDate;
import java.util.List;

import su.onno.annotations.Attribute;
import su.onno.annotations.Catalog;
import su.onno.model.CatalogObject;
import su.onno.rules.BusinessRule;
import su.onno.rules.Validated;

@Catalog(name = "Clients", title = "Клиенты", tableName = "clients", codePrefix = "CL-", context = "Subscriptions")
public class Client extends CatalogObject implements Validated {

    @Attribute(displayName = "Статус", required = true)
    private ClientStatus status = ClientStatus.NEW;

    @Attribute(displayName = "E-mail", required = true, email = true, length = 120)
    private String email;

    @Attribute(displayName = "Телефон", required = true, length = 32)
    private String phone;

    @Attribute(displayName = "Дата регистрации", required = true)
    private LocalDate registrationDate = LocalDate.now();

    @Override
    public List<BusinessRule> rules() {
        return List.of(
                new BusinessRule("clientEmailRequired", "email", "У клиента должен быть e-mail", () -> email != null && !email.isBlank()),
                new BusinessRule("clientPhoneRequired", "phone", "У клиента должен быть телефон", () -> phone != null && !phone.isBlank())
        );
    }

    public ClientStatus getStatus() {
        return status;
    }

    public void setStatus(ClientStatus status) {
        this.status = status;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }
}
