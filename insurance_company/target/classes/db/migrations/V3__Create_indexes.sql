-- Індекси для покращення продуктивності
CREATE INDEX IF NOT EXISTS idx_insurance_type ON insurance_obligations(type);
CREATE INDEX IF NOT EXISTS idx_insurance_risk ON insurance_obligations(risk_level);
CREATE INDEX IF NOT EXISTS idx_insurance_value ON insurance_obligations(calculated_value);
CREATE INDEX IF NOT EXISTS idx_insurance_status ON insurance_obligations(status);
CREATE INDEX IF NOT EXISTS idx_insurance_dates ON insurance_obligations(start_date, end_date);

-- Індекси для таблиць конкретних типів страхування
CREATE INDEX IF NOT EXISTS idx_property_location ON property_insurance(property_location);
CREATE INDEX IF NOT EXISTS idx_property_type ON property_insurance(property_type);
CREATE INDEX IF NOT EXISTS idx_health_age ON health_insurance(age);
CREATE INDEX IF NOT EXISTS idx_health_conditions ON health_insurance(has_preexisting_conditions);

-- Індекси для зв'язків
CREATE INDEX IF NOT EXISTS idx_derivative_obligation ON derivative_obligations(derivative_id, obligation_id);
CREATE INDEX IF NOT EXISTS idx_obligation_risk ON obligation_risks(obligation_id, risk_code);