package com.senac.atividade.model;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import java.time.LocalDate;


@Data
@Entity
@Table(name="Reserva")
public class Reserva {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @ManyToOne
    @JoinColumn(name = "quarto_id", nullable = false)
    private Quarto quarto;
    
    private String nome;
    private String email;
    private LocalDate dataEntrada;
    private LocalDate dataSaida;
    private int adultos;
    private int criancas;
    private String observacoes;
    private String status;  
    private LocalDate dataCriacao;
    
    public Reserva() {
        this.dataCriacao = LocalDate.now();
        this.status = "PENDENTE";
    }
    
    public Reserva(int id, Quarto quarto, String nome, String email, LocalDate dataEntrada, 
                  LocalDate dataSaida, int adultos, int criancas, String observacoes) {
        this.id = id;
        this.quarto = quarto;
        this.nome = nome;
        this.email = email;
        this.dataEntrada = dataEntrada;
        this.dataSaida = dataSaida;
        this.adultos = adultos;
        this.criancas = criancas;
        this.observacoes = observacoes;
        this.dataCriacao = LocalDate.now();
        this.status = "PENDENTE";
    }
}