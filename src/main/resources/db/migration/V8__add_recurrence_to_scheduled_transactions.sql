ALTER TABLE scheduled_transactions ADD COLUMN recurrence VARCHAR(16) NULL;
ALTER TABLE scheduled_transactions ADD COLUMN recurrence_end DATETIME NULL;
ALTER TABLE scheduled_transactions ADD COLUMN occurrences_left INT NULL;