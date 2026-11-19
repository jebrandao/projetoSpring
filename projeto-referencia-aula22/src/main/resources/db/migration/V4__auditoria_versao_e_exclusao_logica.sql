-- V4: controle de versão (bloqueio otimista), exclusão lógica e auditoria

-- Estudante: versão e exclusão lógica
ALTER TABLE estudante ADD COLUMN versao BIGINT NOT NULL DEFAULT 0;
ALTER TABLE estudante ADD COLUMN excluido BOOLEAN NOT NULL DEFAULT FALSE;

-- Auditoria: quem e quando criou/alterou cada registro
ALTER TABLE estudante ADD COLUMN criado_em DATETIME(6);
ALTER TABLE estudante ADD COLUMN atualizado_em DATETIME(6);
ALTER TABLE estudante ADD COLUMN criado_por VARCHAR(255);
ALTER TABLE estudante ADD COLUMN atualizado_por VARCHAR(255);

ALTER TABLE professor ADD COLUMN criado_em DATETIME(6);
ALTER TABLE professor ADD COLUMN atualizado_em DATETIME(6);
ALTER TABLE professor ADD COLUMN criado_por VARCHAR(255);
ALTER TABLE professor ADD COLUMN atualizado_por VARCHAR(255);

ALTER TABLE turma ADD COLUMN criado_em DATETIME(6);
ALTER TABLE turma ADD COLUMN atualizado_em DATETIME(6);
ALTER TABLE turma ADD COLUMN criado_por VARCHAR(255);
ALTER TABLE turma ADD COLUMN atualizado_por VARCHAR(255);
