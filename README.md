# CodeTogether (information_systems_coursework)

Учебный проект “совместная доска”: пользователи создают “доски” (sessions), совместно редактируют документ, видят курсоры/выделения друг друга, ведут список задач.

## Состав

- `backend/` — Spring Boot API + WebSocket (STOMP/SockJS), PostgreSQL, JPA.
- `front/` — React + Vite, Monaco Editor.
- `openapi.yaml` — OpenAPI спецификация.

## Быстрый старт (Docker)

1) Скопируй переменные окружения:
   - `cp .env.example .env`
2) Запусти сервисы:
   - `docker compose up --build`
3) Открой:
   - Frontend: `http://localhost:5173`

Примечание: без реальных OAuth-ключей логин через GitHub/Google работать не будет.

## Запуск без Docker (локально)

Backend:
- `cd backend && ./gradlew bootRun`

Frontend:
- `cd front && npm install && npm run dev`

## Конфигурация (env)

Backend читает конфиг из переменных окружения (см. `.env.example`), включая:
- `DB_URL`, `DB_USER`, `DB_PASSWORD`
- `GITHUB_CLIENT_ID`, `GITHUB_CLIENT_SECRET`, `GITHUB_REDIRECT_URI`
- `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `GOOGLE_REDIRECT_URI`
