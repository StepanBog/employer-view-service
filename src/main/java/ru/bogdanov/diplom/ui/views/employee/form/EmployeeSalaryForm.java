package ru.bogdanov.diplom.ui.views.employee.form;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.PropertyId;
import com.vaadin.flow.data.converter.StringToLongConverter;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.bogdanov.diplom.backend.data.containers.Employee;
import ru.bogdanov.diplom.backend.data.containers.Salary;
import ru.bogdanov.diplom.backend.data.containers.Transaction;
import ru.bogdanov.diplom.backend.data.containers.User;
import ru.bogdanov.diplom.backend.service.ISalaryService;
import ru.bogdanov.diplom.backend.service.ITransactionService;
import ru.bogdanov.diplom.ui.components.field.CustomTextField;
import ru.bogdanov.diplom.ui.components.grid.PaginatedGrid;
import ru.bogdanov.diplom.ui.util.LumoStyles;
import ru.bogdanov.diplom.ui.util.UIUtils;
import ru.bogdanov.diplom.ui.util.converter.BigDecimalToLongConverter;
import ru.bogdanov.diplom.ui.util.converter.LocalDateToLocalDateTimeConverter;
import ru.bogdanov.diplom.ui.util.converter.StringToStringWithNullValueConverter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * @author SBogdanov
 */
public class EmployeeSalaryForm extends VerticalLayout {

    public static final String ID = "employeeSalaryForm";

    @Setter
    private ISalaryService salaryService;
    @Setter
    private ITransactionService transactionService;

    @Getter
    private boolean editable = false;

    @PropertyId("availableCash")
    private BigDecimalField availableCashField = new BigDecimalField("Доступная сумма");
    @PropertyId("earnedForMonthAfter")
    private BigDecimalField earnedForMonthFieldAfter = new BigDecimalField("Заработанно за месяц с учетом налога");
    @PropertyId("earnedForMonth")
    private BigDecimalField earnedForMonthField = new BigDecimalField("Заработанно за месяц без учета налога");
    @PropertyId("rate")
    private BigDecimalField rateField = new BigDecimalField("Ставка");

    private TextField sum = new TextField("Сумма запроса");

    @Getter
    private BeanValidationBinder<Salary> binder;
    private final VerticalLayout salaryFormLayout = new VerticalLayout();
    private Salary salary;

    public void init() {
        setId(ID);
        setSizeFull();

        this.salary = Salary.builder()
                .build();

        this.binder = new BeanValidationBinder<>(Salary.class);
        this.binder.setBean(this.salary);

        LocalDateToLocalDateTimeConverter localDateTimeConverter = new LocalDateToLocalDateTimeConverter();
        BigDecimalToLongConverter bigDecimalToLongConverter = new BigDecimalToLongConverter();

        this.binder.forField(availableCashField)
                .withConverter(bigDecimalToLongConverter)
                .withNullRepresentation(0L)
                .bind(Salary::getAvailableCash, Salary::setAvailableCash);
        this.binder.forField(earnedForMonthField)
                .withConverter(bigDecimalToLongConverter)
                .withNullRepresentation(0L)
                .bind(Salary::getEarnedForMonth, Salary::setEarnedForMonth);
        this.binder.forField(earnedForMonthFieldAfter)
                .withConverter(bigDecimalToLongConverter)
                .withNullRepresentation(0L)
                .bind(Salary::getEarnedForMonth, Salary::setEarnedForMonth);
        this.binder.forField(rateField)
                .withConverter(bigDecimalToLongConverter)
                .withNullRepresentation(0L)
                .bind(Salary::getRate, Salary::setRate);
        this.binder.bindInstanceFields(this);

        VerticalLayout layout = createForm();

        layout.setPadding(false);
        layout.setSpacing(false);
        layout.setSizeFull();
        layout.setAlignItems(Alignment.CENTER);
        add(layout);

        availableCashField.setReadOnly(true);
        rateField.setReadOnly(true);
        earnedForMonthField.setReadOnly(true);
        earnedForMonthFieldAfter.setReadOnly(true);
    }
    public VerticalLayout createForm() {

        salaryFormLayout.add(new Label("Информация о счёте"));
        FormLayout layout = new FormLayout();
        layout.add(availableCashField);
        layout.add(earnedForMonthFieldAfter);
        layout.add(rateField);
        layout.add(earnedForMonthField);
        salaryFormLayout.add(layout);

        salaryFormLayout.addClassNames(
                LumoStyles.Padding.Bottom.XS,
                LumoStyles.Padding.Horizontal.XS,
                LumoStyles.Padding.Top.XS);
        salaryFormLayout.setAlignItems(Alignment.CENTER);
        return salaryFormLayout;
    }


    public void withBean(Employee employee) {
        Salary salary = salaryService.findByEmployeeId(UUID.fromString(employee.getId()));
        this.availableCashField.setValue(BigDecimal.valueOf(salary.getAvailableCash()).movePointLeft(2));
        // this.availableCashField.setValue(String.valueOf(new BigDecimal(salary.getAvailableCash()).movePointLeft(2)));
        this.earnedForMonthField.setValue(BigDecimal.valueOf(salary.getEarnedForMonth()).movePointLeft(2));
        this.earnedForMonthFieldAfter.setValue((new BigDecimal(salary.getEarnedForMonth()).multiply(BigDecimal.valueOf(0.87))).setScale(0, RoundingMode.DOWN).movePointLeft(2));
        this.rateField.setValue(BigDecimal.valueOf(salary.getRate()).movePointLeft(2));
    }

    public void setFieldsReadOnly(boolean flag) {
        editable = !flag;
        rateField.setReadOnly(flag);

    }


}
