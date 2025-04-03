package com.example.pages;

import com.example.model.Client;
import com.example.model.Address;
import com.example.service.ClientService;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.apache.wicket.validation.validator.StringValidator;

import java.time.LocalDate;
import java.util.Arrays;

public class ClientPanel extends Panel {
    @SpringBean
    private ClientService clientService;

    private final Form<Client> form;
    private final WebMarkupContainer individualFields;
    private final WebMarkupContainer companyFields;
    private final WebMarkupContainer addressContainer;

    public ClientPanel(String id, Client client) {
        super(id);

        if (client == null) {
            client = new Client();
            client.setPersonType(Client.PersonType.INDIVIDUAL);
        }

        form = new Form<>("clientForm", new CompoundPropertyModel<>(client));
        add(form);

        // Person Type Radio Choice
        RadioChoice<Client.PersonType> personType = new RadioChoice<>(
            "personType",
            Arrays.asList(Client.PersonType.values()),
            new ChoiceRenderer<>("name", "name")
        );
        personType.add(new AjaxFormComponentUpdatingBehavior("change") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.add(individualFields, companyFields);
            }
        });
        form.add(personType);

        // Individual Fields Container
        individualFields = new WebMarkupContainer("individualFields");
        individualFields.setOutputMarkupPlaceholderTag(true);
        individualFields.setVisible(client.getPersonType() == Client.PersonType.INDIVIDUAL);
        form.add(individualFields);

        TextField<String> cpfField = new TextField<>("cpf");
        cpfField.add(StringValidator.minimumLength(11));
        individualFields.add(cpfField);

        TextField<String> nameField = new TextField<>("name");
        nameField.add(StringValidator.minimumLength(3));
        individualFields.add(nameField);

        TextField<String> rgField = new TextField<>("rg");
        individualFields.add(rgField);

        DateTextField birthDateField = new DateTextField("birthDate");
        individualFields.add(birthDateField);

        // Company Fields Container
        companyFields = new WebMarkupContainer("companyFields");
        companyFields.setOutputMarkupPlaceholderTag(true);
        companyFields.setVisible(client.getPersonType() == Client.PersonType.COMPANY);
        form.add(companyFields);

        TextField<String> cnpjField = new TextField<>("cnpj");
        cnpjField.add(StringValidator.minimumLength(14));
        companyFields.add(cnpjField);

        TextField<String> companyNameField = new TextField<>("companyName");
        companyNameField.add(StringValidator.minimumLength(3));
        companyFields.add(companyNameField);

        TextField<String> stateRegistrationField = new TextField<>("stateRegistration");
        companyFields.add(stateRegistrationField);

        DateTextField foundationDateField = new DateTextField("foundationDate");
        companyFields.add(foundationDateField);

        // Common Fields
        TextField<String> emailField = new TextField<>("email");
        emailField.add(EmailAddressValidator.getInstance());
        form.add(emailField);

        CheckBox activeField = new CheckBox("active");
        form.add(activeField);

        // Addresses
        addressContainer = new WebMarkupContainer("addressContainer");
        addressContainer.setOutputMarkupId(true);
        form.add(addressContainer);

        ListView<Address> addressList = new ListView<Address>("addresses") {
            @Override
            protected void populateItem(ListItem<Address> item) {
                item.add(new TextField<>("street"));
                item.add(new TextField<>("number"));
                item.add(new TextField<>("zipCode"));
                item.add(new TextField<>("neighborhood"));
                item.add(new TextField<>("phone"));
                item.add(new TextField<>("city"));
                item.add(new TextField<>("state"));
                item.add(new CheckBox("mainAddress"));
                item.add(new TextField<>("complement"));
                
                item.add(new AjaxLink<Void>("removeAddress") {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        client.getAddresses().remove(item.getModelObject());
                        target.add(addressContainer);
                    }
                });
            }
        };
        addressContainer.add(addressList);

        form.add(new AjaxLink<Void>("addAddress") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                Address newAddress = new Address();
                client.addAddress(newAddress);
                target.add(addressContainer);
            }
        });

        // Submit Button
        form.add(new AjaxButton("submit") {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                try {
                    clientService.save(client);
                    getSession().info("Client saved successfully");
                    findParent(ModalWindow.class).close(target);
                } catch (Exception e) {
                    error("Error saving client: " + e.getMessage());
                    target.add(form);
                }
            }

            @Override
            protected void onError(AjaxRequestTarget target) {
                target.add(form);
            }
        });

        // Cancel Button
        form.add(new AjaxLink<Void>("cancel") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                findParent(ModalWindow.class).close(target);
            }
        });
    }
}