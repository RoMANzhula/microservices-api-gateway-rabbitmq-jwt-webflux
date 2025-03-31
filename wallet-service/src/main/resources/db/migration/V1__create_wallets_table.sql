CREATE TABLE wallets (
    user_id UUID PRIMARY KEY,
    balance NUMERIC(12, 2) NOT NULL
);

COMMENT ON TABLE wallets IS 'Table for storing user wallets';
COMMENT ON COLUMN wallets.user_id IS 'Unique user identity';
COMMENT ON COLUMN wallets.balance IS 'User balance';