PRAGMA journal_mode=WAL;

CREATE TABLE IF NOT EXISTS role (
                                    id TEXT PRIMARY KEY,
                                    name TEXT NOT NULL UNIQUE
);
INSERT OR IGNORE INTO role(id, name) VALUES
 ('admin','Admin'),
 ('editor','Editor'),
--  ('viewer','Viewer');

CREATE TABLE IF NOT EXISTS category (
                                        id TEXT PRIMARY KEY,
                                        name TEXT NOT NULL UNIQUE
);
INSERT OR IGNORE INTO category(id, name) VALUES
 ('default','Default');

CREATE TABLE IF NOT EXISTS template (
                                        id TEXT PRIMARY KEY,
                                        name TEXT NOT NULL UNIQUE,
                                        body_md TEXT NOT NULL
);
INSERT OR IGNORE INTO template(id, name, body_md) VALUES
 ('Benvenuto','Benvenuto','Benvenuto nell''area dedicata  a ${producerName}.');


CREATE TABLE IF NOT EXISTS ops_log (
    id TEXT PRIMARY KEY,
    ts TEXT NOT NULL,
    level TEXT NOT NULL,
    category TEXT NOT NULL,
    message TEXT NOT NULL,
    data_json TEXT,
    correlation_id TEXT,
    user_id INTEGER
);
CREATE INDEX IF NOT EXISTS idx_ops_log_ts ON ops_log(ts);
CREATE TABLE IF NOT EXISTS content_log (
    id TEXT PRIMARY KEY,
    ts TEXT NOT NULL,
    content_id TEXT NOT NULL,
    action TEXT NOT NULL,
    metadata_json TEXT,
    correlation_id TEXT,
    user_id INTEGER
);