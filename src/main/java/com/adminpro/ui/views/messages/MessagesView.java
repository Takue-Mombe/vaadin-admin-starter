package com.adminpro.ui.views.messages;

import com.adminpro.application.MessageService;
import com.adminpro.domain.SupportMessage;
import com.adminpro.ui.components.PageHeader;
import com.adminpro.ui.layout.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Route(value = "messages", layout = MainLayout.class)
@PageTitle("Messages | Arc Admin")
@PermitAll
public class MessagesView extends Div {

    private static final List<String> STATUS_OPTIONS = List.of("UNREAD", "OPEN", "CLOSED");
    private static final List<String> CHANNEL_OPTIONS = List.of("Email", "Chat", "Phone", "Webhook");
    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm");

    private final MessageService messageService;

    private final Grid<SupportMessage> grid = new Grid<>(SupportMessage.class, false);
    private final TextField searchField = new TextField();
    private final Select<String> statusFilter = new Select<>();

    private final Span totalMessagesValue = new Span("0");
    private final Span unreadMessagesValue = new Span("0");
    private final Span openMessagesValue = new Span("0");

    public MessagesView(MessageService messageService) {
        this.messageService = messageService;
        addClassName("page-content");
        setWidthFull();

        PageHeader header = new PageHeader("Messages", "Manage inbound support and customer conversations");

        Button composeButton = new Button("Compose", VaadinIcon.PLUS.create());
        composeButton.addClassNames("btn", "btn-primary");
        composeButton.addClickListener(event -> openDialog(new SupportMessage()));

        header.addAction(composeButton);

        add(header);
        add(buildStats());
        add(buildToolbar());
        add(buildGridCard());

        refreshSummary();
        refreshGrid();
    }

    private Div buildStats() {
        Div stats = new Div();
        stats.addClassNames("stats", "stats--three");
        stats.add(buildStat("Total Messages", totalMessagesValue, "all conversations", "neu"));
        stats.add(buildStat("Unread", unreadMessagesValue, "needs first response", "up"));
        stats.add(buildStat("Open", openMessagesValue, "in progress", "amber"));
        return stats;
    }

    private Div buildStat(String label, Span value, String delta, String tone) {
        Div stat = new Div();
        stat.addClassName("stat");

        Span labelEl = new Span(label);
        labelEl.addClassName("stat-label");

        value.addClassName("stat-val");

        Span deltaEl = new Span(delta);
        deltaEl.addClassNames("stat-delta", tone);

        stat.add(labelEl, value, deltaEl);
        return stat;
    }

    private Div buildToolbar() {
        Div card = new Div();
        card.addClassNames("card", "toolbar-card");

        Div body = new Div();
        body.addClassNames("card-b", "toolbar-row");

        searchField.setPlaceholder("Search sender, subject, or channel");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.addClassName("toolbar-search");
        searchField.addValueChangeListener(event -> refreshGrid());

        statusFilter.setLabel("Status");
        statusFilter.setItems(Arrays.asList("ALL", "UNREAD", "OPEN", "CLOSED"));
        statusFilter.setValue("ALL");
        statusFilter.addClassName("toolbar-select");
        statusFilter.addValueChangeListener(event -> refreshGrid());

        body.add(searchField, statusFilter);
        card.add(body);
        return card;
    }

    private Div buildGridCard() {
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.addClassName("table-grid");
        grid.setWidthFull();

        grid.addColumn(SupportMessage::getSenderName).setHeader("Sender").setAutoWidth(true);
        grid.addColumn(SupportMessage::getSubject).setHeader("Subject").setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(SupportMessage::getChannel).setHeader("Channel").setAutoWidth(true);
        grid.addColumn(message -> message.getReceivedAt().format(DATE_TIME_FMT)).setHeader("Received").setAutoWidth(true);
        grid.addComponentColumn(message -> buildStatusBadge(message.getStatus())).setHeader("Status").setAutoWidth(true);
        grid.addComponentColumn(this::buildRowActions).setHeader("").setAutoWidth(true).setFlexGrow(0);

        Div card = new Div();
        card.addClassName("card");

        Div body = new Div();
        body.addClassName("card-b");
        body.add(grid);

        card.add(body);
        return card;
    }

    private Span buildStatusBadge(String status) {
        Span badge = new Span(status);
        badge.addClassName("status-pill");

        if ("UNREAD".equalsIgnoreCase(status)) {
            badge.addClassName("status-pill--amber");
        } else if ("OPEN".equalsIgnoreCase(status)) {
            badge.addClassName("status-pill--green");
        } else {
            badge.addClassName("status-pill--gray");
        }
        return badge;
    }

    private Div buildRowActions(SupportMessage message) {
        Div actions = new Div();
        actions.addClassName("table-actions");

        Button manage = new Button("Manage");
        manage.addClassNames("btn", "btn-outline", "btn-sm");
        manage.addClickListener(event -> openDialog(message));

        Button delete = new Button("Delete");
        delete.addClassNames("btn", "btn-danger", "btn-sm");
        delete.addClickListener(event -> confirmDelete(message));

        actions.add(manage, delete);
        return actions;
    }

    private void openDialog(SupportMessage message) {
        boolean isNew = message.getId() == null;

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(isNew ? "Create Message" : "Edit Message");
        dialog.setWidth("580px");
        dialog.setCloseOnOutsideClick(false);

        TextField senderName = new TextField("Sender Name");
        senderName.setWidthFull();

        EmailField senderEmail = new EmailField("Sender Email");
        senderEmail.setWidthFull();

        TextField subject = new TextField("Subject");
        subject.setWidthFull();

        TextArea preview = new TextArea("Preview");
        preview.setWidthFull();

        Select<String> channel = new Select<>();
        channel.setLabel("Channel");
        channel.setItems(CHANNEL_OPTIONS);
        channel.setWidthFull();

        Select<String> status = new Select<>();
        status.setLabel("Status");
        status.setItems(STATUS_OPTIONS);
        status.setWidthFull();

        DateTimePicker receivedAt = new DateTimePicker("Received At");
        receivedAt.setWidthFull();

        Div form = new Div(senderName, senderEmail, subject, preview, channel, status, receivedAt);
        form.getStyle().set("display", "grid").set("gap", "var(--lumo-space-s)");

        BeanValidationBinder<SupportMessage> binder = new BeanValidationBinder<>(SupportMessage.class);
        binder.forField(senderName).asRequired().bind(SupportMessage::getSenderName, SupportMessage::setSenderName);
        binder.forField(senderEmail).bind(SupportMessage::getSenderEmail, SupportMessage::setSenderEmail);
        binder.forField(subject).asRequired().bind(SupportMessage::getSubject, SupportMessage::setSubject);
        binder.forField(preview).bind(SupportMessage::getPreview, SupportMessage::setPreview);
        binder.forField(channel).asRequired().bind(SupportMessage::getChannel, SupportMessage::setChannel);
        binder.forField(status).asRequired().bind(SupportMessage::getStatus, SupportMessage::setStatus);
        binder.forField(receivedAt).asRequired().bind(SupportMessage::getReceivedAt, SupportMessage::setReceivedAt);
        binder.readBean(message);

        if (channel.getValue() == null) {
            channel.setValue("Email");
        }
        if (status.getValue() == null) {
            status.setValue("UNREAD");
        }
        if (receivedAt.getValue() == null) {
            receivedAt.setValue(LocalDateTime.now());
        }

        Button save = new Button(isNew ? "Create" : "Save");
        save.addClassNames("btn", "btn-primary");
        save.addClickListener(event -> {
            try {
                binder.writeBean(message);
                messageService.save(message);
                dialog.close();
                refreshSummary();
                refreshGrid();
                notify(isNew ? "Message created." : "Message updated.", NotificationVariant.LUMO_SUCCESS);
            } catch (ValidationException ex) {
                notify("Please fix the highlighted errors.", NotificationVariant.LUMO_ERROR);
            }
        });

        Button cancel = new Button("Cancel", event -> dialog.close());
        cancel.addClassNames("btn", "btn-outline");

        Div footer = new Div(cancel, save);
        footer.addClassName("dialog-footer");

        dialog.add(form);
        dialog.getFooter().add(footer);
        dialog.open();
    }

    private void confirmDelete(SupportMessage message) {
        ConfirmDialog confirm = new ConfirmDialog();
        confirm.setHeader("Delete message from " + message.getSenderName() + "?");
        confirm.setText("This action cannot be undone.");
        confirm.setCancelable(true);
        confirm.setConfirmText("Delete");
        confirm.setConfirmButtonTheme("error primary");
        confirm.addConfirmListener(event -> {
            messageService.delete(message);
            refreshSummary();
            refreshGrid();
            notify("Message deleted.", NotificationVariant.LUMO_CONTRAST);
        });
        confirm.open();
    }

    private void refreshGrid() {
        List<SupportMessage> messages = messageService.search(searchField.getValue());
        if (!"ALL".equalsIgnoreCase(statusFilter.getValue())) {
            messages = messages.stream()
                .filter(message -> statusFilter.getValue().equalsIgnoreCase(message.getStatus()))
                .toList();
        }
        grid.setItems(messages);
    }

    private void refreshSummary() {
        List<SupportMessage> messages = messageService.findAll();
        totalMessagesValue.setText(String.valueOf(messages.size()));
        unreadMessagesValue.setText(String.valueOf(messageService.countByStatus("UNREAD")));
        openMessagesValue.setText(String.valueOf(messageService.countByStatus("OPEN")));
    }

    private void notify(String message, NotificationVariant variant) {
        Notification notification = Notification.show(message, 3000, Notification.Position.BOTTOM_END);
        notification.addThemeVariants(variant);
    }
}
