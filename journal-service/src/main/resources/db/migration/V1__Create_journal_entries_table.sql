CREATE TABLE journal_entries (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL,
    description TEXT NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);