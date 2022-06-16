package ru.bogdanov.diplom.ui.views.employee.form;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.PropertyId;
import com.vaadin.flow.data.converter.StringToLongConverter;
import com.vaadin.flow.data.validator.RegexpValidator;
import com.vaadin.flow.server.VaadinSession;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.bogdanov.diplom.backend.data.containers.Employee;
import ru.bogdanov.diplom.backend.data.containers.Requisites;
import ru.bogdanov.diplom.backend.data.containers.Salary;
import ru.bogdanov.diplom.backend.data.containers.User;
import ru.bogdanov.diplom.backend.data.enums.EmployeeStatus;
import ru.bogdanov.diplom.backend.data.enums.UserRoleName;
import ru.bogdanov.diplom.backend.service.IEmployeeService;
import ru.bogdanov.diplom.backend.service.IUserService;
import ru.bogdanov.diplom.ui.util.LumoStyles;
import ru.bogdanov.diplom.ui.util.UIUtils;
import ru.bogdanov.diplom.ui.util.converter.StringToLocalDateTimeConverter;

import java.util.UUID;
import java.util.function.Consumer;

@Slf4j
public class CreateEmployeeDialog extends Dialog {

    private final int PAGE_SIZE = 15;

    @Getter
    private BeanValidationBinder<Employee> binder;
    @Getter
    private BeanValidationBinder<User> binderUser;
    @Getter
    private BeanValidationBinder<Requisites> binderRequisites;
    @Getter
    private BeanValidationBinder<Salary> binderSalary;

    @Setter
    private Consumer<Employee> saveAction;
    @Setter
    private Consumer<Employee> closeAction;

    private Employee employee;
    private User user;
    private Requisites requisites;
    private Salary salary;

    @PropertyId("userName")
    private TextField userName = new TextField("Логин");
    @PropertyId("password")
    private TextField password = new TextField("Пароль");
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

    @PropertyId("rate")
    private TextField rate = new TextField("Зарплата");

    @PropertyId("phone")
    private TextField phoneField = new TextField("Телефон");

    private FormLayout userLayout = new FormLayout();
    private FormLayout fioLayout = new FormLayout();
    private FormLayout passportLayout = new FormLayout();
    private FormLayout accountLayout = new FormLayout();

    @Setter
    private IUserService userService;
    @Setter
    private IEmployeeService employeeService;


    public void init() {
        setWidth("60%");

        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        this.employee = new Employee();
        this.user = new User();
        this.requisites = new Requisites();
        this.salary = new Salary();
        salary.setRate(0L);

        this.binderSalary = new BeanValidationBinder<>(Salary.class);
        this.binderSalary.setBean(this.salary);

        this.binder = new BeanValidationBinder<>(Employee.class);
        this.binder.setBean(this.employee);
        this.binder.forField(phoneField)
                .withValidator(new RegexpValidator("Номер телефона","^(\\+7|7|8)?[\\s\\-]?\\(?[489][0-9]{2}\\)?[\\s\\-]?[0-9]{3}[\\s\\-]?[0-9]{2}[\\s\\-]?[0-9]{2}$"))
                .bind(Employee::getPhone,Employee::setPhone);
        this.binder.bindInstanceFields(this);
        this.binderUser = new BeanValidationBinder<>(User.class);
        this.binderUser.setBean(this.user);
        this.binderUser.forField(userName)
                .withValidator(new RegexpValidator("Введите логин","[a-zA-Z](.[a-zA-Z0-9_-]*)$"))
                .bind(User::getUsername, User::setUsername);
        this.binderUser.forField(password)
                .withValidator(new RegexpValidator("Введите пароль от 6ти символов с использованием заглавных,строчных букв, с специальными символами и цифрами","(?=.*[0-9])(?=.*[!@#$%^&*])(?=.*[a-z])(?=.*[A-Z])[0-9a-zA-Z!@#$%^&*]{6,}"))
                .bind(User::getPassword, User::setPassword);
        this.binderUser.bindInstanceFields(this);

        this.binderRequisites = new BeanValidationBinder<>(Requisites.class);
        this.binderRequisites.setBean(this.requisites);
        this.binderRequisites.forField(firstNameField)
                .withValidator(new RegexpValidator("Введите имя работника","^([А-Я]{1}[а-яё]{1,23})$"))
                .bind(Requisites::getFirstName,Requisites::setFirstName);
        this.binderRequisites.forField(lastNameField)
                .withValidator(new RegexpValidator("Введите фамилию работника","^([А-Я]{1}[а-яё]{1,23})$"))
                .bind(Requisites::getLastName,Requisites::setLastName);
        this.binderRequisites.forField(patronymicNameField)
                .withValidator(new RegexpValidator("Введите отчество работника","^([А-Я]{1}[а-яё]{1,23})$"))
                .bind(Requisites::getPatronymicName,Requisites::setPatronymicName);
        this.binderRequisites.forField(passportSeriesField)
                .withValidator(new RegexpValidator("Введите серию паспорта работника","^([1-9]{1}[0-9]{3})$"))
                .bind(Requisites::getPassportSeries,Requisites::setPassportSeries);
        this.binderRequisites.forField(passportNumberField)
                .withValidator(new RegexpValidator("Введите номер паспорта работника","^([1-9]{1}[0-9]{5})$"))
                .bind(Requisites::getPassportNumber,Requisites::setPassportNumber);
        this.binderRequisites.forField(accountNumberField)
                .withValidator(new RegexpValidator("Введите cчёт работника","^([0-9]{20})$"))
                .bind(Requisites::getAccountNumber,Requisites::setAccountNumber);
        this.binderRequisites.forField(snilsField)
                .withValidator(new RegexpValidator("Введите снилс работника","^([0-9]{11})$"))
                .bind(Requisites::getSnils,Requisites::setSnils);
        this.binderRequisites.forField(innField)
                .withValidator(new RegexpValidator("Введите ИНН работника","^([0-9]{10})$"))
                .bind(Requisites::getInn,Requisites::setInn);
        this.binderRequisites.forField(bankNameField)
                .withValidator(new RegexpValidator("Введите снилс работника","^([А-Я]{1}[а-яё]{1,23})$"))
                .bind(Requisites::getBankName,Requisites::setBankName);
        this.binderRequisites.forField(corrField)
                .withValidator(new RegexpValidator("Введите корпоративный счет банка работника","^([0-9]{20})$"))
                .bind(Requisites::getCorr,Requisites::setCorr);
        this.binderRequisites.forField(kppField)
                .withValidator(new RegexpValidator("Введите КПП банка работника","^([0-9]{9})$"))
                .bind(Requisites::getKpp,Requisites::setKpp);
        this.binderRequisites.forField(bikField)
                .withValidator(new RegexpValidator("Введите БИК банка работника","^([0-9]{9})$"))
                .bind(Requisites::getBik,Requisites::setBik);
        StringToLongConverter stringToLongConverter = new StringToLongConverter("");
        this.binderSalary.forField(rate)
                .withValidator(new RegexpValidator("","^[1-9][0-9]*$"))
                .withConverter(stringToLongConverter)
                .bind(Salary::getRate,Salary::setRate);
        this.binderRequisites.bindInstanceFields(this);

        VerticalLayout layout = new VerticalLayout();

        layout.add(createForm());
        layout.add(createButtons());
        add(layout);
    }

    public VerticalLayout createForm() {
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

        userLayout.add(userName,password);
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
        fioLayout.add(phoneField);
        fioLayout.add(rate);
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
        verticalLayout.add(userLayout);
        verticalLayout.add(new Hr());
        verticalLayout.add(fioLayout);
        verticalLayout.add(new Hr());
        verticalLayout.add(passportLayout);
        verticalLayout.add(new Hr());
        verticalLayout.add(accountLayout);

        return verticalLayout;
    }


    private HorizontalLayout createButtons() {
        HorizontalLayout layout = new HorizontalLayout();

        final Button save = UIUtils.createPrimaryButton("Сохранить");
        save.addClickListener(event -> {
            binderUser.validate();
            binder.validate();
            binderRequisites.validate();
            if (binder.isValid() && binderUser.isValid() && binderRequisites.isValid()) {
                Employee employee = this.binder.getBean();
                Requisites requisites = binderRequisites.getBean();
                employee.setRequisites(requisites);
                employee.setSalary(Salary.builder().rate(Long.valueOf(rate.getValue())).build());
                User u = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                employee.setEmployerId(u.getEmployerId() );
                employee = employeeService.create(employee);
                User user = binderUser.getBean();
                user.setEmployerId(employee.getEmployerId());
                user.setEmployeeId(employee.getId());
                user.setRoleName(UserRoleName.ROLE_EMPLOYEE);
                userService.save(user);
                saveAction.accept(employee);
                this.close();
            }
        });
        final Button cancel = UIUtils.createTertiaryButton("Отменить");
        cancel.addClickListener(event -> {
            closeAction.accept(employee);
            this.binder.removeBean();
            this.close();
        });

        layout.add(save);
        layout.add(cancel);
        return layout;
    }
}
