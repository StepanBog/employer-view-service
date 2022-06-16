package ru.bogdanov.diplom.ui.views.employee;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.BoxSizing;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.PropertyId;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.QueryParameters;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import ru.bogdanov.diplom.backend.data.containers.Employee;
import ru.bogdanov.diplom.backend.data.containers.Employer;
import ru.bogdanov.diplom.backend.data.enums.EmployeeStatus;
import ru.bogdanov.diplom.grpc.generated.service.employer.SearchEmployerRequest;
import ru.bogdanov.diplom.ui.components.Badge;
import ru.bogdanov.diplom.ui.components.ColumnToggleContextMenu;
import ru.bogdanov.diplom.ui.components.field.CustomTextField;
import ru.bogdanov.diplom.ui.components.grid.PaginatedGrid;
import ru.bogdanov.diplom.ui.util.IconSize;
import ru.bogdanov.diplom.ui.util.UIUtils;
import ru.bogdanov.diplom.ui.util.converter.LocalDateToLocalDateTimeConverter;
import ru.bogdanov.diplom.ui.util.converter.StringToStringWithNullValueConverter;
import ru.bogdanov.diplom.ui.views.employee.form.CreateEmployeeDialog;

import java.time.format.DateTimeFormatter;
import java.util.Map;

public class EmployeeGridWithFilter extends EmployeeGrid{

    public static final String ID = "employeeGridWithFilter";

    @Setter
    private boolean fromEmployer = false;

    @PropertyId("status")
    private ComboBox<EmployeeStatus> statusField = new ComboBox();
    @PropertyId("lastName")
    private CustomTextField lastNameNameField = new CustomTextField();
    @PropertyId("firstName")
    private CustomTextField firstNameField = new CustomTextField();
    @PropertyId("patronymicName")
    private CustomTextField patronymicNameField = new CustomTextField();

    @PropertyId("createdAt")
    private DatePicker createdAtField = new DatePicker();

    @Getter
    private BeanValidationBinder<Employee> binder;

    public void init() {
        setId(ID);
        setSizeFull();
        initFields();

        initDataProvider();

        this.binder = new BeanValidationBinder<>(Employee.class);
        this.binder.setBean(this.employeeFilter);

        LocalDateToLocalDateTimeConverter localDateTimeConverter = new LocalDateToLocalDateTimeConverter();

        this.binder.forField(createdAtField)
                .withConverter(localDateTimeConverter)
                .bind(Employee::getCreatedAt, Employee::setCreatedAt);
        this.binder.bindInstanceFields(this);

        add(createContent());
    }

    private Component createContent() {
        VerticalLayout content = new VerticalLayout(
                createButton(),
                createGrid()
        );

        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setHeightFull();
        content.setPadding(false);
        content.setMargin(false);
        content.setSpacing(false);
        return content;
    }

    private Component createButton() {
        Button create = UIUtils.createPrimaryButton("Создать нового работника");
        create.addClickListener(buttonClickEvent -> {
            CreateEmployeeDialog dialog = new CreateEmployeeDialog();
            dialog.init();
            dialog.setEmployeeService(employeeService);
            dialog.setSaveAction(action -> {
                dataProvider.refreshAll();
            });
            dialog.setCloseAction(action -> {
                dataProvider.refreshAll();
            });
            dialog.setUserService(userService);
            dialog.open();
        });
        return create;
    }

    private void initFields() {

        StringToStringWithNullValueConverter stringToStringWithNullValueConverter = new StringToStringWithNullValueConverter();

        statusField.setPlaceholder("Статус");
        statusField.setItems(EmployeeStatus.values());
        statusField.setItemLabelGenerator(EmployeeStatus::getDescription);
        statusField.setClearButtonVisible(true);
        statusField.setWidthFull();
        statusField.getElement().getThemeList().add(TextFieldVariant.LUMO_SMALL.getVariantName());
        statusField.getStyle().set("max-width", "100%");
        statusField.addValueChangeListener(
                s -> {
                    employeeFilter.setStatus(s.getValue());
                    grid.getDataProvider().refreshAll();
                    grid.refreshPaginator();
                }
        );

        lastNameNameField.setConverters(stringToStringWithNullValueConverter);
        lastNameNameField.setPlaceholder("Фамилия");
        lastNameNameField.setClearButtonVisible(true);
        lastNameNameField.setWidthFull();
        lastNameNameField.getElement().getThemeList().add(TextFieldVariant.LUMO_SMALL.getVariantName());
        lastNameNameField.getStyle().set("max-width", "100%");
        lastNameNameField.addValueChangeListener(
                s -> {
                    employeeFilter.setLastName(s.getValue());
                    grid.getDataProvider().refreshAll();
                    grid.refreshPaginator();
                }
        );

        firstNameField.setConverters(stringToStringWithNullValueConverter);
        firstNameField.setPlaceholder("Имя");
        firstNameField.setClearButtonVisible(true);
        firstNameField.setWidthFull();
        firstNameField.getElement().getThemeList().add(TextFieldVariant.LUMO_SMALL.getVariantName());
        firstNameField.getStyle().set("max-width", "100%");
        firstNameField.addValueChangeListener(
                s -> {
                    employeeFilter.setFirstName(s.getValue());
                    grid.getDataProvider().refreshAll();
                    grid.refreshPaginator();
                }
        );

        patronymicNameField.setConverters(stringToStringWithNullValueConverter);
        patronymicNameField.setPlaceholder("Отчество");
        patronymicNameField.setClearButtonVisible(true);
        patronymicNameField.setWidthFull();
        patronymicNameField.getElement().getThemeList().add(TextFieldVariant.LUMO_SMALL.getVariantName());
        patronymicNameField.getStyle().set("max-width", "100%");
        patronymicNameField.addValueChangeListener(
                s -> {
                    employeeFilter.setPatronymicName(s.getValue());
                    grid.getDataProvider().refreshAll();
                    grid.refreshPaginator();
                }
        );

        createdAtField.setPlaceholder("Дата регистрации");
        createdAtField.setClearButtonVisible(true);
        createdAtField.setWidthFull();
        createdAtField.getElement().getThemeList().add(TextFieldVariant.LUMO_SMALL.getVariantName());
        createdAtField.getStyle().set("max-width", "100%");
        createdAtField.addValueChangeListener(
                e -> {
                    if (e.getValue() != null) {
                        employeeFilter.setCreatedAt(e.getValue().atStartOfDay());
                    } else {
                        employeeFilter.setCreatedAt(null);
                    }
                    grid.getDataProvider().refreshAll();
                    grid.refreshPaginator();
                });
    }

    private void initDataProvider() {
        this.dataProvider = new CallbackDataProvider<Employee, Employee>(
                query -> employeeService.find(query, PAGE_SIZE).stream(),
                query -> employeeService.getTotalCount(query))
                .withConfigurableFilter();

        this.employeeFilter = Employee.builder()
                .status(null)
                .build();
        this.dataProvider.setFilter(this.employeeFilter);
    }

    private Grid<Employee> createGrid() {
        grid = new PaginatedGrid<>();
        grid.setPageSize(PAGE_SIZE);
        grid.setPaginatorSize(2);
        grid.setDataProvider(dataProvider);
        grid.setHeightFull();

        ComponentRenderer<Button, Employee> actionRenderer = new ComponentRenderer<>(
                employee -> {
                    Button editButton = UIUtils.createButton(VaadinIcon.EDIT,
                            ButtonVariant.LUMO_ICON,
                            ButtonVariant.LUMO_SMALL);
                    editButton.addClassName(IconSize.XS.getClassName());
                    editButton.addClickListener(event -> toViewPage(employee));
                    return editButton;
                }
        );
        ComponentRenderer<Badge, Employee> badgeRenderer = new ComponentRenderer<>(
                employee -> {
                    EmployeeStatus status = employee.getStatus();
                    Badge badge = new Badge(status.getDescription(), employee.getStatusTheme());
                    return badge;
                }
        );

        Grid.Column<Employee> actionColumn = grid.addColumn(actionRenderer)
                .setFrozen(true)
                .setFlexGrow(0)
                .setWidth("100px")
                .setHeader("Действие")
                .setResizable(true);

        Grid.Column<Employee> firstNameColumn = grid.addColumn(Employee::getFirstName)
                .setAutoWidth(true)
                .setComparator(Employee::getFirstName)
                .setHeader("Имя")
                .setResizable(true);
        firstNameColumn.setVisible(true);

        Grid.Column<Employee> lastNameColumn = grid.addColumn(Employee::getLastName)
                .setAutoWidth(true)
                .setComparator(Employee::getLastName)
                .setHeader("Фамилия")
                .setResizable(true);
        lastNameColumn.setVisible(false);

        Grid.Column<Employee> patronymicNameColumn = grid.addColumn(Employee::getPatronymicName)
                .setAutoWidth(true)
                .setComparator(Employee::getPatronymicName)
                .setHeader("Отчество")
                .setResizable(true);
        patronymicNameColumn.setVisible(false);

        Grid.Column<Employee> statusColumn = grid.addColumn(badgeRenderer)
                .setAutoWidth(true)
                .setComparator(Employee::getStatus)
                .setHeader("Статус")
                .setResizable(true);

        Grid.Column<Employee> createdAtColumn = grid.addColumn(new LocalDateTimeRenderer<>(Employee::getCreatedAt, DateTimeFormatter.ofPattern("YYYY dd MMM HH:mm:ss")))
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setComparator(Employee::getCreatedAt)
                .setResizable(true)
                .setHeader("Дата регистрации");


        Button menuButton = new Button();
        menuButton.setIcon(VaadinIcon.ELLIPSIS_DOTS_H.create());
        menuButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        ColumnToggleContextMenu columnToggleContextMenu = new ColumnToggleContextMenu(
                menuButton);
        columnToggleContextMenu.addColumnToggleItem("Имя", firstNameColumn);
        columnToggleContextMenu.addColumnToggleItem("Фамилия", lastNameColumn);
        columnToggleContextMenu.addColumnToggleItem("Отчество", patronymicNameColumn);
        columnToggleContextMenu.addColumnToggleItem("Статус", statusColumn);
        columnToggleContextMenu.addColumnToggleItem("Дата регистрации", createdAtColumn);

        grid.getHeaderRows().clear();
        HeaderRow headerRow = grid.appendHeaderRow();

        headerRow.getCell(actionColumn).setComponent(menuButton);
        headerRow.getCell(statusColumn).setComponent(statusField);
        headerRow.getCell(firstNameColumn).setComponent(firstNameField);
        headerRow.getCell(lastNameColumn).setComponent(lastNameNameField);
        headerRow.getCell(patronymicNameColumn).setComponent(patronymicNameField);
        headerRow.getCell(createdAtColumn).setComponent(createdAtField);

        return grid;
    }

    private void toViewPage(Employee employee) {
        Map<String, String> params = Map.of(
                "employeeId", employee.getId(),
                "backToEmployerForm", String.valueOf(fromEmployer)
        );
        UI.getCurrent().navigate(EmployeeView.ROUTE,
                QueryParameters.simple(
                        params
                )
        );
    }

    public void withFilter(Employee employeeFilter) {
        this.employeeFilter = employeeFilter;

        binder.setBean(employeeFilter);
        binder.bindInstanceFields(this);

        dataProvider.setFilter(employeeFilter);
        grid.getDataProvider().refreshAll();
    }
}
