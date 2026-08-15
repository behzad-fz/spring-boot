CREATE TABLE scheduled_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    amount DECIMAL(18,2) NOT NULL,
    description VARCHAR(255),
    transaction_type VARCHAR(30) NOT NULL,
    run_at DATETIME NOT NULL,
    status VARCHAR(20) NOT NULL,
    processed_at DATETIME,
    status_explanation VARCHAR(255),
    account_id BIGINT,
    CONSTRAINT fk_scheduled_transactions_account
        FOREIGN KEY (account_id) REFERENCES accounts (id)
);
