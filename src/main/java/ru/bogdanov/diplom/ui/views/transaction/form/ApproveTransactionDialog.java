package ru.bogdanov.diplom.ui.views.transaction.form;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.PropertyId;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.validator.RegexpValidator;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.bogdanov.diplom.backend.data.containers.*;
import ru.bogdanov.diplom.backend.data.enums.TransactionStatus;
import ru.bogdanov.diplom.backend.data.enums.UserRoleName;
import ru.bogdanov.diplom.backend.service.IEmployeeService;
import ru.bogdanov.diplom.backend.service.ITransactionService;
import ru.bogdanov.diplom.backend.service.IUserService;
import ru.bogdanov.diplom.ui.util.LumoStyles;
import ru.bogdanov.diplom.ui.util.UIUtils;
import ru.bogdanov.diplom.ui.util.converter.BigDecimalToLongConverter;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.function.Consumer;

@Slf4j
public class ApproveTransactionDialog extends Dialog {

    @Getter
    private BeanValidationBinder<Requisites> binderRequisites;
    @Getter
    private BeanValidationBinder<Transaction> binderTransaction;

    @Setter
    private Consumer<Transaction> approveAction;
    @Setter
    private Consumer<Transaction> declineAction;
    @Setter
    private Consumer<Transaction> cancelAction;

    private Transaction transaction;
    private Requisites requisites;

    @PropertyId("totalSum")
    private BigDecimalField totalSum = new BigDecimalField("Сумма запроса");

    @PropertyId("firstName")
    private TextField firstNameField = new TextField("Имя");
    @PropertyId("lastName")
    private TextField lastNameField = new TextField("Фамилия");
    @PropertyId("patronymicName")
    private TextField patronymicNameField = new TextField("Отчество");


    private FormLayout transactionLayout = new FormLayout();
    private FormLayout fioLayout = new FormLayout();

    @Setter
    private ITransactionService transactionService;

    private Button approve,decline,cancel;


    public void init() {
        setWidth("60%");

        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        this.transaction = Transaction.builder().build();
        this.requisites = new Requisites();
        this.binderTransaction = new BeanValidationBinder<>(Transaction.class);
        this.binderTransaction.setBean(this.transaction);
        this.binderTransaction.bindInstanceFields(this);

        this.binderRequisites = new BeanValidationBinder<>(Requisites.class);
        this.binderRequisites.setBean(this.requisites);
        this.binderRequisites.bindInstanceFields(this);

        VerticalLayout layout = new VerticalLayout();

        layout.add(createForm());
        layout.add(createButtons());
        add(layout);
    }
    private Component createAmount(Transaction transaction) {
        Double amount = transaction.getTotalSum().movePointLeft(2).doubleValue();
        return UIUtils.createAmountLabel(amount);
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


        fioLayout.add(firstNameField);
        fioLayout.add(lastNameField);
        fioLayout.add(patronymicNameField);
        fioLayout.addClassNames(LumoStyles.Padding.Bottom.XS,
                LumoStyles.Padding.Horizontal.L, LumoStyles.Padding.Top.XS);
        fioLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP),
                new FormLayout.ResponsiveStep("600px", 1,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP),
                new FormLayout.ResponsiveStep("1024px", 3,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP));


        transactionLayout.add(totalSum);
        transactionLayout.addClassNames(LumoStyles.Padding.Bottom.XS,
                LumoStyles.Padding.Horizontal.L, LumoStyles.Padding.Top.XS);
        transactionLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP),
                new FormLayout.ResponsiveStep("600px", 1,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP),
                new FormLayout.ResponsiveStep("1024px", 3,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP));

        verticalLayout.add(dataLayout);
        verticalLayout.add(fioLayout);
        verticalLayout.add(transactionLayout);
        return verticalLayout;
    }


    private HorizontalLayout createButtons() {
        HorizontalLayout layout = new HorizontalLayout();

        approve = UIUtils.createPrimaryButton("Подтвердить");
        approve.addClickListener(event -> {
            Transaction transaction = this.binderTransaction.getBean();
            transactionService.approve(transaction);
            approveAction.accept(transaction);
            this.close();
        });
        decline = UIUtils.createRedButton("Отклонить");
        decline.addClickListener(event -> {
            Transaction transaction = this.binderTransaction.getBean();
            transactionService.decline(transaction);
            declineAction.accept(transaction);
            this.close();
        });

       cancel = UIUtils.createTertiaryButton("Отменить");
       cancel.addClickListener(event -> {
            cancelAction.accept(transaction);

            this.close();
        });

        layout.add(approve);
        layout.add(decline);
        layout.add(cancel);
        return layout;
    }
    public void withBean(Requisites requisites, Transaction transaction) {
        this.binderTransaction.removeBean();
        this.binderTransaction.setBean(transaction);
        this.binderTransaction.bindInstanceFields(this);
        this.binderRequisites.removeBean();
        this.binderRequisites.setBean(requisites);
        this.binderRequisites.bindInstanceFields(this);
        if (!transaction.getStatus().name().equals(TransactionStatus.AWAITING_CONFORMATION.name())) {
            approve.setVisible(false);
            decline.setVisible(false);
        }
        totalSum.setValue(totalSum.getValue().movePointLeft(2));
        totalSum.setReadOnly(true);
        firstNameField.setReadOnly(true);
        lastNameField.setReadOnly(true);
        patronymicNameField.setReadOnly(true);
    }

}
