-- Checklist Orbis: esquema PROPOSTO, nao aplicado em projeto remoto.
-- Revisar antes de executar em projeto Supabase dedicado ao Checklist Orbis.
create table if not exists public.orbis_tasks (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  client_id text not null,
  payload jsonb not null default '{}'::jsonb,
  revision bigint not null default 1 check (revision > 0),
  updated_at timestamptz not null default now(),
  deleted_at timestamptz,
  unique (user_id, client_id),
  unique (id, user_id)
);
create index if not exists orbis_tasks_owner_updated_idx on public.orbis_tasks(user_id, updated_at);
alter table public.orbis_tasks enable row level security;
create policy "orbis_tasks_select_owner" on public.orbis_tasks for select to authenticated
 using ((select auth.uid()) = user_id);
create policy "orbis_tasks_insert_owner" on public.orbis_tasks for insert to authenticated
 with check ((select auth.uid()) = user_id);
create policy "orbis_tasks_update_owner" on public.orbis_tasks for update to authenticated
 using ((select auth.uid()) = user_id)
 with check ((select auth.uid()) = user_id);
create policy "orbis_tasks_delete_owner" on public.orbis_tasks for delete to authenticated
 using ((select auth.uid()) = user_id);

create table if not exists public.orbis_attachments (
 id uuid primary key default gen_random_uuid(),
 user_id uuid not null references auth.users(id) on delete cascade,
 task_id uuid not null,
 object_path text not null,
 mime_type text not null check (mime_type like 'image/%' or mime_type like 'video/%'),
 size_bytes bigint not null check (size_bytes >= 0),
 created_at timestamptz not null default now(),
 unique (user_id, object_path),
 foreign key (task_id, user_id) references public.orbis_tasks(id, user_id) on delete cascade,
 check (object_path like user_id::text || '/%')
);
alter table public.orbis_attachments enable row level security;
create policy "orbis_attachments_select_owner" on public.orbis_attachments for select to authenticated
 using ((select auth.uid()) = user_id);
create policy "orbis_attachments_insert_owner" on public.orbis_attachments for insert to authenticated
 with check ((select auth.uid()) = user_id);
create policy "orbis_attachments_delete_owner" on public.orbis_attachments for delete to authenticated
 using ((select auth.uid()) = user_id);

insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values ('orbis-private-media', 'orbis-private-media', false, 20971520,
 array['image/jpeg','image/png','image/webp','image/heic','video/mp4','video/webm'])
on conflict (id) do nothing;

create policy "orbis_media_select_owner" on storage.objects for select to authenticated
 using (bucket_id = 'orbis-private-media' and (storage.foldername(name))[1] = (select auth.uid())::text);
create policy "orbis_media_insert_owner" on storage.objects for insert to authenticated
 with check (bucket_id = 'orbis-private-media' and (storage.foldername(name))[1] = (select auth.uid())::text);
create policy "orbis_media_delete_owner" on storage.objects for delete to authenticated
 using (bucket_id = 'orbis-private-media' and (storage.foldername(name))[1] = (select auth.uid())::text);
-- Sem UPDATE de objetos: usar nomes imutaveis e upload sem upsert.
-- Nao incluir service_role no aplicativo. Nao criar bucket publico.
