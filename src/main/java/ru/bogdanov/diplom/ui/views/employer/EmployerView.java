package ru.bogdanov.diplom.ui.views.employer;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinSession;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.bogdanov.diplom.backend.data.containers.Employer;
import ru.bogdanov.diplom.backend.data.containers.Requisites;
import ru.bogdanov.diplom.backend.data.containers.Transaction;
import ru.bogdanov.diplom.backend.data.containers.User;
import ru.bogdanov.diplom.backend.service.IEmployeeService;
import ru.bogdanov.diplom.backend.service.IEmployerService;
import ru.bogdanov.diplom.backend.service.ITransactionService;
import ru.bogdanov.diplom.backend.service.IUserService;
import ru.bogdanov.diplom.ui.EmployerMainLayout;
import ru.bogdanov.diplom.ui.components.FlexBoxLayout;
import ru.bogdanov.diplom.ui.components.navigation.bar.AppBar;
import ru.bogdanov.diplom.ui.handler.SaveButtonErrorHandler;
import ru.bogdanov.diplom.ui.layout.size.Horizontal;
import ru.bogdanov.diplom.ui.layout.size.Vertical;
import ru.bogdanov.diplom.ui.util.UIUtils;
import ru.bogdanov.diplom.ui.views.ViewFrame;
import ru.bogdanov.diplom.ui.views.employee.EmployeeGridWithFilter;
import ru.bogdanov.diplom.ui.views.employer.form.EmployerSettingsForm;
import ru.bogdanov.diplom.ui.views.transaction.TransactionGridWithFilter;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@PageTitle("Работодатель")
@Route(value = EmployerView.ROUTE, layout = EmployerMainLayout.class)
@RequiredArgsConstructor
public class EmployerView extends ViewFrame implements BeforeEnterObserver {

    public static final String ROUTE = "employer-details";

    private final IEmployerService employerService;
    private final IEmployeeService employeeService;
    private final ITransactionService transactionService;
    private final IUserService userService;

    private Map<String, VerticalLayout> tabLayoutMap;
    private Employer employer;
    private Button save, cancel, edit;

    private final EmployerSettingsForm employerSettingsForm = new EmployerSettingsForm();
    private final EmployeeGridWithFilter employeeGridWithFilter = new EmployeeGridWithFilter();
    private final TransactionGridWithFilter transactionGridWithFilter = new TransactionGridWithFilter();


    @PostConstruct
    public void init() {
        save  = UIUtils.createPrimaryButton("Сохранить");
        cancel = UIUtils.createTertiaryButton("Отменить");
        edit = UIUtils.createPrimaryButton("Редактировать");
        setFormEditable(false);

        save.addClickListener(this::onSaveEvent);

        cancel.addClickListener(event -> {
            setFormEditable(false);
            getEmployer();
        });
        cancel.addClickShortcut(Key.ESCAPE);

        edit.addClickListener(event -> {
            setFormEditable(true);
        });

        HorizontalLayout buttonLayout = new HorizontalLayout(save, cancel, edit);
        buttonLayout.setSpacing(true);
        buttonLayout.setPadding(true);

        setViewContent(createContent());
        setViewFooter(buttonLayout);
    }

    private void onSaveEvent(ClickEvent<Button> event) {
        Employer employer = employerSettingsForm.getBinder().getBean();
        employerService.save(employer);
        setFormEditable(false);
    }

    private void setFormEditable(boolean flag) {
        edit.setVisible(!flag);
        save.setVisible(flag);
        cancel.setVisible(flag);
        employerSettingsForm.setFieldsReadOnly(!flag);

    }

    private Component createContent() {
        FlexBoxLayout content = new FlexBoxLayout(createEmployerUI());
        content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        content.setMargin(Horizontal.XS, Vertical.XS);
        content.setSizeFull();
        return content;
    }
    private Component createEmployerUI() {


        employerSettingsForm.setEmployeeService(employeeService);
        employerSettingsForm.init();
        employerSettingsForm.setVisible(true);

        employeeGridWithFilter.setEmployeeService(employeeService);
        employeeGridWithFilter.setUserService(userService);
        employeeGridWithFilter.setEmployerService(employerService);
        employeeGridWithFilter.init();
        employeeGridWithFilter.setVisible(false);

        transactionGridWithFilter.setTransactionService(transactionService);
        transactionGridWithFilter.setEmployeeService(employeeService);
        transactionGridWithFilter.init();
        transactionGridWithFilter.setVisible(false);

        tabLayoutMap =
                Map.of(
                        EmployerSettingsForm.ID, employerSettingsForm,
                        EmployeeGridWithFilter.ID, employeeGridWithFilter,
                        TransactionGridWithFilter.ID, transactionGridWithFilter
                );

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSizeFull();
        verticalLayout.setPadding(false);
        verticalLayout.setMargin(false);
        verticalLayout.setSpacing(false);
        verticalLayout.add(
                employerSettingsForm,
                employeeGridWithFilter,
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
        UI.getCurrent().getPage().setTitle(employer.getName());
    }

    private AppBar initAppBar() {
        AppBar appBar = EmployerMainLayout.get().getAppBar();

        appBar.addTab(createTab(EmployerSettingsForm.ID, VaadinIcon.MODAL_LIST.create(), "Реквизиты"));
        appBar.addTab(createTab(EmployeeGridWithFilter.ID, VaadinIcon.MONEY.create(), "Работники"));
        appBar.addTab(createTab(TransactionGridWithFilter.ID, VaadinIcon.BOOK.create(), "Запросы"));
        appBar.centerTabs();
        tabLayoutMap.get(EmployerSettingsForm.ID).setVisible(true);
        appBar.addTabSelectionListener(event -> {
            tabLayoutMap.get(event.getPreviousTab().getId().orElse("")).setVisible(false);
            tabLayoutMap.get(event.getSelectedTab().getId().orElse("")).setVisible(true);
            if (tabLayoutMap.get(EmployerSettingsForm.ID).isVisible()) {
                if (employerSettingsForm.isEditable()) {
                    save.setVisible(true);
                    cancel.setVisible(true);
                } else {
                    edit.setVisible(true);
                }
            } else {
                edit.setVisible(false);
                save.setVisible(false);
                cancel.setVisible(false);
            }
        });


        return appBar;
    }
    private void getEmployer() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID employerId = UUID.fromString(user.getEmployerId());
        employer = employerService.findById(employerId);
    }
    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID employerId = UUID.fromString(user.getEmployerId());
        employer = employerService.findById(employerId);
        employerSettingsForm.withBean(employer);
    }

}
