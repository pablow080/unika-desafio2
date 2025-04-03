package com.example.pages;

import com.example.service.ClientService;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.spring.injection.annot.SpringBean;

public class ConfirmDeletePanel extends Panel {
    @SpringBean
    private ClientService clientService;

    public ConfirmDeletePanel(String id, Long clientId) {
        super(id);

        add(new AjaxLink<Void>("confirm") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                try {
                    clientService.delete(clientId);
                    getSession().info("Client deleted successfully");
                    findParent(ModalWindow.class).close(target);
                } catch (Exception e) {
                    error("Error deleting client: " + e.getMessage());
                }
            }
        });

        add(new AjaxLink<Void>("cancel") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                findParent(ModalWindow.class).close(target);
            }
        });
    }
}