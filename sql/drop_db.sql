-- Удаление объектов схемы в базе codetogether без удаления самой БД.
\connect codetogether

BEGIN;

DROP TABLE IF EXISTS public.task_data          CASCADE;
DROP TABLE IF EXISTS public.task               CASCADE;
DROP TABLE IF EXISTS public.document_operation CASCADE;
DROP TABLE IF EXISTS public.document_snapshot  CASCADE;
DROP TABLE IF EXISTS public.cursor_state       CASCADE;
DROP TABLE IF EXISTS public.highlight          CASCADE;
DROP TABLE IF EXISTS public.user_session       CASCADE;
DROP TABLE IF EXISTS public.oauth_credentials  CASCADE;
DROP TABLE IF EXISTS public.document           CASCADE;
DROP TABLE IF EXISTS public.session            CASCADE;
DROP TABLE IF EXISTS public."user"            CASCADE;

COMMIT;
