ALTER TABLE accounts ADD COLUMN iban VARCHAR(34) NULL;
ALTER TABLE accounts ADD UNIQUE INDEX uk_accounts_iban (iban);