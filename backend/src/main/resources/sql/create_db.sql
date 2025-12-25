BEGIN;

CREATE TABLE IF NOT EXISTS public.users (
    id          SERIAL PRIMARY KEY,
    name        TEXT        NOT NULL,
    email       TEXT        NOT NULL,
    avatar_url  TEXT,
    role        VARCHAR(32) NOT NULL DEFAULT 'member'
);

CREATE TABLE IF NOT EXISTS public.session (
    id               SERIAL PRIMARY KEY,
    name             TEXT           NOT NULL,
    language         VARCHAR(128),
    user_id          INTEGER        NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    link             VARCHAR(256)   NOT NULL,
    link_expires_at  TIMESTAMP      NOT NULL
);

CREATE TABLE IF NOT EXISTS public.document (
    id           SERIAL PRIMARY KEY,
    session_id   INTEGER      NOT NULL REFERENCES public.session(id) ON DELETE CASCADE,
    version      INTEGER      NOT NULL DEFAULT 0,
    content_text TEXT         NOT NULL DEFAULT ''
);

CREATE TABLE IF NOT EXISTS public.document_snapshot (
    id           SERIAL PRIMARY KEY,
    document_id  INTEGER NOT NULL REFERENCES public.document(id) ON DELETE CASCADE,
    version      INTEGER NOT NULL,
    content_text TEXT    NOT NULL,
    user_id      INTEGER NOT NULL REFERENCES public.users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS public.document_operation (
    id             SERIAL PRIMARY KEY,
    document_id    INTEGER      NOT NULL REFERENCES public.document(id) ON DELETE CASCADE,
    operation_type VARCHAR(64),
    node_counter   INTEGER      NOT NULL,
    node_site      INTEGER      NOT NULL,
    left_node      INTEGER,
    right_node     INTEGER,
    color          VARCHAR(30),
    value          TEXT         NOT NULL,
    version        INTEGER      NOT NULL,
    user_id        INTEGER      NOT NULL REFERENCES public.users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS public.highlight (
    session_id  INTEGER     NOT NULL REFERENCES public.session(id) ON DELETE CASCADE,
    user_id     INTEGER     NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    start_line  INTEGER     NOT NULL,
    end_line    INTEGER     NOT NULL,
    start_col   INTEGER     NOT NULL,
    end_col     INTEGER     NOT NULL,
    color       VARCHAR(30) NOT NULL,
    PRIMARY KEY (session_id, user_id)
);

CREATE TABLE IF NOT EXISTS public.cursor_state (
    session_id INTEGER     NOT NULL REFERENCES public.session(id) ON DELETE CASCADE,
    user_id    INTEGER     NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    line       INTEGER     NOT NULL,
    col        INTEGER     NOT NULL,
    color      VARCHAR(30) NOT NULL,
    PRIMARY KEY (session_id, user_id)
);

CREATE TABLE IF NOT EXISTS public.oauth_credentials (
    id               SERIAL PRIMARY KEY,
    user_id          INTEGER       NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    provider         VARCHAR(128)  NOT NULL,
    access_token     VARCHAR(2048),
    refresh_token    VARCHAR(2048),
    scopes           TEXT,
    token_expires_at TIMESTAMP     NOT NULL
);

CREATE TABLE IF NOT EXISTS public.user_session (
    session_id INTEGER     NOT NULL REFERENCES public.session(id) ON DELETE CASCADE,
    user_id    INTEGER     NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    role       VARCHAR(64) NOT NULL DEFAULT 'editor',
    PRIMARY KEY (session_id, user_id)
);

CREATE TABLE IF NOT EXISTS public.task (
    id         SERIAL PRIMARY KEY,
    session_id INTEGER     NOT NULL REFERENCES public.session(id) ON DELETE CASCADE,
    text       TEXT        NOT NULL,
    status     VARCHAR(30) NOT NULL DEFAULT 'open',
    user_id    INTEGER     NOT NULL REFERENCES public.users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS public.task_data (
    id      SERIAL PRIMARY KEY,
    task_id INTEGER NOT NULL REFERENCES public.task(id) ON DELETE CASCADE,
    payload JSONB   NOT NULL
);

-- Индексы
CREATE UNIQUE INDEX IF NOT EXISTS document_session_id_uidx ON public.document(session_id);
CREATE INDEX IF NOT EXISTS document_version_idx ON public.document(version DESC);

CREATE UNIQUE INDEX IF NOT EXISTS user_email_uidx ON public.users(lower(email));
CREATE INDEX IF NOT EXISTS user_name_idx ON public.users(name);

CREATE UNIQUE INDEX IF NOT EXISTS session_link_uidx ON public.session(link);
CREATE INDEX IF NOT EXISTS session_owner_idx ON public.session(user_id);
CREATE INDEX IF NOT EXISTS session_expiration_idx ON public.session(link_expires_at);

CREATE UNIQUE INDEX IF NOT EXISTS user_session_pair_uidx ON public.user_session(session_id, user_id);
CREATE INDEX IF NOT EXISTS user_session_user_idx ON public.user_session(user_id);

CREATE INDEX IF NOT EXISTS document_operation_doc_version_idx ON public.document_operation(document_id, version);
CREATE INDEX IF NOT EXISTS document_operation_doc_node_idx ON public.document_operation(document_id, node_site, node_counter);
CREATE INDEX IF NOT EXISTS document_operation_user_idx ON public.document_operation(user_id);

CREATE UNIQUE INDEX IF NOT EXISTS document_snapshot_doc_version_uidx ON public.document_snapshot(document_id, version);
CREATE INDEX IF NOT EXISTS document_snapshot_user_idx ON public.document_snapshot(user_id);

CREATE INDEX IF NOT EXISTS highlight_session_range_idx ON public.highlight(session_id, start_line, start_col, end_line, end_col);
CREATE INDEX IF NOT EXISTS highlight_user_idx ON public.highlight(user_id);

CREATE INDEX IF NOT EXISTS cursor_state_user_idx ON public.cursor_state(user_id, session_id);

COMMIT;
