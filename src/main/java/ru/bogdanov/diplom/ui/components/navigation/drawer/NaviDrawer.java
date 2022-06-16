package ru.bogdanov.diplom.ui.components.navigation.drawer;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import elemental.json.JsonObject;
import ru.bogdanov.diplom.ui.util.UIUtils;

@CssImport("./styles/components/navi-drawer.css")
@JsModule("./swipe-away.js")
public class NaviDrawer extends Div implements AfterNavigationObserver {

    private String CLASS_NAME = "navi-drawer";
    private String RAIL = "rail";
    private String OPEN = "open";

    private Div scrim;

    private Div mainContent;
    private Div scrollableArea;

    private Button railButton;
    private NaviMenu menu;

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
    }

    @ClientCallable
    public void onSwipeAway(JsonObject data) {
        close();
    }

    public NaviDrawer() {
        setClassName(CLASS_NAME);

        initScrim();
        initMainContent();

        getElement().setAttribute(OPEN, true);

        initScrollableArea();
        initMenu();

        initFooter();
    }

    private void initScrim() {
        // Backdrop on small viewports
        scrim = new Div();
        scrim.addClassName(CLASS_NAME + "__scrim");
        scrim.addClickListener(event -> close());
        add(scrim);
    }

    private void initMainContent() {
        mainContent = new Div();
        mainContent.addClassName(CLASS_NAME + "__content");
        add(mainContent);
    }

    private void initScrollableArea() {
        scrollableArea = new Div();
        scrollableArea.addClassName(CLASS_NAME + "__scroll-area");
        mainContent.add(scrollableArea);
    }

    private void initMenu() {
        menu = new NaviMenu();
        scrollableArea.add(menu);
    }

    private void initFooter() {
        getElement().setAttribute(RAIL, true);
        railButton = UIUtils.createSmallButton("Expand", VaadinIcon.CHEVRON_RIGHT_SMALL);
        railButton.addClassName(CLASS_NAME + "__footer");
        railButton.addClickListener(event -> toggleRailMode());
        railButton.getElement().setAttribute("aria-label", "Expand menu");
        mainContent.add(railButton);
    }

    private void toggleRailMode() {
        if (getElement().hasAttribute(RAIL)) {
            getElement().setAttribute(RAIL, false);
            railButton.setIcon(new Icon(VaadinIcon.CHEVRON_LEFT_SMALL));
            railButton.setText("Collapse");
            UIUtils.setAriaLabel("Collapse menu", railButton);

        } else {
            getElement().setAttribute(RAIL, true);
            railButton.setIcon(new Icon(VaadinIcon.CHEVRON_RIGHT_SMALL));
            railButton.setText("Expand");
            UIUtils.setAriaLabel("Expand menu", railButton);
        }
    }

    public void toggle() {
        if (getElement().hasAttribute(OPEN)) {
            close();
        } else {
            open();
        }
    }

    private void open() {
        getElement().setAttribute(OPEN, true);
    }

    private void close() {
        getElement().setAttribute(OPEN, false);
    }

    public NaviMenu getMenu() {
        return menu;
    }

    @Override
    public void afterNavigation(AfterNavigationEvent afterNavigationEvent) {
        close();
    }

}
