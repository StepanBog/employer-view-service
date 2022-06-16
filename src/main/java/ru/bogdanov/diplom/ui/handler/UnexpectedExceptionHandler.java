package ru.bogdanov.diplom.ui.handler;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.InternalServerError;
import com.vaadin.flow.router.ParentLayout;
import lombok.extern.slf4j.Slf4j;
import ru.bogdanov.diplom.ui.EmployerMainLayout;
import ru.bogdanov.diplom.utils.NotificationUtils;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;

@Slf4j
@ParentLayout(EmployerMainLayout.class)
public class UnexpectedExceptionHandler extends InternalServerError {
    @Override
    public int setErrorParameter(BeforeEnterEvent event,
                                 ErrorParameter<Exception> parameter) {
        log.error("ExternalServerError: at :" + LocalDateTime.now() + " "
                + parameter.getCaughtException().getCause()
                + " The message: " + parameter.getCaughtException().getMessage());
        UI.getCurrent().access(() -> NotificationUtils.showNotificationOnExternalServerError(event));
        return HttpServletResponse.SC_NOT_FOUND;
    }
}
