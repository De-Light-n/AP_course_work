DO $$
BEGIN
    -- Перевірка та вставка ризику 'FIRE01'
    IF NOT EXISTS (SELECT 1 FROM risks WHERE code = 'FIRE01') THEN
        INSERT INTO risks (code, name, description, base_risk_factor, category)
        VALUES ('FIRE01', 'Пожежа', 'Ризик пожежі', 0.15, 'PROPERTY');
    END IF;

    -- Перевірка та вставка ризику 'THFT01'
    IF NOT EXISTS (SELECT 1 FROM risks WHERE code = 'THFT01') THEN
        INSERT INTO risks (code, name, description, base_risk_factor, category)
        VALUES ('THFT01', 'Крадіжка', 'Ризик крадіжки', 0.10, 'PROPERTY');
    END IF;

    -- Перевірка та вставка ризику 'NATD01'
    IF NOT EXISTS (SELECT 1 FROM risks WHERE code = 'NATD01') THEN
        INSERT INTO risks (code, name, description, base_risk_factor, category)
        VALUES ('NATD01', 'Стихія', 'Ризик стихійного лиха', 0.20, 'PROPERTY');
    END IF;

    -- Перевірка та вставка ризику 'HLTH01'
    IF NOT EXISTS (SELECT 1 FROM risks WHERE code = 'HLTH01') THEN
        INSERT INTO risks (code, name, description, base_risk_factor, category)
        VALUES ('HLTH01', 'Медичні витрати', 'Ризик медичних витрат', 0.20, 'HEALTH');
    END IF;

    -- Перевірка та вставка ризику 'HOSP01'
    IF NOT EXISTS (SELECT 1 FROM risks WHERE code = 'HOSP01') THEN
        INSERT INTO risks (code, name, description, base_risk_factor, category)
        VALUES ('HOSP01', 'Госпіталізація', 'Ризик госпіталізації', 0.15, 'HEALTH');
    END IF;

    -- Перевірка та вставка ризику 'DENT01'
    IF NOT EXISTS (SELECT 1 FROM risks WHERE code = 'DENT01') THEN
        INSERT INTO risks (code, name, description, base_risk_factor, category)
        VALUES ('DENT01', 'Стоматологія', 'Ризик стоматологічних витрат', 0.10, 'HEALTH');
    END IF;

    -- Перевірка та вставка ризику 'DEATH01'
    IF NOT EXISTS (SELECT 1 FROM risks WHERE code = 'DEATH01') THEN
        INSERT INTO risks (code, name, description, base_risk_factor, category)
        VALUES ('DEATH01', 'Смерть', 'Ризик смерті застрахованої особи', 0.25, 'LIFE');
    END IF;

    -- Перевірка та вставка ризику 'CRIL01'
    IF NOT EXISTS (SELECT 1 FROM risks WHERE code = 'CRIL01') THEN
        INSERT INTO risks (code, name, description, base_risk_factor, category)
        VALUES ('CRIL01', 'Критичне захворювання', 'Ризик критичного захворювання', 0.15, 'LIFE');
    END IF;

    -- Перевірка та вставка ризику 'ACCD01'
    IF NOT EXISTS (SELECT 1 FROM risks WHERE code = 'ACCD01') THEN
        INSERT INTO risks (code, name, description, base_risk_factor, category)
        VALUES ('ACCD01', 'Нещасний випадок', 'Ризик смерті від нещасного випадку', 0.10, 'LIFE');
    END IF;
END $$;