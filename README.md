# Notification Engine (Spring Boot + MySQL)

A multi-client notification engine. Any client app (e-commerce, bank, ...) registers, subscribes to
vendors, stores its users and their events (order placed, order deleted ...) and sends notifications
through the API. Notifications are saved as QUEUED, a scheduled worker sends them and retries up to
4 times (10s, 20s, 40s, 80s). One log row is written per attempt.

Architecture: controller -> service -> repository (Spring Data JPA / Hibernate) -> MySQL

## Run in VS Code
1. Install JDK 17+, MySQL 8, and the VS Code "Extension Pack for Java" + "Spring Boot Extension Pack".
2. Open this folder (the one with pom.xml) in VS Code.
3. Set your MySQL username/password in `src/main/resources/application.properties`.
   The database `notification_db` is created automatically.
4. Run `NotificationEngineApplication.java` (or `mvn spring-boot:run`).
5. In Postman: Import -> `Notification-Engine.postman_collection.json`.

Demo data is created on first start: X-API-KEY `demo-api-key-123`, user `user-101`,
types `WELCOME_EMAIL` and `OTP_SMS`. Admin key: `admin-secret-123`.

## APIs (client APIs need header X-API-KEY, admin APIs need X-ADMIN-KEY)

| Resource | POST | GET | PUT | DELETE |
|---|---|---|---|---|
| /api/v1/recipients (users) | create | /{id}, list | /{id} | /{id} (also removes their notifications and events) |
| /api/v1/events (order placed, deleted...) | create | /{id}, list (?recipientId=&eventType=) | /{id} | /{id} |
| /api/v1/notification-types | create | /{id}, list | /{id} | /{id} (409 if used, set active=false instead) |
| /api/v1/vendor-subscriptions | create | /{id}, list | /{id} | /{id} |
| /api/v1/notifications | /send, /{id}/retry | /{id} (with logs), list (?status=) | /{id} (only QUEUED) | /{id} |
| /api/v1/vendors | - | list | - | - |
| /api/admin/clients | create | /{id}, list | /{id}, PATCH /{id}/status | /{id} (removes all its data) |
| /api/admin/vendors | create | /{id}, list | /{id} | /{id} (409 if subscribed) |
| /api/admin/notifications | - | /{id}, list | - | - |

Mock senders fail randomly (40%) so you can watch retries in the console.
Set `notification.mock.failure-rate=0.0` for no failures, `1.0` for permanent FAILED.

## Adding a real vendor
Create a class implementing `VendorSender` with its own provider code, add the vendor via
`POST /api/admin/vendors`, then subscribe a client to it.
