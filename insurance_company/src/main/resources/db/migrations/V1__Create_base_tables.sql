-- Основна таблиця для страхових зобов'язань
CREATE TABLE IF NOT EXISTS insurance_obligations (
    id SERIAL PRIMARY KEY,
    policy_number VARCHAR(40) NOT NULL UNIQUE,
    type VARCHAR(40) NOT NULL CHECK (type IN ('PROPERTY', 'HEALTH', 'LIFE', 'AUTO', 'TRAVEL', 'ACCIDENT')),
    risk_level DECIMAL(5,2) NOT NULL CHECK (risk_level BETWEEN 0 AND 1),
    amount DECIMAL(20,2) NOT NULL,
    duration_months INTEGER NOT NULL,
    calculated_value DECIMAL(20,2) NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    status VARCHAR(40) NOT NULL CHECK (status IN ('DRAFT', 'ACTIVE', 'EXPIRED', 'CANCELLED', 'PENDING', 'CLAIMED')),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Таблиця ризиків
CREATE TABLE IF NOT EXISTS risks (
    code VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    base_risk_factor DECIMAL(20,2) NOT NULL,
    category VARCHAR(40) NOT NULL CHECK (category IN ('PROPERTY', 'HEALTH', 'LIFE', 'LIABILITY', 'FINANCIAL'))
);

-- Таблиця зв'язку між зобов'язаннями та ризиками
CREATE TABLE IF NOT EXISTS obligation_risks (
    obligation_id INTEGER REFERENCES insurance_obligations(id) ON DELETE CASCADE,
    risk_code VARCHAR(20) REFERENCES risks(code) ON DELETE CASCADE,
    PRIMARY KEY (obligation_id, risk_code)
);

-- Таблиця для страхування майна
CREATE TABLE IF NOT EXISTS property_insurance (
    obligation_id INTEGER PRIMARY KEY REFERENCES insurance_obligations(id) ON DELETE CASCADE,
    property_location VARCHAR(255) NOT NULL,
    property_value DECIMAL(20,2) NOT NULL,
    is_high_risk_area BOOLEAN NOT NULL DEFAULT FALSE,
    property_type VARCHAR(40) NOT NULL CHECK (property_type IN ('APARTMENT', 'HOUSE', 'COMMERCIAL')),
    includes_natural_disasters BOOLEAN NOT NULL DEFAULT FALSE
);

-- Таблиця для медичного страхування
CREATE TABLE IF NOT EXISTS health_insurance (
    obligation_id INTEGER PRIMARY KEY REFERENCES insurance_obligations(id) ON DELETE CASCADE,
    age INTEGER NOT NULL CHECK (age > 0),
    has_preexisting_conditions BOOLEAN NOT NULL DEFAULT FALSE,
    coverage_limit INTEGER NOT NULL,
    includes_hospitalization BOOLEAN NOT NULL DEFAULT FALSE,
    includes_dental_care BOOLEAN NOT NULL DEFAULT FALSE
);

-- Таблиця для страхування життя
CREATE TABLE IF NOT EXISTS life_insurance (
    obligation_id INTEGER PRIMARY KEY REFERENCES insurance_obligations(id) ON DELETE CASCADE,
    beneficiary VARCHAR(255) NOT NULL,
    includes_critical_illness BOOLEAN NOT NULL DEFAULT FALSE,
    includes_accidental_death BOOLEAN NOT NULL DEFAULT FALSE
);