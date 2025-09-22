-- DROP TYPE public."channel";

CREATE TYPE public."channel" AS ENUM (
	'EMAIL',
	'TELEGRAM');

-- DROP TYPE public."onboarding_state";

CREATE TYPE public."onboarding_state" AS ENUM (
	'REQUESTED',
	'APPROVED',
	'PREPARATION',
	'ENABLED',
	'FAILED',
	'EXPIRED',
	'REJECTED');

-- DROP TYPE public."user_role";

CREATE TYPE public."user_role" AS ENUM (
	'ADMIN',
	'EDITOR',
	'VIEWER');

-- public.app_user definition

-- Drop table

-- DROP TABLE public.app_user;

CREATE TABLE public.app_user ( user_id bigserial NOT NULL, username text NOT NULL, email text NOT NULL, password_hash text NOT NULL, full_name text NULL, status text DEFAULT 'PENDING'::text NOT NULL, last_login_at timestamptz NULL, last_failed_login_at timestamptz NULL, failed_login_count int4 DEFAULT 0 NOT NULL, correlation_id text NULL, created_at timestamptz DEFAULT now() NOT NULL, updated_at timestamptz DEFAULT now() NOT NULL, is_curator bool DEFAULT false NULL, CONSTRAINT app_user_email_key UNIQUE (email), CONSTRAINT app_user_pkey PRIMARY KEY (user_id), CONSTRAINT app_user_status_check CHECK ((status = ANY (ARRAY['PENDING'::text, 'ENABLED'::text, 'DISABLED'::text, 'LOCKED'::text]))), CONSTRAINT app_user_username_key UNIQUE (username));


CREATE TABLE public.channel_config ( "channel" public."channel" NOT NULL, transport_cfg_json jsonb NOT NULL, enabled bool DEFAULT true NOT NULL, rate_limit_cfg jsonb NULL, CONSTRAINT channel_config_pkey PRIMARY KEY (channel));

-- public.contact_information definition

-- Drop table

-- DROP TABLE public.contact_information;

CREATE TABLE public.contact_information (
	id bigserial NOT NULL,
	user_id int8 NOT NULL,
	"channel" public."channel" NOT NULL,
	value jsonb NOT NULL,
	enabled bool DEFAULT true NOT NULL,
	priority int4 DEFAULT 100 NOT NULL,
	CONSTRAINT contact_information_pkey PRIMARY KEY (id),
	CONSTRAINT contact_information_user_id_channel_priority_key UNIQUE (user_id, channel, priority)
);


-- public.contact_information foreign keys

ALTER TABLE public.contact_information ADD CONSTRAINT contact_information_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.app_user(user_id) ON DELETE CASCADE;

-- public.tenant definition

-- Drop table

-- DROP TABLE public.tenant;

CREATE TABLE public.tenant (
	tenant_id text NOT NULL,
	producer_name text NOT NULL,
	base_dir text NOT NULL,
	ds_provider text DEFAULT 'SQLITE'::text NOT NULL,
	ds_config jsonb DEFAULT '{}'::jsonb NOT NULL,
	state public."onboarding_state" DEFAULT 'PREPARATION'::onboarding_state NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	CONSTRAINT tenant_pkey PRIMARY KEY (tenant_id)
);
CREATE INDEX tenant_state_idx ON public.tenant USING btree (state);

-- public.user_tenant definition

-- Drop table

-- DROP TABLE public.user_tenant;

CREATE TABLE public.user_tenant (
	user_id int8 NOT NULL,
	tenant_id text NOT NULL,
	"role" public."user_role" NOT NULL,
	joined_at timestamptz DEFAULT now() NOT NULL,
	CONSTRAINT user_tenant_pkey PRIMARY KEY (user_id, tenant_id)
);


-- public.user_tenant foreign keys

ALTER TABLE public.user_tenant ADD CONSTRAINT user_tenant_tenant_id_fkey FOREIGN KEY (tenant_id) REFERENCES public.tenant(tenant_id) ON DELETE CASCADE;
ALTER TABLE public.user_tenant ADD CONSTRAINT user_tenant_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.app_user(user_id) ON DELETE CASCADE;

-- public.idempotency_store definition

-- Drop table

-- DROP TABLE public.idempotency_store;

CREATE TABLE public.idempotency_store (
	idem_key text NOT NULL,
	valid_period tstzrange NOT NULL
);
CREATE INDEX idempotency_store_idem_key_idx ON public.idempotency_store USING btree (idem_key);

-- public.notification_audit definition


-- DROP TABLE public.notification_template;

CREATE TABLE public.notification_template (
	id bigserial NOT NULL,
	"name" text NOT NULL,
	"version" int4 NOT NULL,
	body_md text NOT NULL,
	"scope" text DEFAULT 'GLOBAL'::text NOT NULL,
	tenant_id text NULL,
	CONSTRAINT notification_template_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uq_template_name_version_scope_tenant ON public.notification_template USING btree (name, version, scope, COALESCE(tenant_id, 'GLOBAL'::text));


-- public.notification_template foreign keys

ALTER TABLE public.notification_template ADD CONSTRAINT notification_template_tenant_id_fkey FOREIGN KEY (tenant_id) REFERENCES public.tenant(tenant_id);

-- Drop table

-- DROP TABLE public.notification_audit;

CREATE TABLE public.notification_audit (
	id text NOT NULL,
	correlation_id text NULL,
	tenant_id text NULL,
	user_id int8 NULL,
	template_id int8 NULL,
	"channel" public."channel" NOT NULL,
	status text NOT NULL,
	attempts int4 DEFAULT 0 NOT NULL,
	max_attempts int4 DEFAULT 0 NOT NULL,
	error_code text NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	sent_at timestamptz NULL,
	error_at timestamptz NULL,
	event_type text NULL,
	ttl interval NULL,
	provider_meta jsonb NULL,
	has_pii bool DEFAULT false NOT NULL,
	body_audit_stored text NULL,
	body_audit text NULL,
	ticket_id text NULL,
	CONSTRAINT notification_audit_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_notification_audit_corr ON public.notification_audit USING btree (correlation_id);
CREATE INDEX idx_notification_audit_event ON public.notification_audit USING btree (event_type);


-- public.notification_audit foreign keys

ALTER TABLE public.notification_audit ADD CONSTRAINT notification_audit_template_id_fkey FOREIGN KEY (template_id) REFERENCES public.notification_template(id);
ALTER TABLE public.notification_audit ADD CONSTRAINT notification_audit_tenant_id_fkey FOREIGN KEY (tenant_id) REFERENCES public.tenant(tenant_id);
ALTER TABLE public.notification_audit ADD CONSTRAINT notification_audit_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.app_user(user_id);
-- public.notification_template definition

-- Drop table


-- public.onboarding_audit definition


-- Drop table

-- DROP TABLE public.onboarding_request;

CREATE TABLE public.onboarding_request (
	id bigserial NOT NULL,
	requester_user_id int8 NOT NULL,
	producer_name text NOT NULL,
	email text NOT NULL,
	website text NULL,
	vat_or_fiscal_code text NULL,
	state public."onboarding_state" DEFAULT 'REQUESTED'::onboarding_state NOT NULL,
	correlation_id text NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	CONSTRAINT onboarding_request_pkey PRIMARY KEY (id)
);
CREATE INDEX onboarding_request_state_idx ON public.onboarding_request USING btree (state);


-- Drop table

-- DROP TABLE public.onboarding_audit;

CREATE TABLE public.onboarding_audit (
	id bigserial NOT NULL,
	onboarding_id int8 NOT NULL,
	"action" text NOT NULL,
	actor_user_id int8 NULL,
	ts timestamptz DEFAULT now() NOT NULL,
	details_json jsonb NULL,
	CONSTRAINT onboarding_audit_pkey PRIMARY KEY (id)
);


-- public.onboarding_audit foreign keys

ALTER TABLE public.onboarding_audit ADD CONSTRAINT onboarding_audit_onboarding_id_fkey FOREIGN KEY (onboarding_id) REFERENCES public.onboarding_request(id) ON DELETE CASCADE;

-- public.onboarding_request definition


-- public.redmine_settings definition

-- Drop table

-- DROP TABLE public.redmine_settings;

CREATE TABLE public.redmine_settings (
	id bigserial NOT NULL,
	base_url text NOT NULL,
	api_key text NOT NULL,
	project_id int4 NOT NULL,
	CONSTRAINT redmine_settings_pkey PRIMARY KEY (id)
);

-- DROP FUNCTION public.curators_address(channel);

CREATE OR REPLACE FUNCTION public.curators_address(ch channel DEFAULT 'EMAIL'::channel)
 RETURNS TABLE(user_id bigint, channel channel, address text)
 LANGUAGE sql
AS $function$
SELECT DISTINCT ON (u.user_id, ci.channel)
    u.user_id,
--    u.username,
--    u.full_name,
    ci.channel,
    COALESCE(ci.value->>'address', ci.value->>'email') as address
--    ci.priority
FROM app_user u
INNER JOIN contact_information ci ON u.user_id = ci.user_id
WHERE u.is_curator = true
  AND ci.enabled = true
  and ci.channel = ch
ORDER BY u.user_id, ci.channel, ci.priority ASC;
$function$
;


-- DROP FUNCTION public.idem_exists(text, timestamptz);

CREATE OR REPLACE FUNCTION public.idem_exists(idem_key text, created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP)
 RETURNS boolean
 LANGUAGE sql
AS $function$
SELECT EXISTS (
    SELECT 1 FROM public.idempotency_store
    WHERE idem_key = idem_exists.idem_key
      AND valid_period @> CURRENT_TIMESTAMP
)
$function$
;

-- DROP FUNCTION public.idem_put(text, timestamptz, int8);

CREATE OR REPLACE FUNCTION public.idem_put(idem_key text, created_at timestamp with time zone, ttl bigint)
 RETURNS boolean
 LANGUAGE sql
AS $function$
insert into public.idempotency_store (idem_key, valid_period)
select idem_put.idem_key, tstzrange(created_at, created_at + (ttl || ' second')::interval)
WHERE NOT EXISTS (
    SELECT 1 FROM public.idempotency_store
    WHERE idem_key = idem_put.idem_key
      AND valid_period @> CURRENT_TIMESTAMP
)
RETURNING TRUE AS inserted;
$function$
;

-- DROP FUNCTION public.idem_put(text, int8);

CREATE OR REPLACE FUNCTION public.idem_put(idem_key text, ttl bigint)
 RETURNS boolean
 LANGUAGE sql
AS $function$
  select public.idem_put(idem_key,CURRENT_TIMESTAMP,ttl);
$function$
;




-- DROP FUNCTION public.onboarding_curators(int8, channel);

CREATE OR REPLACE FUNCTION public.onboarding_curators(request_id bigint, ch channel DEFAULT 'EMAIL'::channel)
 RETURNS TABLE(user_id bigint, username text, channel channel, address text)
 LANGUAGE sql
AS $function$

select u.user_id, u.username, a.channel, a.address
from onboarding_audit oa
join app_user u on oa.actor_user_id = u.user_id
join curators_address() a on a.user_id = u.user_id
where onboarding_id =  request_id
  and u.is_curator;
$function$
;

-- DROP FUNCTION public.onboarding_get_request_by_id(int4);

CREATE OR REPLACE FUNCTION public.onboarding_get_request_by_id(p_id integer)
 RETURNS TABLE(id bigint, requester_user_id bigint, producer_name text, email text, website text, vat_or_fiscal_code text, state text, correlation_id text, created_at timestamp with time zone)
 LANGUAGE sql
AS $function$

    SELECT
        o.id, requester_user_id,  producer_name, o.email, website,vat_or_fiscal_code, state::text, correlation_id, created_at
    FROM onboarding_request o
    WHERE o.id = p_id;

$function$
;
















