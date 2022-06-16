package ru.bogdanov.diplom.ui.views.employee;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.bogdanov.diplom.backend.data.containers.Employee;
import ru.bogdanov.diplom.backend.data.containers.Salary;
import ru.bogdanov.diplom.backend.data.containers.Transaction;
import ru.bogdanov.diplom.backend.service.IEmployeeService;
import ru.bogdanov.diplom.backend.service.ISalaryService;
import ru.bogdanov.diplom.backend.service.ITransactionService;
import ru.bogdanov.diplom.ui.EmployerMainLayout;
import ru.bogdanov.diplom.ui.components.FlexBoxLayout;
import ru.bogdanov.diplom.ui.components.navigation.bar.AppBar;
import ru.bogdanov.diplom.ui.handler.SaveButtonErrorHandler;
import ru.bogdanov.diplom.ui.layout.size.Horizontal;
import ru.bogdanov.diplom.ui.layout.size.Vertical;
import ru.bogdanov.diplom.ui.util.UIUtils;
import ru.bogdanov.diplom.ui.views.ViewFrame;
import ru.bogdanov.diplom.ui.views.employee.form.EmployeeSalaryForm;
import ru.bogdanov.diplom.ui.views.employer.EmployerView;
import ru.bogdanov.diplom.ui.views.employer.form.EmployerSettingsForm;
import ru.bogdanov.diplom.ui.views.requisites.RequisitesForm;
import ru.bogdanov.diplom.ui.views.transaction.TransactionGridWithFilter;
import ru.bogdanov.diplom.utils.NotificationUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@PageTitle("Работник")
@Route(value = EmployeeView.ROUTE, layout = EmployerMainLayout.class)
@RequiredArgsConstructor
public class EmployeeView extends ViewFrame implements BeforeEnterObserver {

    public static final String ROUTE = "employee";

    private final IEmployeeService employeeService;
    private final ISalaryService salaryService;
    private final ITransactionService transactionService;

    private Map<String, VerticalLayout> tabLayoutMap;
    private Employee employee;

    private final RequisitesForm requisitesForm = new RequisitesForm();
    private final TransactionGridWithFilter transactionGridWithFilter = new TransactionGridWithFilter();
    private final EmployeeSalaryForm employeeSalaryForm = new EmployeeSalaryForm();

    private Button save, cancel, edit;
    @PostConstruct
    public void init() {
        save = UIUtils.createPrimaryButton("Сохранить");
        save.addClickListener(event -> {
            if (tabLayoutMap.get(RequisitesForm.ID).isVisible()) {
                Employee employee = requisitesForm.getEmployee();
                employee.setRequisites(requisitesForm.getBinder().getBean());
                VaadinSession.getCurrent().setErrorHandler(new SaveButtonErrorHandler());
                employeeService.save(employee);
                setFormEditable(false);
                NotificationUtils.showNotificationOnSave();
            } else if (tabLayoutMap.get(EmployeeSalaryForm.ID).isVisible()) {
                Employee employee = requisitesForm.getEmployee();
                Salary salary = employeeSalaryForm.getBinder().getBean();
                salary.setRate(Long.valueOf(salary.getRate().toString() + "00")) ;
                employee.setSalary(salary);
                VaadinSession.getCurrent().setErrorHandler(new SaveButtonErrorHandler());
                setSalaryFormEditable(false);
                employeeService.save(employee);
                NotificationUtils.showNotificationOnSave();
            }
        });

        edit = UIUtils.createPrimaryButton("Редактировать");
        edit.addClickListener(event -> {
            if (tabLayoutMap.get(RequisitesForm.ID).isVisible()) {
                setFormEditable(true);
            } else if (tabLayoutMap.get(EmployeeSalaryForm.ID).isVisible()) {
                setSalaryFormEditable(true);
            }
        });


        cancel = UIUtils.createTertiaryButton("Отменить");
        cancel.addClickListener(event -> {
            if (tabLayoutMap.get(RequisitesForm.ID).isVisible()) {
                Employee employee = employeeService.findById(UUID.fromString(requisitesForm.getEmployee().getId()));
                requisitesForm.withBean(employee.getRequisites(),employee);
                setFormEditable(false);
            } else if (tabLayoutMap.get(EmployeeSalaryForm.ID).isVisible()) {
                Employee employee = employeeService.findById(UUID.fromString(requisitesForm.getEmployee().getId()));
                employeeSalaryForm.withBean(employee);
                setSalaryFormEditable(false);
            }
            requisitesForm.getEmployee();
        });

       setFormEditable(false);
       setSalaryFormEditable(false);

        HorizontalLayout buttonFooterLayout = new HorizontalLayout(save, cancel,edit);
        buttonFooterLayout.setSpacing(true);
        buttonFooterLayout.setPadding(true);

        setViewContent(createContent());
        setViewFooter(buttonFooterLayout);
    }

    private Component createContent() {
        FlexBoxLayout content = new FlexBoxLayout(
                createEmployerUI()
        );
        content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        content.setMargin(Horizontal.AUTO, Vertical.RESPONSIVE_X);
        content.setWidth("100%");
        content.setHeightFull();
        return content;
    }

    private Component createEmployerUI() {
        requisitesForm.init();
   //     requisitesForm.setFieldsReadOnly(true);
        requisitesForm.setVisible(true);
        requisitesForm.setMaxWidth("800px");

        transactionGridWithFilter.setTransactionService(transactionService);
        transactionGridWithFilter.setEmployeeService(employeeService);
        transactionGridWithFilter.init();
        transactionGridWithFilter.setVisible(false);

        employeeSalaryForm.setSalaryService(salaryService);
        employeeSalaryForm.init();
        employeeSalaryForm.setVisible(false);

        tabLayoutMap =
                Map.of(RequisitesForm.ID, requisitesForm,
                        EmployeeSalaryForm.ID, employeeSalaryForm,
                        TransactionGridWithFilter.ID, transactionGridWithFilter
                );

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSizeFull();
        verticalLayout.setPadding(false);
        verticalLayout.setMargin(false);
        verticalLayout.setSpacing(false);
        verticalLayout.add(
                requisitesForm,
                employeeSalaryForm,
                transactionGridWithFilter);
        verticalLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        return verticalLayout;
    }

    private Tab createTab(String id, Icon icon, String label) {
        Tab tab = new Tab(icon, new Span(label));
        tab.setId(id);
        return tab;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        initAppBar();
        UI.getCurrent().getPage().setTitle(getTitle());
    }

    private AppBar initAppBar() {
        AppBar appBar = EmployerMainLayout.get().getAppBar();

        appBar.addTab(createTab(RequisitesForm.ID, VaadinIcon.MODAL_LIST.create(), "Реквизиты"));
        appBar.addTab(createTab(TransactionGridWithFilter.ID, VaadinIcon.MONEY.create(), "Запросы"));
        appBar.addTab(createTab(EmployeeSalaryForm.ID, VaadinIcon.BOOK.create(), "Счёт"));
        appBar.centerTabs();

        tabLayoutMap.get(RequisitesForm.ID).setVisible(true);
        appBar.addTabSelectionListener(event -> {
            tabLayoutMap.get(event.getPreviousTab().getId().orElse("")).setVisible(false);
            tabLayoutMap.get(event.getSelectedTab().getId().orElse("")).setVisible(true);
            if (tabLayoutMap.get(RequisitesForm.ID).isVisible() || tabLayoutMap.get(EmployeeSalaryForm.ID).isVisible()) {
                if (requisitesForm.isEditable()) {
                    save.setVisible(true);
                    cancel.setVisible(true);
                    edit.setVisible(false);
                } else {
                    edit.setVisible(true);
                    save.setVisible(false);
                    cancel.setVisible(false);
                }
                if (employeeSalaryForm.isEditable()) {
                    save.setVisible(true);
                    cancel.setVisible(true);
                    edit.setVisible(false);
                } else {
                    edit.setVisible(true);
                    save.setVisible(false);
                    cancel.setVisible(false);
                }
            } else {
                edit.setVisible(false);
                save.setVisible(false);
                cancel.setVisible(false);
            }
        });

        appBar.setNaviMode(AppBar.NaviMode.CONTEXTUAL);
        appBar.getContextIcon().addClickListener(e -> navigateToBack());
        appBar.setTitle(getTitle());
        return appBar;
    }

    private String getTitle() {
        String title = "";
        if (employee.getRequisites() != null) {
            title = employee.getRequisites().getLastName() + " "
                    + employee.getRequisites().getFirstName() + " "
                    + employee.getRequisites().getPatronymicName();
        }
        return title;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        Map<String, List<String>> params = beforeEnterEvent.getLocation().getQueryParameters().getParameters();

        UUID employerId = UUID.fromString(params.get("employeeId").get(0));

        employee = employeeService.findById(employerId);

        requisitesForm.withBean(employee.getRequisites(),employee);
       //employeeSettingsForm.withBean(employee);

        employeeSalaryForm.withBean(employee);
        transactionGridWithFilter.withFilter(
                Transaction.builder()
                        .employeeId(employee.getId())
                        .employerId(employee.getEmployerId())
                        .build()
        );
    }

    private void navigateToBack() {
            UI.getCurrent().navigate(EmployerView.class);
    }
    private void setFormEditable(boolean flag) {
        edit.setVisible(!flag);
        save.setVisible(flag);
        cancel.setVisible(flag);
        requisitesForm.setFieldsReadOnly(!flag);

    }
    private void setSalaryFormEditable(boolean flag) {
        edit.setVisible(!flag);
        save.setVisible(flag);
        cancel.setVisible(flag);
        employeeSalaryForm.setFieldsReadOnly(!flag);

    }
}
