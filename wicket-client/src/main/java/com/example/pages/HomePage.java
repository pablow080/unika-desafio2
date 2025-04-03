package com.example.pages;

import com.example.model.Client;
import com.example.service.ClientService;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

public class HomePage extends WebPage {
    @SpringBean
    private ClientService clientService;

    private final ModalWindow clientModal;
    private final ModalWindow confirmDelete;
    private final FeedbackPanel feedbackPanel;

    public HomePage() {
        // Initialize components
        feedbackPanel = new FeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);
        add(feedbackPanel);

        clientModal = new ModalWindow("clientModal");
        clientModal.setTitle(Model.of("Client Details"));
        clientModal.setInitialWidth(600);
        clientModal.setInitialHeight(400);
        add(clientModal);

        confirmDelete = new ModalWindow("confirmDelete");
        confirmDelete.setTitle(Model.of("Confirm Delete"));
        confirmDelete.setInitialWidth(400);
        confirmDelete.setInitialHeight(200);
        add(confirmDelete);

        // Add new client button
        add(new AjaxLink<Void>("newClientBtn") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                clientModal.setContent(new ClientPanel(clientModal.getContentId(), null));
                clientModal.show(target);
            }
        });

        // Add client list
        DataView<Client> clients = new DataView<Client>("clients", 
            new ListDataProvider<>(clientService.findAll())) {
            @Override
            protected void populateItem(Item<Client> item) {
                Client client = item.getModelObject();
                item.add(new Label("personType", client.getPersonType().toString()));
                item.add(new Label("name", client.getPersonType() == Client.PersonType.INDIVIDUAL ? 
                    client.getName() : client.getCompanyName()));
                item.add(new Label("document", client.getPersonType() == Client.PersonType.INDIVIDUAL ? 
                    client.getCpf() : client.getCnpj()));
                item.add(new Label("email", client.getEmail()));
                
                Label status = new Label("status", client.isActive() ? "Active" : "Inactive");
                status.add(new AttributeModifier("class", 
                    client.isActive() ? "badge badge-success" : "badge badge-danger"));
                item.add(status);

                // Add action buttons
                item.add(new AjaxLink<Void>("editBtn") {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        clientModal.setContent(new ClientPanel(clientModal.getContentId(), client));
                        clientModal.show(target);
                    }
                });

                item.add(new AjaxLink<Void>("deleteBtn") {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        confirmDelete.setContent(new ConfirmDeletePanel(confirmDelete.getContentId(), 
                            client.getId()));
                        confirmDelete.show(target);
                    }
                });

                item.add(new AjaxLink<Void>("pdfBtn") {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        // TODO: Implement PDF generation
                    }
                });

                item.add(new AjaxLink<Void>("excelBtn") {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        // TODO: Implement Excel export
                    }
                });
            }
        };
        clients.setItemsPerPage(10);
        add(clients);

        add(new PagingNavigator("navigator", clients));
    }
}