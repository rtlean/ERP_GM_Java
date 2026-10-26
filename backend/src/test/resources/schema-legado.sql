-- Recorte do schema do sistema atual (drizzle/schema.ts), só com as colunas usadas pelo novo backend.
-- Existe apenas para os testes: o banco real nunca é criado ou alterado por este projeto.
-- Os enums do Postgres viram VARCHAR no H2.

CREATE TABLE IF NOT EXISTS companies (
    id SERIAL PRIMARY KEY,
    name VARCHAR(180) NOT NULL,
    owner_user_id INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    open_id VARCHAR(255) NOT NULL UNIQUE,
    name TEXT,
    email VARCHAR(320),
    role VARCHAR(20) DEFAULT 'user' NOT NULL,
    status VARCHAR(20) DEFAULT 'active' NOT NULL,
    password_hash VARCHAR(255),
    temporary_access_expires_at TIMESTAMP WITH TIME ZONE,
    must_change_password BOOLEAN DEFAULT FALSE NOT NULL,
    sessions_invalidated_at TIMESTAMP WITH TIME ZONE,
    active_company_id INTEGER,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    last_signed_in TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS company_users (
    id SERIAL PRIMARY KEY,
    company_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    role VARCHAR(20) DEFAULT 'user' NOT NULL,
    status VARCHAR(20) DEFAULT 'active' NOT NULL
);

CREATE TABLE IF NOT EXISTS owners (
    id SERIAL PRIMARY KEY,
    full_name VARCHAR(180) NOT NULL,
    document VARCHAR(30),
    email VARCHAR(320),
    phone VARCHAR(30),
    preferred_repass_method VARCHAR(30),
    company_id INTEGER
);

CREATE TABLE IF NOT EXISTS properties (
    id SERIAL PRIMARY KEY,
    code VARCHAR(40) NOT NULL,
    type VARCHAR(80) NOT NULL,
    address VARCHAR(255) NOT NULL,
    city VARCHAR(120) NOT NULL,
    state VARCHAR(2) NOT NULL,
    zip_code VARCHAR(12),
    rent_cents INTEGER DEFAULT 0 NOT NULL,
    occupancy_status VARCHAR(20) DEFAULT 'vacant' NOT NULL,
    owner_id INTEGER,
    company_id INTEGER
);

CREATE TABLE IF NOT EXISTS tenants (
    id SERIAL PRIMARY KEY,
    full_name VARCHAR(180) NOT NULL,
    cpf VARCHAR(14),
    email VARCHAR(320),
    phone VARCHAR(30),
    owner_id INTEGER,
    rent_cents INTEGER DEFAULT 0 NOT NULL,
    property_type VARCHAR(80),
    company_id INTEGER
);

CREATE TABLE IF NOT EXISTS contracts (
    id SERIAL PRIMARY KEY,
    property_id INTEGER NOT NULL,
    tenant_id INTEGER NOT NULL,
    start_date TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(20) DEFAULT 'active' NOT NULL,
    company_id INTEGER
);

CREATE TABLE IF NOT EXISTS login_attempts (
    id SERIAL PRIMARY KEY,
    scope VARCHAR(10) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    attempted_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS audit_events (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    action VARCHAR(80) NOT NULL,
    entity VARCHAR(80) NOT NULL,
    entity_id INTEGER,
    metadata TEXT,
    company_id INTEGER,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);
