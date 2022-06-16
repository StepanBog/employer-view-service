package ru.bogdanov.diplom.ui.views.transaction;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.BoxSizing;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.PropertyId;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import ru.bogdanov.diplom.backend.data.containers.Requisites;
import ru.bogdanov.diplom.backend.data.containers.Transaction;
import ru.bogdanov.diplom.backend.data.enums.TransactionStatus;
import ru.bogdanov.diplom.ui.components.Badge;
import ru.bogdanov.diplom.ui.components.ColumnToggleContextMenu;
import ru.bogdanov.diplom.ui.components.grid.PaginatedGrid;
import ru.bogdanov.diplom.ui.util.IconSize;
import ru.bogdanov.diplom.ui.util.UIUtils;
import ru.bogdanov.diplom.ui.util.converter.LocalDateToLocalDateTimeConverter;
import ru.bogdanov.diplom.ui.views.employee.form.CreateEmployeeDialog;
import ru.bogdanov.diplom.ui.views.transaction.form.ApproveTransactionDialog;

import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class TransactionGridWithFilter extends TransactionGrid{
    public static final String ID = "transactionGridWithFilter";
    protected UUID id;
    @PropertyId("status")
    protected ComboBox<TransactionStatus> statusField = new ComboBox();
    @PropertyId("totalSum")
    protected BigDecimalField totalSumField = new BigDecimalField();

    @PropertyId("updatedAt")
    protected DatePicker updatedAtField = new DatePicker();
    @PropertyId("createdAt")
    protected DatePicker createdAtField = new DatePicker();
    @Getter
    private BeanValidationBinder<Transaction> binder;

    private boolean dialogIsOpen = false;


    public void init() {
        setId(ID);
        setSizeFull();
        initFields();

        initDataProvider();

        this.binder = new BeanValidationBinder<>(Transaction.class);
        this.binder.setBean(this.transactionFilter);

        LocalDateToLocalDateTimeConverter localDateTimeConverter = new LocalDateToLocalDateTimeConverter();
        this.binder.forField(updatedAtField)
                .withConverter(localDateTimeConverter)
                .bind(Transaction::getUpdatedAt, Transaction::setUpdatedAt);
        this.binder.forField(createdAtField)
                .withConverter(localDateTimeConverter)
                .bind(Transaction::getCreatedAt, Transaction::setCreatedAt);
        this.binder.bindInstanceFields(this);

        add(createContent());
    }

    private void initFields() {

        statusField.setPlaceholder("Статус");
        statusField.setItems(TransactionStatus.values());
        statusField.setItemLabelGenerator(TransactionStatus::getDescription);
        statusField.setClearButtonVisible(true);
        statusField.setWidthFull();
        statusField.getElement().getThemeList().add(TextFieldVariant.LUMO_SMALL.getVariantName());
        statusField.getStyle().set("max-width", "100%");
        statusField.addValueChangeListener(
                s -> {
                    transactionFilter.setStatus(s.getValue());
                    grid.getDataProvider().refreshAll();
                    grid.refreshPaginator();
                }
        );

        totalSumField.setPlaceholder("Общая сумма");
        totalSumField.setClearButtonVisible(true);
        totalSumField.setWidthFull();
        totalSumField.getElement().getThemeList().add(TextFieldVariant.LUMO_SMALL.getVariantName());
        totalSumField.getStyle().set("max-width", "100%");
        totalSumField.addValueChangeListener(
                s -> {
                    if (s.getValue() != null) {
                        transactionFilter.setTotalSum(s.getValue().movePointRight(2));
                    } else {
                        transactionFilter.setTotalSum(null);
                    }
                    grid.getDataProvider().refreshAll();
                    grid.refreshPaginator();
                }
        );

        updatedAtField.setPlaceholder("Дата обновления");
        updatedAtField.setClearButtonVisible(true);
        updatedAtField.setWidthFull();
        updatedAtField.getElement().getThemeList().add(TextFieldVariant.LUMO_SMALL.getVariantName());
        updatedAtField.getStyle().set("max-width", "100%");
        updatedAtField.addValueChangeListener(
                e -> {
                    if (e.getValue() != null) {
                        transactionFilter.setUpdatedAt(e.getValue().atStartOfDay());
                    } else {
                        transactionFilter.setUpdatedAt(null);
                    }

                    grid.getDataProvider().refreshAll();
                    grid.refreshPaginator();
                });

        createdAtField.setPlaceholder("Дата создания");
        createdAtField.setClearButtonVisible(true);
        createdAtField.setWidthFull();
        createdAtField.getElement().getThemeList().add(TextFieldVariant.LUMO_SMALL.getVariantName());
        createdAtField.getStyle().set("max-width", "100%");
        createdAtField.addValueChangeListener(
                e -> {
                    if (e.getValue() != null) {
                        transactionFilter.setCreatedAt(e.getValue().atStartOfDay());
                    } else {
                        transactionFilter.setCreatedAt(null);
                    }
                    grid.getDataProvider().refreshAll();
                    grid.refreshPaginator();
                });
    }

    private Component createContent() {

        VerticalLayout content = new VerticalLayout(
                createGrid()
        );

        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setHeightFull();
        content.setPadding(false);
        content.setMargin(false);
        content.setSpacing(false);
        return content;
    }

    private void initDataProvider() {
        this.dataProvider = new CallbackDataProvider<Transaction, Transaction>(
                query -> transactionService.find(query, PAGE_SIZE).stream(),
                query -> transactionService.getTotalCount(query))
                .withConfigurableFilter();

        this.transactionFilter = Transaction.builder()
                .status(null)
                .build();
        this.dataProvider.setFilter(this.transactionFilter);
    }


    private Grid<Transaction> createGrid() {
        grid = new PaginatedGrid<>();
        grid.addSelectionListener(action -> {
            if (!dialogIsOpen) {
                dialogIsOpen = true;
                ApproveTransactionDialog dialog = new ApproveTransactionDialog();
                Transaction transaction = transactionService.findById(UUID.fromString(action.getFirstSelectedItem().get().getId()));
                Requisites requisites = employeeService.findById(UUID.fromString(transaction.getEmployeeId())).getRequisites();
                dialog.init();
                dialog.withBean(requisites, transaction);
                dialog.setTransactionService(transactionService);
                dialog.setApproveAction(approve -> {
                    dataProvider.refreshAll();
                    dialogIsOpen = false;
                });
                dialog.setDeclineAction(decline -> {
                    dataProvider.refreshAll();
                    dialogIsOpen = false;
                });
                dialog.setCancelAction(decline -> {
                    dataProvider.refreshAll();
                    dialogIsOpen = false;
                });
                dialog.open();
            }
        });
        grid.setPageSize(PAGE_SIZE);
        grid.setDataProvider(dataProvider);
        grid.setSizeFull();

        ComponentRenderer<Badge, Transaction> badgeRenderer = new ComponentRenderer<>(
                transaction -> {
                    TransactionStatus status = transaction.getStatus();
                    Badge badge = new Badge(status.getDescription(), transaction.getStatusTheme());
                    return badge;
                }
        );
        Grid.Column<Transaction> idColumn = grid.addColumn(Transaction::getId)
                .setAutoWidth(true)
                .setWidth("100px")
                .setHeader("ID")
                .setSortable(true)
                .setComparator(Transaction::getId)
                .setResizable(true);
        idColumn.setVisible(false);

        Grid.Column<Transaction> statusColumn = grid.addColumn(badgeRenderer)
                .setAutoWidth(true)
                .setComparator(Transaction::getStatus)
                .setWidth("200px")
                .setHeader("Статус")
                .setSortable(true)
                .setResizable(true);

        Grid.Column<Transaction> totalSumColumn = grid.addColumn(new ComponentRenderer<>(this::createAmount))
                .setWidth("200px")
                .setComparator(Transaction::getTotalSum)
                .setHeader("Общая сумма")
                .setSortable(true)
                .setResizable(true);

        Grid.Column<Transaction> updatedAtColumn = grid.addColumn(new LocalDateTimeRenderer<>(Transaction::getUpdatedAt, DateTimeFormatter.ofPattern("YYYY dd MMM HH:mm:ss")))
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setComparator(Transaction::getUpdatedAt)
                .setHeader("Дата обновления")
                .setResizable(true);

        Grid.Column<Transaction> createdAtColumn = grid.addColumn(new LocalDateTimeRenderer<>(Transaction::getCreatedAt, DateTimeFormatter.ofPattern("YYYY dd MMM HH:mm:ss")))
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setComparator(Transaction::getCreatedAt)
                .setHeader("Дата создания")
                .setResizable(true);

        grid.getHeaderRows().clear();
        HeaderRow headerRow = grid.appendHeaderRow();

        headerRow.getCell(statusColumn).setComponent(statusField);
        headerRow.getCell(totalSumColumn).setComponent(totalSumField);
        headerRow.getCell(updatedAtColumn).setComponent(updatedAtField);
        headerRow.getCell(createdAtColumn).setComponent(createdAtField);

        return grid;
    }

    private Component createAmount(Transaction transaction) {
        Double amount = transaction.getTotalSum().movePointLeft(2).doubleValue();
        return UIUtils.createAmountLabel(amount);
    }

    public void withFilter(Transaction transactionFilter) {
        this.transactionFilter = transactionFilter;
        dataProvider.setFilter(transactionFilter);
        binder.setBean(transactionFilter);
        binder.bindInstanceFields(this);


        grid.getDataProvider().refreshAll();
    }

    private void toViewPage(Transaction transaction) {
//        UI.getCurrent().navigate(AccountDetails.class, bankAccount.getId());
    }
}

