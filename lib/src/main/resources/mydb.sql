CREATE TABLE IF NOT EXISTS notify_settings(ticker_symbol VARCHAR(30) UNSIGNED NOT NULL PRIMARY KEY, javascript JSON, notify_period INT DEFAULT 0);
