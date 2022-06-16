package ru.bogdanov.diplom.ui.views.requisites;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.PropertyId;
import lombok.Getter;
import ru.bogdanov.diplom.backend.data.containers.Employee;
import ru.bogdanov.diplom.backend.data.containers.Requisites;
import ru.bogdanov.diplom.backend.data.enums.EmployeeStatus;
import ru.bogdanov.diplom.ui.components.field.CustomTextField;
import ru.bogdanov.diplom.ui.util.LumoStyles;
import ru.bogdanov.diplom.ui.util.converter.StringToLocalDateTimeConverter;

import javax.annotation.PostConstruct;
import java.util.UUID;

public class RequisitesForm extends VerticalLayout {

    public static final String ID = "requisitesForm";

    @Getter
    private BeanValidationBinder<Requisites> binder;

    private UUID idField;

    @Getter
    private boolean editable = false;

    @PropertyId("accountNumber")
    private TextField accountNumberField = new TextField("Счет");
    @PropertyId("inn")
    private TextField innField = new TextField("ИНН");
    @PropertyId("kpp")
    private TextField kppField = new TextField("КПП");
    @PropertyId("bik")
    private TextField bikField = new TextField("БИК");
    @PropertyId("bankName")
    private TextField bankNameField = new TextField("Банк");
    @PropertyId("corr")
    private TextField corrField = new TextField("Корсчет");
    @PropertyId("snils")
    private TextField snilsField = new TextField("Cнилс (без проблелов и разделителей)");
    @PropertyId("passportSeries")
    private TextField passportSeriesField = new TextField("Cерия паспортa");
    @PropertyId("passportNumber")
    private TextField passportNumberField = new TextField("Номер паспорта");
    @PropertyId("firstName")
    private TextField firstNameField = new TextField("Имя");
    @PropertyId("lastName")
    private TextField lastNameField = new TextField("Фамилия");
    @PropertyId("patronymicName")
    private TextField patronymicNameField = new TextField("Отчество");

    @PropertyId("status")
    private ComboBox<EmployeeStatus> statusField = new ComboBox<>("Статус");

    @PropertyId("phone")
    private TextField phoneField = new TextField("Телефон");

    private FormLayout fioLayout = new FormLayout();
    private FormLayout passportLayout = new FormLayout();
    private FormLayout accountLayout = new FormLayout();


    @PostConstruct
    public void init() {
        setId(ID);
        setFieldsReadOnly(true);
        initFields();

        StringToLocalDateTimeConverter stringToLocalDateTimeConverter = new StringToLocalDateTimeConverter();

        this.binder = new BeanValidationBinder<>(Requisites.class);
        createForm();
    }

    private void initFields() {
        statusField.setItems(EmployeeStatus.values());
        statusField.setItemLabelGenerator(EmployeeStatus::getDescription);
    }

    public void createForm() {
        FormLayout dataLayout = new FormLayout();
        dataLayout.addClassNames(LumoStyles.Padding.Bottom.XS,
                LumoStyles.Padding.Horizontal.L, LumoStyles.Padding.Top.XS);
        dataLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP),
                new FormLayout.ResponsiveStep("600px", 2,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP),
                new FormLayout.ResponsiveStep("1024px", 2,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP));

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setPadding(false);
        verticalLayout.setMargin(false);
        verticalLayout.setSpacing(false);

        accountNumberField.setRequired(true);

        accountLayout.add(accountNumberField);
        accountLayout.add(bankNameField);
        accountLayout.add(snilsField);
        accountLayout.add(innField);
        accountLayout.add(kppField);
        accountLayout.add(bikField);
        accountLayout.add(corrField);

        accountLayout.addClassNames(LumoStyles.Padding.Bottom.XS,
                LumoStyles.Padding.Horizontal.L, LumoStyles.Padding.Top.XS);
        accountLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP),
                new FormLayout.ResponsiveStep("600px", 2,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP),
                new FormLayout.ResponsiveStep("1024px", 2,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP));


        fioLayout.add(firstNameField);
        fioLayout.add(lastNameField);
        fioLayout.add(patronymicNameField);
        fioLayout.add(statusField);
        fioLayout.add(phoneField);

        fioLayout.addClassNames(LumoStyles.Padding.Bottom.XS,
                LumoStyles.Padding.Horizontal.L, LumoStyles.Padding.Top.XS);
        fioLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP),
                new FormLayout.ResponsiveStep("600px", 1,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP),
                new FormLayout.ResponsiveStep("1024px", 3,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP));


        passportLayout.add(passportSeriesField);
        passportLayout.add(passportNumberField);
        passportLayout.addClassNames(LumoStyles.Padding.Bottom.XS,
                LumoStyles.Padding.Horizontal.L, LumoStyles.Padding.Top.XS);
        passportLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP),
                new FormLayout.ResponsiveStep("600px", 2,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP),
                new FormLayout.ResponsiveStep("1024px", 2,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP));


        verticalLayout.add(dataLayout);
        verticalLayout.add(fioLayout);
        verticalLayout.add(new Hr());
        verticalLayout.add(passportLayout);
        verticalLayout.add(new Hr());
        verticalLayout.add(accountLayout);

        add(verticalLayout);
    }

    public void withBean(Requisites requisites, Employee employee) {
        this.binder.removeBean();
        this.idField = UUID.fromString(employee.getId());
        if (employee.getStatus() != null) {
            statusField.setValue(employee.getStatus());
        }
        if (employee.getPhone() != null) {
            this.phoneField.setValue(employee.getPhone());
        }
        this.binder.setBean(requisites);
        this.binder.bindInstanceFields(this);
    }

    public Employee getEmployee() {
        return Employee.builder()
                .id(String.valueOf(idField))
                .phone(phoneField.getValue())
                .status(statusField.getValue())
                .firstName(firstNameField.getValue())
                .lastName(lastNameField.getValue())
                .patronymicName(patronymicNameField.getValue())
                .build();
    }

    public void setFieldsReadOnly(boolean flag) {
        editable = !flag;
        firstNameField.setReadOnly(flag);
        lastNameField.setReadOnly(flag);
        patronymicNameField.setReadOnly(flag);
        passportNumberField.setReadOnly(flag);
        passportSeriesField.setReadOnly(flag);
        phoneField.setReadOnly(flag);
        statusField.setReadOnly(flag);
        accountNumberField.setReadOnly(flag);
        corrField.setReadOnly(flag);
        bikField.setReadOnly(flag);
        bankNameField.setReadOnly(flag);
        snilsField.setReadOnly(flag);
        innField.setReadOnly(flag);
        kppField.setReadOnly(flag);

    }

}
