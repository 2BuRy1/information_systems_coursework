
BEGIN;

TRUNCATE TABLE
    task_data,
    task,
    document_operation,
    document_snapshot,
    cursor_state,
    highlight,
    user_session,
    oauth_credentials,
    document,
    session,
    "user"
RESTART IDENTITY CASCADE;

INSERT INTO public."user" (name, email, avatar_url, role) VALUES
    ('Евгений Фёдоров', 'owner@codetogether.local', 'https://avatars.example/owner.png', 'owner'),
    ('Артём Бондаренко', 'mentor@codetogether.local', 'https://avatars.example/mentor.png', 'mentor'),
    ('София Иванова', 'guest@codetogether.local', NULL, 'guest');

INSERT INTO public.session (name, language, user_id, link, link_expires_at) VALUES
    ('Backend kata', 'Java', 1, 'sess-backend-2025', now() + interval '2 day'),
    ('Frontend warmup', 'TypeScript', 2, 'sess-frontend-2025', now() + interval '1 day');

INSERT INTO public.document (session_id, version, content_text) VALUES
    (1, 3, 'public class Kata {\n    public static void main(String[] args) {\n        System.out.println("Hello");\n    }\n}'),
    (2, 1, 'const root = document.getElementById("root");');

INSERT INTO public.document_snapshot (document_id, version, content_text, user_id) VALUES
    (1, 1, 'public class Kata { }', 1),
    (1, 2, 'public class Kata {\n    public static void main(String[] args) { }\n}', 2),
    (2, 1, 'const root = document.getElementById("root");', 2);

INSERT INTO public.document_operation (document_id, operation_type, node_counter, node_site, left_node, right_node, color, value, version, user_id) VALUES
    (1, 'insert', 10, 1, NULL, NULL, '#ff0000', 'public class Kata { }', 1, 1),
    (1, 'insert', 15, 1, 10, NULL, '#00ff00', 'public static void main', 2, 2),
    (1, 'insert', 20, 1, 15, NULL, '#0000ff', 'System.out.println("Hello");', 3, 1),
    (2, 'insert', 5, 2, NULL, NULL, '#ff9900', 'const root = document.getElementById("root");', 1, 2);

INSERT INTO public.user_session (session_id, user_id, role) VALUES
    (1, 1, 'owner'),
    (1, 2, 'editor'),
    (1, 3, 'viewer'),
    (2, 2, 'owner'),
    (2, 1, 'editor');

INSERT INTO public.cursor_state (session_id, user_id, line, col, color) VALUES
    (1, 1, 2, 5, '#ff0000'),
    (1, 2, 4, 12, '#00ff00'),
    (2, 2, 1, 20, '#ff9900');

INSERT INTO public.highlight (session_id, user_id, start_line, end_line, start_col, end_col, color) VALUES
    (1, 2, 2, 4, 1, 20, 'rgba(0,255,0,0.2)'),
    (1, 3, 5, 5, 1, 15, 'rgba(255,215,0,0.2)');

INSERT INTO public.oauth_credentials (user_id, provider, access_token, refresh_token, scopes, token_expires_at) VALUES
    (1, 'github', 'gho_owner_token', 'ghr_owner_refresh', 'repo, user:email', now() + interval '7 day'),
    (2, 'github', 'gho_mentor_token', 'ghr_mentor_refresh', 'repo, user:email', now() + interval '5 day');

INSERT INTO public.task (session_id, text, status, user_id) VALUES
    (1, 'Настроить OAuth вход', 'in_progress', 2),
    (1, 'Добавить unit-тесты на OT', 'open', 1),
    (2, 'Сверстать панель задач', 'done', 2);

INSERT INTO public.task_data (task_id, payload) VALUES
    (1, '{"type": "checklist", "items": ["REST client", "OAuth callback"]}'::jsonb),
    (2, '{"type": "note", "priority": "high"}'::jsonb),
    (3, '{"type": "ui", "figma": "https://figma.example/task"}'::jsonb);

COMMIT;
