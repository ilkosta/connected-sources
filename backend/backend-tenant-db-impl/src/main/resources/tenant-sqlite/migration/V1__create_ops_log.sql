CREATE TABLE IF NOT EXISTS ops_log(
  id TEXT PRIMARY KEY,
  ts TIMESTAMP NOT NULL,
  tenant_id TEXT,
  user_id BIGINT,
  correlation_id TEXT,
  category TEXT NOT NULL,
  level TEXT NOT NULL,
  message TEXT NOT NULL,
  data_json TEXT
);
CREATE INDEX IF NOT EXISTS idx_ops_log_ts ON ops_log(ts);
