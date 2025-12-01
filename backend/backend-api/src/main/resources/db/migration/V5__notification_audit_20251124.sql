ALTER TABLE public.notification_audit ADD COLUMN IF NOT EXISTS recipient_key text NULL;
