# Vaadin Admin Pro Kit (Arc UI)

A Vaadin 24 + Spring Boot 3 admin starter with:
- Arc-themed responsive UI
- Route-based app shell (sidebar + topbar)
- Full CRUD pages for core admin modules
- Custom login experience + Spring Security authentication

## Stack

| Layer | Technology |
|---|---|
| UI | Vaadin 24 Flow |
| Backend | Spring Boot 3.2 |
| Database | H2 (in-memory for dev) |
| ORM | Spring Data JPA / Hibernate |
| Validation | Jakarta Bean Validation |
| Security | Spring Security + VaadinWebSecurity |
| Java | 17+ |
| Build | Maven |

## Features

- Arc visual design system (Fraunces + DM Sans, responsive layout, light/dark theme)
- Custom login page (`/login`) with branded split layout and client-side validation
- Authenticated app routes with Vaadin access annotations
- Profile management in Settings (`/settings`) for current authenticated user
- CRUD modules:
  - Analytics
  - Users
  - Orders
  - Products
  - Messages
  - Reports

## Run locally

Prerequisites:
- Java 17+
- Maven 3.8+

```bash
mvn clean spring-boot:run
```

Open:
- App: `http://localhost:8080`
- Login: `http://localhost:8080/login`

## Demo credentials

Seeded on startup (`spring.jpa.hibernate.ddl-auto=create-drop`):

- Username: `alice.muller@adminpro.io`
- Password: `admin123`
- Role: `ADMIN`

You can change email/password from `http://localhost:8080/settings`.

## H2 console

- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:adminprodb`
- Username: `sa`
- Password: *(empty)*

Note: `/h2-console/**` is restricted to `ADMIN` in security config.

## Routes

| Route | View | Purpose |
|---|---|---|
| `/login` | `LoginView` | Custom sign-in page |
| `/` or `/dashboard` | `DashboardView` | KPI cards + charts + activity |
| `/analytics` | `AnalyticsView` | Analytics metric CRUD |
| `/users` | `UsersView` | User CRUD |
| `/orders` | `OrdersView` | Order CRUD |
| `/products` | `ProductsView` | Product CRUD |
| `/messages` | `MessagesView` | Support message CRUD |
| `/reports` | `ReportsView` | Report config CRUD |
| `/settings` | `SettingsView` | Current-user profile + preferences |

## Security model

- Security is configured in `src/main/java/com/adminpro/security/SecurityConfig.java`.
- `setLoginView(http, LoginView.class)` enables browser login page flow.
- `http.httpBasic(...)` remains available for API/tooling use.
- Users are loaded from DB by `AppUserDetailsService`.
- Passwords are BCrypt-hashed (`PasswordEncoder` bean).

Vaadin route access is enforced with annotations (`@PermitAll` on app views, `@AnonymousAllowed` on `LoginView`).

## Project structure

```text
src/main/java/com/adminpro/
  AdminProApplication.java

  ui/
    AppShell.java
    layout/
      MainLayout.java
      SideNav.java
    views/
      login/LoginView.java
      dashboard/DashboardView.java
      analytics/AnalyticsView.java
      users/UsersView.java
      orders/OrdersView.java
      products/ProductsView.java
      messages/MessagesView.java
      reports/ReportsView.java
      settings/SettingsView.java

  domain/
    User.java
    UserSettings.java
    AnalyticsMetric.java
    OrderRecord.java
    ProductItem.java
    SupportMessage.java
    ReportConfig.java
    Role.java

  application/
    UserService.java
    SettingsService.java
    DashboardService.java
    AnalyticsService.java
    OrderService.java
    ProductService.java
    MessageService.java
    ReportService.java

  infrastructure/
    repo/...
    seed/DevDataSeeder.java

  security/
    SecurityConfig.java
    AppUserDetailsService.java
    CurrentUserService.java

src/main/frontend/themes/adminpro/
  styles.css
  theme.json
  components/
    sidebar.css
    cards.css
    login.css
```

## Seed data behavior

- Seeders run on startup via `DevDataSeeder`.
- Because schema is `create-drop`, data resets on every restart.
- Users, settings, analytics, orders, products, messages, and reports are all pre-populated.

## Production build

```bash
mvn package -Pproduction
java -jar target/adminpro-1.0.0-SNAPSHOT.jar
```

The `production` profile builds and bundles Vaadin frontend assets.
