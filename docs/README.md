# 📚 MTNG – Complete Project Documentation Index

## Meeting Management Platform

| Field               | Value                                     |
|---------------------|-------------------------------------------|
| **Project**         | MTNG (Meeting Management Platform)        |
| **Version**         | 0.0.1-SNAPSHOT (MVP)                      |
| **Date Generated**  | March 22, 2026                            |
| **Technology**      | Java 17 · Spring Boot 3.4.3 · H2 Database |

---

## Documentation Suite

| #  | Document                                              | File                              | Description                                      |
|----|-------------------------------------------------------|-----------------------------------|--------------------------------------------------|
| 1  | [SRS / BRS](./01_SRS_BRS_Document.md)                | `01_SRS_BRS_Document.md`          | Software & Business Requirements Specification – functional requirements, non-functional requirements, business rules, acceptance criteria |
| 2  | [High-Level Design (HLD)](./02_HLD_High_Level_Design.md) | `02_HLD_High_Level_Design.md` | System architecture, module decomposition, security architecture, data flow diagrams, deployment architecture |
| 3  | [Low-Level Design (LLD)](./03_LLD_Low_Level_Design.md) | `03_LLD_Low_Level_Design.md`    | Entity designs, repository interfaces, service class designs, controller endpoints, sequence diagrams |
| 4  | [Database Design](./04_Database_Design.md)            | `04_Database_Design.md`           | ERD, table definitions, SQL DDL, indexes, relationships, data access patterns, migration guide |
| 5  | [System Architecture](./05_System_Architecture.md)    | `05_System_Architecture.md`       | Layered architecture, component diagrams, networking, Jitsi integration, security layers, deployment |
| 6  | [Function / API Documentation](./06_Function_API_Documentation.md) | `06_Function_API_Documentation.md` | Complete REST API reference, view controller routes, service layer functions, configuration beans |
| 7  | [Features Incorporated](./07_Features_Incorporated.md) | `07_Features_Incorporated.md`   | 68 features catalog with descriptions, technology matrix, security features, deployment features |

---

## Quick Reference

### Default Credentials
| Account   | Username | Password   | Role       |
|-----------|----------|------------|------------|
| Admin     | admin    | admin123   | ROLE_ADMIN |
| User      | user     | user123    | ROLE_USER  |
| Student 1 | HARI34   | pass1234   | ROLE_USER  |
| Student 2 | PRIYA01  | pass1234   | ROLE_USER  |
| Student 3 | RAM22    | pass1234   | ROLE_USER  |

### Key URLs
| URL                  | Description                  |
|----------------------|------------------------------|
| `http://host:8080/`  | Dashboard (Meeting Controls) |
| `http://host:8080/login` | Login page              |
| `http://host:8080/meeting-room` | Jitsi Meeting Room |
| `http://host:8080/h2-console`   | Database Console (ADMIN) |

### Technology Stack
```
Java 17 → Spring Boot 3.4.3 → Spring Security 6 → Spring Data JPA
→ Hibernate 6 → H2 Database → Thymeleaf 3 → Jitsi Meet (External)
```

---

*Documentation generated from source code analysis of the MTNG project.*

