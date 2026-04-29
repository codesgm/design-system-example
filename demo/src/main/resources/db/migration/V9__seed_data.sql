INSERT INTO companies (id, name, document) VALUES
    ('a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'Empresa Demo', '12.345.678/0001-90');

INSERT INTO financial_accounts (id, company_id, bank_code, agency, account_number, name) VALUES
    ('11111111-1111-1111-1111-111111111111', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', '237', '0001', 'Bradesco', 'Bradesco'),
    ('22222222-2222-2222-2222-222222222222', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', '341', '0001', 'Itau', 'Itaú'),
    ('33333333-3333-3333-3333-333333333333', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', '001', '0001', 'Banco do Brasil', 'Banco do Brasil'),
    ('44444444-4444-4444-4444-444444444444', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', '033', '0001', 'Santander', 'Santander'),
    ('55555555-5555-5555-5555-555555555555', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', '104', '0001', 'Caixa', 'Caixa'),
    ('66666666-6666-6666-6666-666666666666', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', '260', '0001', 'Nubank', 'Nubank'),
    ('77777777-7777-7777-7777-777777777777', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', '077', '0001', 'Inter', 'Inter'),
    ('88888888-8888-8888-8888-888888888888', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', '422', '0001', 'Safra', 'Safra');
