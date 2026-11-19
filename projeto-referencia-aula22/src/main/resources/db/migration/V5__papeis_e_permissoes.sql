-- V5: papéis e permissões guardados em tabelas (substituem o enum Perfil)

CREATE TABLE permissao (
    id   INT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(50) NOT NULL UNIQUE,
    PRIMARY KEY (id)
);

CREATE TABLE papel (
    id   INT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(50) NOT NULL UNIQUE,
    PRIMARY KEY (id)
);

CREATE TABLE papel_permissao (
    papel_id     INT NOT NULL,
    permissao_id INT NOT NULL,
    PRIMARY KEY (papel_id, permissao_id),
    CONSTRAINT fk_pp_papel
        FOREIGN KEY (papel_id) REFERENCES papel (id),
    CONSTRAINT fk_pp_permissao
        FOREIGN KEY (permissao_id) REFERENCES permissao (id)
);

CREATE TABLE usuario_papel (
    usuario_id INT NOT NULL,
    papel_id   INT NOT NULL,
    PRIMARY KEY (usuario_id, papel_id),
    CONSTRAINT fk_up_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT fk_up_papel
        FOREIGN KEY (papel_id) REFERENCES papel (id)
);

-- Dados iniciais
INSERT INTO permissao (nome) VALUES ('LEITURA');
INSERT INTO permissao (nome) VALUES ('ESCRITA');
INSERT INTO permissao (nome) VALUES ('USUARIOS');

INSERT INTO papel (nome) VALUES ('ADMIN');
INSERT INTO papel (nome) VALUES ('USER');

-- ADMIN tem todas as permissões
INSERT INTO papel_permissao (papel_id, permissao_id)
SELECT p.id, m.id FROM papel p CROSS JOIN permissao m WHERE p.nome = 'ADMIN';

-- USER só pode ler
INSERT INTO papel_permissao (papel_id, permissao_id)
SELECT p.id, m.id FROM papel p JOIN permissao m ON m.nome = 'LEITURA'
WHERE p.nome = 'USER';

-- Migração dos dados: cada usuário recebe o papel que antes estava na coluna perfil
INSERT INTO usuario_papel (usuario_id, papel_id)
SELECT u.id, p.id FROM usuario u
JOIN papel p ON p.nome = CAST(u.perfil AS CHAR(50));

ALTER TABLE usuario DROP COLUMN perfil;
