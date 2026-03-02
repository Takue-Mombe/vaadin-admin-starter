# Vaadin Admin Pro Kit

A professional **Vaadin 24 + Spring Boot 3** admin UI starter template.
Clone, run, and build on top of it.

---

## Stack

| Layer | Technology |
|---|---|
| UI | Vaadin 24 Flow |
| Backend | Spring Boot 3.2 |
| Database | H2 (dev) / swap for Postgres in prod |
| ORM | Spring Data JPA / Hibernate |
| Validation | Jakarta Bean Validation |
| Security | Spring Security (public routes for MVP) |
| Java | 17+ |
| Build | Maven |

---

## How to run

**Prerequisites:** Java 17+, Maven 3.8+

```bash
# Clone and enter the directory
cd adminpro

# Start in development mode (Vaadin DevTools + live reload)
./mvnw spring-boot:run

# Or with system Maven
mvn spring-boot:run
```

Open **http://localhost:8080** — you'll land on the Dashboard.

**H2 console:** http://localhost:8080/h2-console  
JDBC URL: `jdbc:h2:mem:adminprodb`  Username: `sa`  Password: *(empty)*

---

## Pages

| Route | View | Description |
|---|---|---|
| `/` or `/dashboard` | `DashboardView` | Stat cards + recent activity grid |
| `/users` | `UsersView` | Searchable CRUD grid + add/edit dialog |
| `/settings` | `SettingsView` | Profile form + preference toggles |

---

## Project structure

```
src/main/java/com/adminpro/
  AdminProApplication.java          ← Spring Boot entry point

  ui/
    layout/
      MainLayout.java               ← AppLayout shell (@Theme applied here)
      SideNav.java                  ← Sidebar brand + nav registry + footer
    views/
      dashboard/DashboardView.java
      users/UsersView.java
      settings/SettingsView.java
    components/
      StatCard.java                 ← Metric tile
      PageHeader.java               ← Title + description + actions slot
      ContentCard.java              ← Styled container card

  application/
    DashboardService.java
    UserService.java
    SettingsService.java

  domain/
    User.java                       ← JPA entity + BeanValidation
    Role.java                       ← ADMIN / MANAGER / VIEWER enum
    DashboardStat.java              ← DTO (not persisted)
    UserSettings.java               ← JPA entity (id=1 singleton row)

  infrastructure/
    repo/UserRepository.java
    repo/UserSettingsRepository.java
    seed/DevDataSeeder.java         ← Seeds 10 demo users on startup

  security/
    SecurityConfig.java             ← All routes public (MVP)

src/main/resources/
  application.properties
  themes/adminpro/
    styles.css                      ← Root Lumo token overrides + Inter font
    theme.json
    components/
      sidebar.css
      cards.css
```

---

## How to add a new page

1. Create a new view class:
   ```java
   @Route(value = "reports", layout = MainLayout.class)
   @PageTitle("Reports | AdminPro")
   public class ReportsView extends Div {
       public ReportsView() {
           addClassName("page-content");
           add(new PageHeader("Reports", "Analyse your data."));
           // ... your content
       }
   }
   ```

2. Register it in **`SideNav.java`** — add one line to `NAV_ITEMS`:
   ```java
   new NavEntry("Reports", ReportsView.class, VaadinIcon.CHART)
   ```

That's it. Navigation, active-link highlighting, and the topbar all update automatically.

---

## Swap to a real database (Postgres)

1. Replace the H2 dependency in `pom.xml` with:
   ```xml
   <dependency>
       <groupId>org.postgresql</groupId>
       <artifactId>postgresql</artifactId>
   </dependency>
   ```

2. Update `application.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/adminpro
   spring.datasource.username=your_user
   spring.datasource.password=your_pass
   spring.jpa.hibernate.ddl-auto=update
   ```

---

## Add login (Phase 2)

1. Create a `LoginView` extending `Div` annotated with `@Route("login")`.
2. In `SecurityConfig`, replace the `anyRequest().permitAll()` line with:
   ```java
   setLoginView(http, LoginView.class);
   ```
3. Add `@RolesAllowed("ADMIN")` to views that need protection.
4. Add a `UserDetailsService` bean backed by `UserRepository`.

---

## Production build

```bash
mvn package -Pproduction
java -jar target/adminpro-1.0.0-SNAPSHOT.jar
```

The `production` Maven profile bundles all Vaadin frontend assets via `build-frontend`.
