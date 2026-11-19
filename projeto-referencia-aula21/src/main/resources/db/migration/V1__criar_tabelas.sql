-- V1: estrutura inicial do banco (compatível com H2 e MySQL)

CREATE TABLE endereco (
    id         INT NOT NULL AUTO_INCREMENT,
    logradouro VARCHAR(255),
    numero     VARCHAR(255),
    cep        VARCHAR(255),
    cidade     VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE TABLE estudante (
    id              INT NOT NULL AUTO_INCREMENT,
    nome            VARCHAR(255),
    sobre_nome      VARCHAR(255),
    email           VARCHAR(255),
    data_nascimento DATE,
    endereco_id     INT UNIQUE,
    PRIMARY KEY (id),
    CONSTRAINT fk_estudante_endereco
        FOREIGN KEY (endereco_id) REFERENCES endereco (id)
);

CREATE TABLE professor (
    matricula      INT NOT NULL AUTO_INCREMENT,
    nome           VARCHAR(255),
    especializacao VARCHAR(255),
    telefone       VARCHAR(255),
    PRIMARY KEY (matricula)
);

CREATE TABLE turma (
    id                  INT NOT NULL AUTO_INCREMENT,
    numero              VARCHAR(8) NOT NULL UNIQUE,
    titulo              VARCHAR(255),
    turno               ENUM ('MANHA', 'NOITE', 'TARDE'),
    professor_matricula INT,
    PRIMARY KEY (id),
    CONSTRAINT fk_turma_professor
        FOREIGN KEY (professor_matricula) REFERENCES professor (matricula)
);

CREATE TABLE turma_estudante (
    turma_id     INT NOT NULL,
    estudante_id INT NOT NULL,
    PRIMARY KEY (estudante_id, turma_id),
    CONSTRAINT fk_te_turma
        FOREIGN KEY (turma_id) REFERENCES turma (id),
    CONSTRAINT fk_te_estudante
        FOREIGN KEY (estudante_id) REFERENCES estudante (id)
);

CREATE TABLE usuario (
    id       INT NOT NULL AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    perfil   ENUM ('ADMIN', 'USER') NOT NULL,
    PRIMARY KEY (id)
);
