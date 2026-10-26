-- Dados FICTÍCIOS para desenvolvimento local (perfil "local"). Nenhum dado real.
-- Senha de todos os usuários: Senha@123 (hash no formato do sistema atual).
DELETE FROM audit_events; DELETE FROM login_attempts; DELETE FROM contracts; DELETE FROM tenants;
DELETE FROM properties; DELETE FROM owners; DELETE FROM company_users; DELETE FROM users; DELETE FROM companies;

INSERT INTO companies (id, name, owner_user_id) VALUES (1, 'GM Imobiliária (fictícia)', 1);
INSERT INTO users (id, open_id, name, email, role, status, password_hash) VALUES
  (1, 'local-1', 'Administrador Fictício', 'admin@ficticio.local', 'admin', 'active',
   '0123456789abcdef0123456789abcdef:72fb9f406c870be54290efe23dfdab096d60a4181d2440ee3e0bbd80e8e72178b2dde38c6484a23be70f3bb3b092c3d5592a9fdfd7c5437f570ca5c21afe866b'),
  (2, 'local-2', 'Operacional Fictícia', 'operacional@ficticio.local', 'user', 'active',
   '0123456789abcdef0123456789abcdef:72fb9f406c870be54290efe23dfdab096d60a4181d2440ee3e0bbd80e8e72178b2dde38c6484a23be70f3bb3b092c3d5592a9fdfd7c5437f570ca5c21afe866b');
INSERT INTO company_users (company_id, user_id, role, status) VALUES (1, 1, 'admin', 'active'), (1, 2, 'user', 'active');

INSERT INTO owners (id, full_name, document, email, phone, preferred_repass_method, company_id) VALUES
  (1, 'Ana Exemplo Souza', '111.444.777-35', 'ana@ficticio.local', '(34) 90000-0001', 'PIX', 1),
  (2, 'Bruno Exemplo Lima', '52998224725', 'bruno@ficticio.local', '(34) 90000-0002', 'TRANSFERÊNCIA BANCÁRIA', 1),
  (3, 'Empresa Exemplo Ltda', '11222333000181', NULL, '(34) 3000-0003', 'PIX', 1);
INSERT INTO properties (id, code, type, address, city, state, zip_code, rent_cents, occupancy_status, owner_id, company_id) VALUES
  (1, 'GM-001', 'Casa', 'Rua das Flores, 100', 'Uberlândia', 'MG', '38400-000', 180000, 'occupied', 1, 1),
  (2, 'GM-002', 'Apartamento', 'Av. Brasil, 2500, ap. 301', 'Uberlândia', 'MG', '38400-100', 125050, 'occupied', 2, 1),
  (3, 'GM-003', 'Sala comercial', 'Rua do Comércio, 45, sala 2', 'Uberaba', 'MG', NULL, 90000, 'vacant', 3, 1),
  (4, 'GM-004', 'Casa', 'Rua Projetada, 12', 'Araguari', 'MG', NULL, 110000, 'maintenance', 1, 1);
INSERT INTO tenants (id, full_name, cpf, email, phone, owner_id, rent_cents, property_type, company_id) VALUES
  (1, 'Carla Exemplo Rocha', '39053344705', 'carla@ficticio.local', '(34) 98888-0001', 1, 180000, 'Casa', 1),
  (2, 'Diego Exemplo Alves', NULL, NULL, '(34) 98888-0002', 2, 125050, 'Apartamento', 1);
INSERT INTO contracts (property_id, tenant_id, start_date, status, company_id) VALUES
  (1, 1, TIMESTAMP WITH TIME ZONE '2026-01-10 00:00:00+00', 'active', 1),
  (2, 2, TIMESTAMP WITH TIME ZONE '2025-08-01 00:00:00+00', 'active', 1);
