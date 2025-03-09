-- Criação do banco de dados
CREATE DATABASE IF NOT EXISTS hotelparaiso;
USE hotelparaiso;

-- Criação da tabela Quarto com o campo numero
CREATE TABLE Quarto (
    id INT AUTO_INCREMENT PRIMARY KEY,
    numero VARCHAR(10) NOT NULL UNIQUE,
    tipo VARCHAR(100) NOT NULL,
    descricao TEXT,
    valor_diaria DOUBLE NOT NULL,
    capacidade_adultos INT NOT NULL,
    capacidade_criancas INT NOT NULL,
    imagem_url VARCHAR(255)
);

-- Criação da tabela Reserva
CREATE TABLE Reserva (
    id INT AUTO_INCREMENT PRIMARY KEY,
    quarto_id INT NOT NULL,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    data_entrada DATE NOT NULL,
    data_saida DATE NOT NULL,
    adultos INT NOT NULL,
    criancas INT NOT NULL,
    observacoes TEXT,
    status VARCHAR(20) NOT NULL,
    data_criacao DATE NOT NULL,
    FOREIGN KEY (quarto_id) REFERENCES Quarto(id) ON DELETE CASCADE
);

-- Inserção de dados iniciais na tabela Quarto com números
INSERT INTO Quarto (numero, tipo, descricao, valor_diaria, capacidade_adultos, capacidade_criancas, imagem_url) VALUES
('101', 'Quarto Solteiro', 'Quarto confortável com cama de casal e vista para o jardim', 150.00, 2, 0, '/media/quarto_solteiro.jpg'),
('102', 'Quarto Solteiro', 'Quarto confortável com cama de casal e vista para o jardim', 150.00, 2, 0, '/media/quarto_solteiro.jpg'),
('103', 'Quarto Solteiro', 'Quarto confortável com cama de casal e vista para o jardim', 150.00, 2, 0, '/media/quarto_solteiro.jpg'),
('201', 'Quarto Casal', 'Quarto com cama de casal e duas camas de solteiro, além de varanda', 250.00, 2, 2, '/media/quarto_casal.jpg'),
('202', 'Quarto Casal', 'Quarto com cama de casal e duas camas de solteiro, além de varanda', 250.00, 2, 2, '/media/quarto_casal.jpg'),
('203', 'Quarto Casal', 'Quarto com cama de casal e duas camas de solteiro, além de varanda', 250.00, 2, 2, '/media/quarto_casal.jpg'),
('301', 'Suíte Master', 'Suíte ampla com cama de casal e três camas de solteiro, além de sala de estar', 350.00, 2, 3, '/media/quarto_master.jpg'),
('302', 'Suíte Master', 'Suíte ampla com cama de casal e três camas de solteiro, além de sala de estar', 350.00, 2, 3, '/media/quarto_master.jpg'),
('303', 'Suíte Master', 'Suíte ampla com cama de casal e três camas de solteiro, além de sala de estar', 350.00, 2, 3, '/media/quarto_master.jpg');

-- Índices para melhorar consultas de disponibilidade
CREATE INDEX idx_reserva_quarto_data ON Reserva (quarto_id, data_entrada, data_saida);
CREATE INDEX idx_quarto_capacidade ON Quarto (capacidade_adultos, capacidade_criancas);