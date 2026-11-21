-- Checklist Orbis: testes de seguranca do esquema PROPOSTO.
-- Executar SOMENTE em projeto de testes dedicado, depois de aplicar o esquema.
-- Este script NAO cria usuarios, NAO altera dados e NAO substitui testes reais com 2 contas.
-- Falha deliberadamente quando alguma premissa estrutural nao e atendida.
do $$
declare n integer;
begin
  select count(*) into n from pg_class c join pg_namespace s on s.oid=c.relnamespace
  where s.nspname='public' and c.relname in ('orbis_tasks','orbis_attachments')
    and c.relkind='r' and c.relrowsecurity and not c.relforcerowsecurity is false;
  -- Verificacao simples e explicita abaixo, sem depender de bypass de superusuario.
  if not exists (select 1 from pg_class c join pg_namespace s on s.oid=c.relnamespace
    where s.nspname='public' and c.relname='orbis_tasks' and c.relrowsecurity)
    or not exists (select 1 from pg_class c join pg_namespace s on s.oid=c.relnamespace
    where s.nspname='public' and c.relname='orbis_attachments' and c.relrowsecurity)
  then raise exception 'RLS nao habilitada nas duas tabelas'; end if;
  if not exists (select 1 from pg_policies where schemaname='public' and tablename='orbis_tasks'
    and cmd='SELECT' and roles @> array['authenticated']::name[])
  then raise exception 'Politica de leitura de tarefas ausente'; end if;
  if not exists (select 1 from pg_policies where schemaname='public' and tablename='orbis_attachments'
    and cmd='SELECT' and roles @> array['authenticated']::name[])
  then raise exception 'Politica de leitura de anexos ausente'; end if;
  if not exists (select 1 from storage.buckets where id='orbis-private-media'
    and public=false and file_size_limit=20971520)
  then raise exception 'Bucket privado ou limite de tamanho incorreto'; end if;
  if exists (select 1 from pg_policies where schemaname='storage' and tablename='objects'
    and policyname like 'orbis_media_%' and cmd='ALL')
  then raise exception 'Politica de acesso irrestrito encontrada'; end if;
  raise notice 'Estrutura basica presente; testes reais entre duas contas ainda obrigatorios';
end $$;

-- Casos manuais obrigatorios antes da liberacao:
-- 1. Conta A cria tarefa e anexo; conta B nao le, edita ou exclui esses dados.
-- 2. Conta B nao insere task/anexo usando user_id da conta A.
-- 3. Conta B nao faz upload/download/delete em prefixo A/ do bucket privado.
-- 4. Nao autenticado nao acessa tabela ou bucket.
-- 5. Dois dispositivos da conta A sincronizam sem duplicar client_id.
-- 6. Offline -> online preserva alteracoes concorrentes e historico.
-- 7. Backup/restauracao local existente nao perde dados apos login.
-- 8. Limites gratuitos atingidos: nao habilitar cobranca automaticamente.
