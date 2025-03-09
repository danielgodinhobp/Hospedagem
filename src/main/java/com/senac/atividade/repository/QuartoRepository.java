package com.senac.atividade.repository;
import com.senac.atividade.model.Quarto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
public interface QuartoRepository extends JpaRepository<Quarto, Integer> {
    List<Quarto> findByTipo(String tipo);
    List<Quarto> findByCapacidadeAdultosGreaterThanEqualAndCapacidadeCriancasGreaterThanEqual(int adultos, int criancas);
}