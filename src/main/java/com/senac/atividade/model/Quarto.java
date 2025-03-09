package com.senac.atividade.model;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;


@Data
@Entity
@Table(name="Quarto")
public class Quarto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String numero; 
    private String tipo;
    private String descricao;
    private double valorDiaria;
    private int capacidadeAdultos;
    private int capacidadeCriancas;
    private String imagemUrl;
    
    public Quarto() {
    }
    
    public Quarto(int id, String numero, String tipo, String descricao, double valorDiaria, int capacidadeAdultos, int capacidadeCriancas, String imagemUrl) {
        this.id = id;
        this.numero = numero;
        this.tipo = tipo;
        this.descricao = descricao;
        this.valorDiaria = valorDiaria;
        this.capacidadeAdultos = capacidadeAdultos;
        this.capacidadeCriancas = capacidadeCriancas;
        this.imagemUrl = imagemUrl;
    }
}