-- Таблиця деривативів
CREATE TABLE IF NOT EXISTS derivatives (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    total_value DECIMAL(20,2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Таблиця для зв'язку деривативів і зобов'язань (many-to-many)
CREATE TABLE IF NOT EXISTS derivative_obligations (
    derivative_id INTEGER REFERENCES derivatives(id) ON DELETE CASCADE,
    obligation_id INTEGER REFERENCES insurance_obligations(id) ON DELETE CASCADE,
    PRIMARY KEY (derivative_id, obligation_id)
);