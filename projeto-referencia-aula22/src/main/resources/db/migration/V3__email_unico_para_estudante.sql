-- V3: dois estudantes não podem ter o mesmo e-mail
ALTER TABLE estudante ADD CONSTRAINT uk_estudante_email UNIQUE (email);
