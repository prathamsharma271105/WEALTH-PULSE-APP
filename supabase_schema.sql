-- =========================================================================
-- WealthPulse: Supabase PostgreSQL Database Schema
-- Run this script inside the Supabase SQL Editor (Dashboard -> SQL Editor)
-- =========================================================================

-- Enable pgcrypto for password hashing if needed
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    role TEXT NOT NULL DEFAULT 'user',
    currency TEXT DEFAULT 'INR',
    phone TEXT DEFAULT '',
    income_target REAL DEFAULT 0,
    savings_target REAL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 2. Feedback Table
CREATE TABLE IF NOT EXISTS feedback (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE SET NULL,
    message TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'open',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 3. Investments Table
CREATE TABLE IF NOT EXISTS investments (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    asset_name TEXT NOT NULL,
    asset_type TEXT NOT NULL,
    amount_invested REAL NOT NULL,
    current_value REAL NOT NULL,
    date TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 4. Goals Table
CREATE TABLE IF NOT EXISTS goals (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    target_amount REAL NOT NULL,
    saved_amount REAL NOT NULL DEFAULT 0,
    deadline TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 5. Habits Table
CREATE TABLE IF NOT EXISTS habits (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    frequency TEXT NOT NULL,
    target_amount REAL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 6. Habit Logs Table
CREATE TABLE IF NOT EXISTS habit_logs (
    id SERIAL PRIMARY KEY,
    habit_id INTEGER NOT NULL REFERENCES habits(id) ON DELETE CASCADE,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    completed_on TEXT NOT NULL,
    UNIQUE (habit_id, completed_on)
);

-- 7. Expenses Table
CREATE TABLE IF NOT EXISTS expenses (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category TEXT NOT NULL,
    amount REAL NOT NULL,
    date TEXT NOT NULL,
    note TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 8. Income Table
CREATE TABLE IF NOT EXISTS income (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    source TEXT NOT NULL,
    amount REAL NOT NULL,
    date TEXT NOT NULL,
    note TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- =========================================================================
-- Disable Row Level Security (RLS) so the mobile app's Anon Key can read & write
-- This resolves PostgreSQL error 42501 ("new row violates row-level security policy")
-- =========================================================================
ALTER TABLE users DISABLE ROW LEVEL SECURITY;
ALTER TABLE feedback DISABLE ROW LEVEL SECURITY;
ALTER TABLE investments DISABLE ROW LEVEL SECURITY;
ALTER TABLE goals DISABLE ROW LEVEL SECURITY;
ALTER TABLE habits DISABLE ROW LEVEL SECURITY;
ALTER TABLE habit_logs DISABLE ROW LEVEL SECURITY;
ALTER TABLE expenses DISABLE ROW LEVEL SECURITY;
ALTER TABLE income DISABLE ROW LEVEL SECURITY;

-- If you prefer RLS enabled, run the permissive policies below instead:
-- ALTER TABLE users ENABLE ROW LEVEL SECURITY;
-- ALTER TABLE feedback ENABLE ROW LEVEL SECURITY;
-- ALTER TABLE investments ENABLE ROW LEVEL SECURITY;
-- ALTER TABLE goals ENABLE ROW LEVEL SECURITY;
-- ALTER TABLE habits ENABLE ROW LEVEL SECURITY;
-- ALTER TABLE habit_logs ENABLE ROW LEVEL SECURITY;
-- ALTER TABLE expenses ENABLE ROW LEVEL SECURITY;
-- ALTER TABLE income ENABLE ROW LEVEL SECURITY;
-- DROP POLICY IF EXISTS "Allow anon all users" ON users;
-- CREATE POLICY "Allow anon all users" ON users FOR ALL TO anon, authenticated USING (true) WITH CHECK (true);
-- DROP POLICY IF EXISTS "Allow anon all feedback" ON feedback;
-- CREATE POLICY "Allow anon all feedback" ON feedback FOR ALL TO anon, authenticated USING (true) WITH CHECK (true);
-- DROP POLICY IF EXISTS "Allow anon all investments" ON investments;
-- CREATE POLICY "Allow anon all investments" ON investments FOR ALL TO anon, authenticated USING (true) WITH CHECK (true);
-- DROP POLICY IF EXISTS "Allow anon all goals" ON goals;
-- CREATE POLICY "Allow anon all goals" ON goals FOR ALL TO anon, authenticated USING (true) WITH CHECK (true);
-- DROP POLICY IF EXISTS "Allow anon all habits" ON habits;
-- CREATE POLICY "Allow anon all habits" ON habits FOR ALL TO anon, authenticated USING (true) WITH CHECK (true);
-- DROP POLICY IF EXISTS "Allow anon all habit_logs" ON habit_logs;
-- CREATE POLICY "Allow anon all habit_logs" ON habit_logs FOR ALL TO anon, authenticated USING (true) WITH CHECK (true);
-- DROP POLICY IF EXISTS "Allow anon all expenses" ON expenses;
-- CREATE POLICY "Allow anon all expenses" ON expenses FOR ALL TO anon, authenticated USING (true) WITH CHECK (true);
-- DROP POLICY IF EXISTS "Allow anon all income" ON income;
-- CREATE POLICY "Allow anon all income" ON income FOR ALL TO anon, authenticated USING (true) WITH CHECK (true);

-- Insert Default Admin User (Password: Admin@12345)
INSERT INTO users (name, email, password_hash, role, currency)
VALUES ('Admin', 'admin@financetrack.com', crypt('Admin@12345', gen_salt('bf')), 'admin', 'INR')
ON CONFLICT (email) DO NOTHING;
