INSERT INTO public.app_user (username,email,password_hash,full_name,status,last_login_at,last_failed_login_at,failed_login_count,correlation_id,created_at,updated_at,is_curator) VALUES
('fake','pippo@gmail.com','123','buco nel numeratore :)','ENABLED',NULL,NULL,0,NULL,'2025-09-20 18:20:00.851187+02','2025-09-20 18:20:00.851187+02',true),
	 ('curatore','admin@gmail.com','123','curatore di test','ENABLED',NULL,NULL,0,NULL,'2025-09-20 18:20:00.851187+02','2025-09-20 18:20:00.851187+02',true),
	 ('utente di test','user@gmail.com','123','utente di test','ENABLED',NULL,NULL,0,NULL,'2025-09-20 18:20:27.466887+02','2025-09-20 18:20:27.466887+02',false);
