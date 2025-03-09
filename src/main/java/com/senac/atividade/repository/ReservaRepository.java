package com.senac.atividade.repository;
import com.senac.atividade.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;


@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Integer> {
    List<Reserva> findByQuarto_Id(Integer quartoId);
    List<Reserva> findByEmail(String email);
    List<Reserva> findByStatus(String status);
    
    @Query("SELECT r FROM Reserva r WHERE r.quarto.id = ?1 AND " +
           "((r.dataEntrada BETWEEN ?2 AND ?3) OR " +
           "(r.dataSaida BETWEEN ?2 AND ?3) OR " +
           "(?2 BETWEEN r.dataEntrada AND r.dataSaida) OR " +
           "(?3 BETWEEN r.dataEntrada AND r.dataSaida)) AND " +
           "r.status != 'CANCELADA'")
    List<Reserva> findConflictingReservations(Integer quartoId, LocalDate dataEntrada, LocalDate dataSaida);
}