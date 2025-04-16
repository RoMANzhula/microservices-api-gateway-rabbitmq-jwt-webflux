CREATE TABLE expenses (
    expense_id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL,
    expense_title TEXT,
    amount NUMERIC(12, 2) NOT NULL,
    message_request TEXT,
    remaining_balance NUMERIC(12, 2) NOT NULL
);