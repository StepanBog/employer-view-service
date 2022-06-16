package ru.bogdanov.diplom.ui.views.employer.form;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.PropertyId;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.function.ValueProvider;
import lombok.Getter;
import lombok.Setter;
import ru.bogdanov.diplom.backend.data.containers.Contact;
import ru.bogdanov.diplom.backend.data.containers.Employer;
import ru.bogdanov.diplom.backend.data.enums.ContactPosition;
import ru.bogdanov.diplom.backend.service.IEmployeeService;
import ru.bogdanov.diplom.grpc.generated.EmployeeStatus;
import ru.bogdanov.diplom.ui.util.LumoStyles;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;


public class EmployerSettingsForm extends VerticalLayout {

    public static final String ID = "employerSettingsForm";

    private Pattern phonePattern = Pattern.compile(
                    "^(\\+7|7|8)?[\\s\\-]?\\(?[489][0-9]{2}\\)?[\\s\\-]?[0-9]{3}[\\s\\-]?[0-9]{2}[\\s\\-]?[0-9]{2}$");

    @Getter
    private BeanValidationBinder<Employer> binder;

    @Getter
    private boolean editable = false;

    @PropertyId("name")
    private TextField nameField = new TextField("Название");
    @PropertyId("inn")
    private TextField innField = new TextField("ИНН");
    @PropertyId("kpp")
    private TextField kppField = new TextField("КПП");
    @PropertyId("employees")
    private TextField employeesField = new TextField("Зарегистрировано работников");
    @PropertyId("activeEmployees")
    private TextField activeEmployeesField = new TextField("Активных работников");

    private TextField managerFioField = new TextField("Менеджер");
    private TextField managerPhoneField = new TextField("Телефон менеджера");
    private TextField managerEmailField = new TextField("Email");
    private TextField employerContactFioField = new TextField("Контактное лицо");
    private TextField employerContactPhoneField = new TextField("Телефон контактного лица");
    private TextField employerContactEmailField = new TextField("Email");

    private final List<FormLayout.ResponsiveStep> responsiveSteps = List.of(
            new FormLayout.ResponsiveStep("0", 1,
                    FormLayout.ResponsiveStep.LabelsPosition.TOP),
            new FormLayout.ResponsiveStep("600px", 1,
                    FormLayout.ResponsiveStep.LabelsPosition.TOP));

    @Setter
    private IEmployeeService employeeService;

    public void init() {
        setId(ID);
        setFieldsReadOnly(true);
        binder = new BeanValidationBinder<>(Employer.class);
        binder.bind(employeesField, (ValueProvider<Employer, String>) employer ->
                                employer.getId() == null ? "" : Long.toString(
                                        employeeService.countEmployeesByEmployer(employer)), null);
        binder.bind(activeEmployeesField, (ValueProvider<Employer, String>) employer ->
                        employer.getId() == null ? "" : Long.toString(
                                employeeService.countEmployeesByEmployer(employer, EmployeeStatus.ENABLED_EMPLOYEE)
                        ), null);

        add(createForm());
    }

    private HorizontalLayout createForm() {
        VerticalLayout rightVerticalLayout = new VerticalLayout(
                new Label("Контактные лица"),
                new Hr(),
                getTopRightFormLayout(),
                getEmptySpace("40px"),
                getBottomRightFormLayout());
        rightVerticalLayout.setMaxWidth("25%");

        VerticalLayout leftFormLayout = new VerticalLayout(
                new Label("Основная информация"),
                new Hr(),
                getTopLeftFormLayout());
        leftFormLayout.setMaxWidth("25%");

        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setSpacing(false);
        mainLayout.setWidthFull();
        mainLayout.getThemeList().add("spacing-xl");
        mainLayout.addClassNames(LumoStyles.Padding.Bottom.L,
                LumoStyles.Padding.Horizontal.L, LumoStyles.Padding.Top.S);

        mainLayout.add(leftFormLayout, rightVerticalLayout);
        mainLayout.setMaxWidth(null);
        mainLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        return mainLayout;
    }

    private FormLayout getEmptySpace(String size) {
        FormLayout formLayout = new FormLayout();
        formLayout.setHeight(size);
        return formLayout;
    }

    private FormLayout getBottomRightFormLayout() {
        FormLayout formLayout = new FormLayout(
                employerContactFioField,
                employerContactPhoneField,
                employerContactEmailField
        );
        formLayout.setResponsiveSteps(responsiveSteps);
        return formLayout;
    }

    private FormLayout getTopRightFormLayout() {
        FormLayout formLayout = new FormLayout(
                managerFioField,
                managerPhoneField,
                managerEmailField
        );
        formLayout.setResponsiveSteps(responsiveSteps);
        return formLayout;
    }

    private FormLayout getTopLeftFormLayout() {
        nameField.setRequired(true);
        nameField.setRequiredIndicatorVisible(true);

        innField.setRequired(true);

        FormLayout formLayout = new FormLayout(
                nameField,
                innField,
                kppField,
                employeesField,
                activeEmployeesField
        );
        formLayout.setResponsiveSteps(responsiveSteps);
        return formLayout;
    }

    public void withBean(Employer employer) {

        Contact managerContact = setContact(employer, ContactPosition.MANAGER);
        Contact employerContact = setContact(employer, ContactPosition.EMPLOYERS_CONTACT);

        bindContacts(managerFioField, managerPhoneField, managerEmailField, managerContact);
        bindContacts(employerContactFioField, employerContactPhoneField, employerContactEmailField, employerContact);

        binder.bindInstanceFields(this);
        binder.setBean(employer);
    }

    private Contact setContact(Employer employer, ContactPosition position) {
        Contact result;
        Set<Contact> contacts = employer.getContacts();
        Optional<Contact> optionalContact = contacts.stream()
                .filter(c -> c.getPosition() == position)
                .findAny();
        if (optionalContact.isPresent()) {
            result = optionalContact.get();
        } else {
            result = Contact.builder().position(position).build();
            contacts.add(result);
        }
        return result;
    }

    private void bindContacts(TextField fioField, TextField phoneFiled, TextField emailField, Contact contact) {
        binder.bind(fioField, (ValueProvider<Employer, String>) employer -> contact.getName(),
                (com.vaadin.flow.data.binder.Setter<Employer, String>) (employer, s) -> contact.setName(s)
        );
        binder.forField(phoneFiled)
                .withValidator(s -> phonePattern.matcher(s).find(), "Некорректный номер телефона")
                .bind((ValueProvider<Employer, String>) employer -> contact.getPhone(),
                (com.vaadin.flow.data.binder.Setter<Employer, String>) (employer, s) -> contact.setPhone(s)
        );
        binder.forField(emailField)
                .withValidator(new EmailValidator("Некоректный адрес электронной почты"))
                .bind((ValueProvider<Employer, String>) employer -> contact.getEmail(),
                (com.vaadin.flow.data.binder.Setter<Employer, String>) (employer, s) -> contact.setEmail(s)
        );
    }

    public void setFieldsReadOnly(boolean flag) {
        editable = !flag;
        nameField.setReadOnly(flag);
        innField.setReadOnly(flag);
        kppField.setReadOnly(flag);
        managerFioField.setReadOnly(flag);
        managerPhoneField.setReadOnly(flag);
        managerEmailField.setReadOnly(flag);
        employerContactFioField.setReadOnly(flag);
        employerContactPhoneField.setReadOnly(flag);
        employerContactEmailField.setReadOnly(flag);
    }
}
