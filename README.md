# Java Chat Application

> Real-time chat апликација изградена од нула со Java Sockets, Multithreading и JavaFX.

**Статус:** 🚧 Во развој — V1 (MVP)

## Архитектура

Multi-module Maven проект:

- `chat-common` — споделени модели и протокол (server ↔ client)
- `chat-server` — headless TCP сервер, multithreaded
- `chat-client` — JavaFX desktop клиент

Детален architecture dijagram доаѓа откако ќе има реални класи за прикажување.

## Технологии

- Java 21
- Maven (multi-module)
- Socket Programming & Multithreading
- JavaFX (GUI)
- MySQL + JDBC (V3)

## Стартување

_(ќе се дополни откако ќе имаме работечки сервер и клиент)_

## Roadmap

- [ ] **V1 — MVP**: login, real-time chat, online users листа, приватни пораки, timestamps, join/leave известувања
- [ ] **V2**: chat rooms, emoji, dark/light тема
- [ ] **V3**: registracija, password hashing, MySQL persistencija, message history

## Развој

Овој проект се гради чекор по чекор, со fokus на чиста архитектура наместо брз "working demo". Секој commit одговара на еден логички чекор од развојот.
