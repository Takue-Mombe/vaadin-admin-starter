package com.adminpro.ui.views.login;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.spring.security.AuthenticationContext;

@Route("login")
@PageTitle("Sign In | Arc Admin")
@AnonymousAllowed
public class LoginView extends Div implements BeforeEnterObserver {

    private final AuthenticationContext authenticationContext;

    private final Div loginView = new Div();
    private final Div forgotView = new Div();
    private final Div successView = new Div();
    private final Div signupView = new Div();

    private final Div authError = new Div("Invalid email or password.");

    private final Input resetEmail = new Input();
    private final Input signupEmail = new Input();

    private final NativeButton themeButton = new NativeButton();

    private boolean darkTheme;

    public LoginView(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;

        addClassName("login-page");
        setSizeFull();

        configureThemeToggle();

        Div split = new Div();
        split.addClassName("split");
        split.add(buildLeftPanel(), buildRightPanel());

        add(themeButton, split);

        showView(loginView);
        configureClientValidation();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (authenticationContext.isAuthenticated()) {
            event.forwardTo("");
            return;
        }

        boolean hasError = event.getLocation()
            .getQueryParameters()
            .getParameters()
            .containsKey("error");

        authError.getElement().getClassList().set("show", hasError);
        if (hasError) {
            showView(loginView);
        }
    }

    private void configureThemeToggle() {
        themeButton.addClassName("theme-btn");
        themeButton.getElement().setAttribute("title", "Toggle theme");

        Icon sun = VaadinIcon.SUN_O.create();
        sun.addClassNames("theme-icon", "sun-icon");

        Icon moon = VaadinIcon.MOON_O.create();
        moon.addClassNames("theme-icon", "moon-icon");

        themeButton.add(sun, moon);
        themeButton.addClickListener(event -> {
            darkTheme = !darkTheme;
            getElement().executeJs(
                "document.documentElement.setAttribute('data-theme', $0)",
                darkTheme ? "dark" : "light"
            );
            themeButton.getElement().getClassList().set("dark", darkTheme);
        });
    }

    private Div buildLeftPanel() {
        Div panelLeft = new Div();
        panelLeft.addClassName("panel-left");

        Div brand = new Div();
        brand.addClassName("brand");

        Div brandIcon = new Div();
        brandIcon.addClassName("brand-icon");
        brandIcon.setText("A");

        Span brandName = new Span("Arc");
        brandName.addClassName("brand-name");

        brand.add(brandIcon, brandName);

        Div body = new Div();
        body.addClassName("panel-left-body");

        Div tagline = new Div();
        tagline.addClassName("left-tagline");
        tagline.getElement().setProperty(
            "innerHTML",
            "Your workspace,<br/><em>under control.</em>"
        );

        Paragraph description = new Paragraph(
            "Arc gives your team a single, calm place to manage users, track revenue, "
                + "and understand what is working without the noise."
        );
        description.addClassName("left-desc");

        Div statRow = new Div();
        statRow.addClassName("stat-row");
        statRow.add(
            statCell("3.8K", "Active users"),
            statCell("$84K", "Monthly rev."),
            statCell("99.9%", "Uptime")
        );

        body.add(tagline, description, statRow);

        Div footer = new Div();
        footer.addClassName("panel-left-footer");
        footer.setText("© 2026 Arc Admin. All rights reserved.");

        panelLeft.add(brand, body, footer);
        return panelLeft;
    }

    private Div statCell(String value, String label) {
        Div cell = new Div();
        cell.addClassName("stat-cell");

        Div valueEl = new Div();
        valueEl.addClassName("stat-cell-val");
        valueEl.setText(value);

        Div labelEl = new Div();
        labelEl.addClassName("stat-cell-label");
        labelEl.setText(label);

        cell.add(valueEl, labelEl);
        return cell;
    }

    private Div buildRightPanel() {
        Div panelRight = new Div();
        panelRight.addClassName("panel-right");

        Div shell = new Div();
        shell.addClassName("form-shell");

        buildLoginView();
        buildForgotView();
        buildSuccessView();
        buildSignupView();

        shell.add(loginView, forgotView, successView, signupView);
        panelRight.add(shell);
        return panelRight;
    }

    private void buildLoginView() {
        loginView.setId("login-view");

        Div head = new Div();
        head.addClassName("form-head");

        Div title = new Div();
        title.addClassName("form-title");
        title.setText("Sign in");

        Div sub = new Div();
        sub.addClassName("form-sub");
        sub.setText("No account? ");

        NativeButton requestAccess = linkButton("Request access", this::showSignup);
        requestAccess.addClassName("inline-link");
        sub.add(requestAccess);

        head.add(title, sub);

        HtmlForm form = new HtmlForm();
        form.setId("loginForm");
        form.getElement().setAttribute("method", "post");
        form.getElement().setAttribute("action", "login");
        form.getElement().setAttribute("novalidate", true);

        form.add(buildEmailField());
        form.add(buildPasswordField());
        form.add(buildFormRow());
        form.add(buildSubmitButton());

        Div divider = new Div();
        divider.addClassName("or-divider");
        divider.add(
            line(),
            textSpan("or continue with", "or-text"),
            line()
        );

        Div ssoRow = new Div();
        ssoRow.addClassName("sso-row");
        ssoRow.add(
            simpleButton("Google", "sso-btn"),
            simpleButton("Microsoft", "sso-btn"),
            simpleButton("SSO", "sso-btn")
        );

        Div footer = new Div();
        footer.addClassName("form-footer");
        footer.getElement().setProperty(
            "innerHTML",
            "By signing in you agree to our <a href='#'>Terms of Service</a> and <a href='#'>Privacy Policy</a>."
        );

        loginView.add(head, form, divider, ssoRow, footer);
    }

    private Div buildEmailField() {
        Div field = new Div();
        field.addClassName("field");

        NativeLabel label = new NativeLabel("Email address");
        label.setFor("email");
        label.addClassName("field-label");

        Div wrap = new Div();
        wrap.addClassName("field-wrap");

        Input input = new Input();
        input.setId("email");
        input.setType("email");
        input.addClassName("field-input");
        input.setPlaceholder("you@company.com");
        input.getElement().setAttribute("name", "username");
        input.getElement().setAttribute("autocomplete", "username");
        input.getElement().setAttribute("required", true);

        wrap.add(input);

        Div error = new Div("Please enter a valid email address.");
        error.setId("email-err");
        error.addClassName("field-error");

        field.add(label, wrap, error);
        return field;
    }

    private Div buildPasswordField() {
        Div field = new Div();
        field.addClassName("field");

        NativeLabel label = new NativeLabel("Password");
        label.setFor("password");
        label.addClassName("field-label");

        Div wrap = new Div();
        wrap.addClassName("field-wrap");

        Input input = new Input();
        input.setId("password");
        input.setType("password");
        input.addClassName("field-input");
        input.setPlaceholder("••••••••");
        input.getElement().setAttribute("name", "password");
        input.getElement().setAttribute("autocomplete", "current-password");
        input.getElement().setAttribute("required", true);
        input.getElement().setAttribute("minlength", "8");

        NativeButton eyeButton = new NativeButton();
        eyeButton.setId("eyeBtn");
        eyeButton.addClassName("eye-btn");
        eyeButton.getElement().setAttribute("type", "button");
        eyeButton.getElement().setAttribute("aria-label", "Toggle password");

        Icon eyeShow = VaadinIcon.EYE.create();
        eyeShow.setId("eye-show");

        Icon eyeHide = VaadinIcon.EYE_SLASH.create();
        eyeHide.setId("eye-hide");
        eyeHide.getStyle().set("display", "none");

        eyeButton.add(eyeShow, eyeHide);
        wrap.add(input, eyeButton);

        Div error = new Div("Password must be at least 8 characters.");
        error.setId("pw-err");
        error.addClassName("field-error");

        authError.setId("auth-err");
        authError.addClassName("field-error");

        field.add(label, wrap, error, authError);
        return field;
    }

    private Div buildFormRow() {
        Div row = new Div();
        row.addClassName("form-row");

        Div checkWrap = new Div();
        checkWrap.addClassName("check-wrap");

        Input remember = new Input();
        remember.setId("remember");
        remember.setType("checkbox");
        remember.getElement().setAttribute("name", "remember-me");

        Span checkLabel = textSpan("Remember me", "check-label");
        checkWrap.add(remember, checkLabel);

        NativeButton forgotButton = linkButton("Forgot password?", this::showForgot);
        forgotButton.addClassName("forgot-link");

        row.add(checkWrap, forgotButton);
        return row;
    }

    private NativeButton buildSubmitButton() {
        NativeButton submit = new NativeButton();
        submit.setId("submitBtn");
        submit.addClassName("btn-submit");
        submit.getElement().setAttribute("type", "submit");

        Span label = new Span("Sign in");
        label.setId("btn-label");

        Div spinner = new Div();
        spinner.setId("spinner");
        spinner.addClassName("spinner");

        submit.add(label, spinner);
        return submit;
    }

    private void buildForgotView() {
        forgotView.setId("forgot-view");
        forgotView.addClassName("forgot-shell");

        NativeButton back = linkButton("Back to sign in", this::showLogin);
        back.addClassName("back-btn");

        Div head = new Div();
        head.addClassName("form-head");
        head.add(
            textDiv("Reset password", "form-title"),
            textDiv("We will send a reset link to your email address.", "form-sub")
        );

        Div field = new Div();
        field.addClassName("field");

        NativeLabel label = new NativeLabel("Email address");
        label.addClassName("field-label");
        label.setFor("reset-email");

        resetEmail.setId("reset-email");
        resetEmail.setType("email");
        resetEmail.addClassName("field-input");
        resetEmail.setPlaceholder("you@company.com");

        field.add(label, resetEmail);

        NativeButton sendReset = simpleButton("Send reset link", "btn-submit");
        sendReset.getStyle().set("margin-top", "8px");
        sendReset.addClickListener(event -> sendReset());

        forgotView.add(back, head, field, sendReset);
    }

    private void buildSuccessView() {
        successView.setId("success-view");
        successView.addClassName("success-shell");

        Div iconWrap = new Div(VaadinIcon.CHECK.create());
        iconWrap.addClassName("success-icon");

        Div title = textDiv("Check your inbox", "success-title");

        Paragraph sub = new Paragraph(
            "We sent a password reset link to your email address. It expires in 15 minutes."
        );
        sub.addClassName("success-sub");

        NativeButton back = linkButton("Back to sign in", this::showLogin);
        back.addClassName("back-btn");
        back.getStyle().set("margin-top", "24px").set("justify-content", "center").set("width", "100%");

        successView.add(iconWrap, title, sub, back);
    }

    private void buildSignupView() {
        signupView.setId("signup-view");
        signupView.addClassName("forgot-shell");

        NativeButton back = linkButton("Back to sign in", this::showLogin);
        back.addClassName("back-btn");

        Div head = new Div();
        head.addClassName("form-head");
        head.add(
            textDiv("Request access", "form-title"),
            textDiv("Arc is invite-only. Enter your work email and we will reach out within 24 hours.", "form-sub")
        );

        Div emailField = new Div();
        emailField.addClassName("field");
        NativeLabel emailLabel = new NativeLabel("Work email");
        emailLabel.addClassName("field-label");
        signupEmail.setType("email");
        signupEmail.addClassName("field-input");
        signupEmail.setPlaceholder("you@company.com");
        emailField.add(emailLabel, signupEmail);

        Div companyField = new Div();
        companyField.addClassName("field");
        NativeLabel companyLabel = new NativeLabel("Company name");
        companyLabel.addClassName("field-label");
        Input company = new Input();
        company.setType("text");
        company.addClassName("field-input");
        company.setPlaceholder("Acme Corp");
        companyField.add(companyLabel, company);

        NativeButton request = simpleButton("Request access", "btn-submit");
        request.getStyle().set("margin-top", "8px");
        request.addClickListener(event -> showView(successView));

        signupView.add(back, head, emailField, companyField, request);
    }

    private void configureClientValidation() {
        getElement().executeJs("""
            const root = this;
            const eyeBtn = root.querySelector('#eyeBtn');
            const password = root.querySelector('#password');
            const eyeShow = root.querySelector('#eye-show');
            const eyeHide = root.querySelector('#eye-hide');

            if (eyeBtn && !eyeBtn.__bound) {
              eyeBtn.__bound = true;
              eyeBtn.addEventListener('click', () => {
                const isText = password.type === 'text';
                password.type = isText ? 'password' : 'text';
                eyeShow.style.display = isText ? 'block' : 'none';
                eyeHide.style.display = isText ? 'none' : 'block';
              });
            }

            const form = root.querySelector('#loginForm');
            if (form && !form.__bound) {
              form.__bound = true;

              const email = root.querySelector('#email');
              const pw = root.querySelector('#password');
              const emailErr = root.querySelector('#email-err');
              const pwErr = root.querySelector('#pw-err');
              const authErr = root.querySelector('#auth-err');
              const submitBtn = root.querySelector('#submitBtn');
              const label = root.querySelector('#btn-label');
              const spinner = root.querySelector('#spinner');

              const clearErrors = () => {
                email.classList.remove('error');
                pw.classList.remove('error');
                emailErr.classList.remove('show');
                pwErr.classList.remove('show');
                if (authErr) authErr.classList.remove('show');
              };

              email.addEventListener('input', clearErrors);
              pw.addEventListener('input', clearErrors);

              form.addEventListener('submit', (e) => {
                e.preventDefault();
                clearErrors();

                let valid = true;
                const emailRe = /^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$/;
                if (!emailRe.test((email.value || '').trim())) {
                  email.classList.add('error');
                  emailErr.classList.add('show');
                  valid = false;
                }

                if (!pw.value || pw.value.length < 8) {
                  pw.classList.add('error');
                  pwErr.classList.add('show');
                  valid = false;
                }

                if (!valid) {
                  return;
                }

                submitBtn.disabled = true;
                label.style.display = 'none';
                spinner.style.display = 'block';

                setTimeout(() => form.submit(), 180);
              });
            }
        """);
    }

    private void showForgot() {
        showView(forgotView);
    }

    private void showSignup() {
        showView(signupView);
    }

    private void showLogin() {
        showView(loginView);
    }

    private void sendReset() {
        String value = resetEmail.getValue() == null ? "" : resetEmail.getValue().trim();
        if (!value.contains("@") || !value.contains(".")) {
            resetEmail.addClassName("error");
            return;
        }
        resetEmail.removeClassName("error");
        showView(successView);
    }

    private void showView(Div target) {
        Div[] views = new Div[] {loginView, forgotView, successView, signupView};
        for (Div view : views) {
            boolean show = view == target;
            view.getStyle().set("display", show ? "block" : "none");
            view.getElement().getClassList().set("show", show);
        }
    }

    private Div line() {
        Div line = new Div();
        line.addClassName("or-line");
        return line;
    }

    private Span textSpan(String text, String className) {
        Span span = new Span(text);
        span.addClassName(className);
        return span;
    }

    private Div textDiv(String text, String className) {
        Div div = new Div();
        div.addClassName(className);
        div.setText(text);
        return div;
    }

    private NativeButton simpleButton(String text, String className) {
        NativeButton button = new NativeButton(text);
        button.addClassName(className);
        button.getElement().setAttribute("type", "button");
        return button;
    }

    private NativeButton linkButton(String text, Runnable action) {
        NativeButton button = new NativeButton(text);
        button.getElement().setAttribute("type", "button");
        button.addClickListener(event -> action.run());
        return button;
    }

    @Tag("form")
    private static class HtmlForm extends Component implements HasComponents {
    }
}
