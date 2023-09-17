ALTER TABLE accounts
MODIFY currency ENUM(
    'EUR',
    'USD',
    'GBP'
) NOT NULL;
