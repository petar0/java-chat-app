# Java Chat Application

> Real-time chat апликација изградена од нула со Java Sockets, 
> Multithreading и JavaFX.

**Статус:** ✅ V1 MVP завршен

## Архитектура

Multi-module Maven проект:

- `chat-common` — споделени модели и JSON протокол (Gson)
- `chat-server` — headless TCP сервер, multithreaded (ExecutorService)
- `chat-client` — JavaFX desktop клиент + конзолен клиент за тестирање

## Технологии

- Java 21
- Maven (multi-module)
- TCP Socket Programming
- Multithreading (ExecutorService thread pool)
- JSON протокол (Gson)
- JavaFX (GUI)
- MySQL + JDBC (V3 — во план)

## Стартување

### Барања
- Java 21+
- Maven 3.8+

### Сервер
```bash
cd chat-server
mvn exec:java -Dexec.mainClass="mk.finki.chat.server.ChatServer"
```
Или директно во IntelliJ: отвори `ChatServer.java` → стартувај `main()`

Конзолата покажува: `Chat серверот слуша на порта 5000...`

### Клиент
Отвори нов IntelliJ прозорец → стартувај го клиентот

## Функционалности (V1 MVP)

- ✅ Login со username
- ✅ Real-time chat (broadcast)
- ✅ Приватни пораки
- ✅ Online корисници листа
- ✅ Join/Leave известувања
- ✅ Timestamps на пораки
- ✅ JavaFX GUI клиент

## Roadmap

- ✅ **V1** — MVP (завршен)
- [ ] **V2** — Chat rooms, emoji, dark/light тема
- [ ] **V3** — Регистрација, BCrypt hashing, MySQL, message history

## Развој

Проектот е граден инкрементално со focus на чиста архитектура:
`chore: skeleton` → `feat: models` → `feat: protocol` → 
`feat: server` → `feat: V1 MVP`
