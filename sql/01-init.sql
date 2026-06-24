-- Banco usado pela opção microrna.org
CREATE DATABASE IF NOT EXISTS targets;

-- Banco usado pela opção targetScan
CREATE DATABASE IF NOT EXISTS targetscan;

-- Estrutura da tabela microrna.org
USE targets;
CREATE TABLE IF NOT EXISTS micrornaorg (
    id     INT AUTO_INCREMENT PRIMARY KEY,
    mirna  VARCHAR(100) NOT NULL,
    gene   VARCHAR(100) NOT NULL
);

-- Estrutura da tabela targetScan
USE targetscan;
CREATE TABLE IF NOT EXISTS mirna (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    mirna      VARCHAR(100) NOT NULL,
    miRFamily  VARCHAR(255) NOT NULL,
    INDEX idx_mirna_mirfamily (miRFamily)
);

CREATE TABLE IF NOT EXISTS targets (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    miRFamily  VARCHAR(255) NOT NULL,
    Gene       VARCHAR(100) NOT NULL,
    INDEX idx_targets_mirfamily (miRFamily)
);