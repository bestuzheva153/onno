package ru.bestuzheva153.subscriptions.ui;

import org.springframework.stereotype.Component;

import ru.bestuzheva153.subscriptions.model.Client;
import su.onno.ui.EntityConfigBuilder;
import su.onno.ui.EntityView;
import su.onno.ui.ListSpec;

@Component
public class ClientView implements EntityView<Client> {

    @Override
    public Class<Client> entity() {
        return Client.class;
    }

    @Override
    public void list(ListSpec<Client> list) {
        list.title("Клиенты");
        list.columns("code", "description", "status", "email", "phone", "registrationDate");
        list.sortBy("code");
    }

    @Override
    public void fields(EntityConfigBuilder<Client> fields) {
        fields.icon("user");
        fields.field(Client::getDescription).label("Имя").order(10).width("half").placeholder("Название клиента");
        fields.field(Client::getStatus).order(20).width("half");
        fields.field(Client::getEmail).order(30).width("half").placeholder("client@example.com");
        fields.field(Client::getPhone).order(40).width("half").placeholder("+7 ...");
        fields.field(Client::getRegistrationDate).order(50).width("half").format("date");
    }
}
