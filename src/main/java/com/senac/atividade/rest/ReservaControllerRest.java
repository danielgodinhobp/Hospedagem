package com.senac.atividade.rest;
import com.senac.atividade.model.Quarto;
import com.senac.atividade.model.Reserva;
import com.senac.atividade.repository.QuartoRepository;
import com.senac.atividade.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/reservas")
public class ReservaControllerRest {

    @Autowired
    private ReservaRepository reservaRepository;
    
    @Autowired
    private QuartoRepository quartoRepository;

    @GetMapping
    public List<Reserva> listarReservas() {
        return reservaRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reserva> buscarReserva(@PathVariable Integer id) {
        Optional<Reserva> reserva = reservaRepository.findById(id);
        return reserva.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @GetMapping("/quarto/{quartoId}")
    public List<Reserva> buscarReservasPorQuarto(@PathVariable Integer quartoId) {
        return reservaRepository.findByQuarto_Id(quartoId);
    }
    
    @GetMapping("/email/{email}")
    public List<Reserva> buscarReservasPorEmail(@PathVariable String email) {
        return reservaRepository.findByEmail(email);
    }
    
    @GetMapping("/status/{status}")
    public List<Reserva> buscarReservasPorStatus(@PathVariable String status) {
        return reservaRepository.findByStatus(status);
    }
    
    @GetMapping("/disponibilidade")
    public ResponseEntity<?> verificarDisponibilidade(
            @RequestParam Integer quartoId,
            @RequestParam String dataEntrada,
            @RequestParam String dataSaida) {
        
        LocalDate dataEntradaLocal = LocalDate.parse(dataEntrada);
        LocalDate dataSaidaLocal = LocalDate.parse(dataSaida);
        
        if (dataSaidaLocal.isBefore(dataEntradaLocal) || dataSaidaLocal.isEqual(dataEntradaLocal)) {
            return ResponseEntity.badRequest().body("A data de saída deve ser posterior à data de entrada.");
        }
        
        List<Reserva> conflictingReservations = reservaRepository.findConflictingReservations(
            quartoId, dataEntradaLocal, dataSaidaLocal);
        
        boolean isAvailable = conflictingReservations.isEmpty();
        
        return ResponseEntity.ok().body(isAvailable);
    }

    @PostMapping
    public ResponseEntity<?> adicionarReserva(@RequestBody Reserva reserva) {
        
        if (reserva.getDataSaida().isBefore(reserva.getDataEntrada()) || 
            reserva.getDataSaida().isEqual(reserva.getDataEntrada())) {
            return ResponseEntity.badRequest().body("A data de saída deve ser posterior à data de entrada.");
        }
        
        if (reserva.getQuarto() == null || reserva.getQuarto().getId() == 0) {
            return ResponseEntity.badRequest().body("É necessário informar um quarto válido.");
        }
        
        Optional<Quarto> quartoOpt = quartoRepository.findById(reserva.getQuarto().getId());
        if (!quartoOpt.isPresent()) {
            return ResponseEntity.badRequest().body("Quarto não encontrado.");
        }
        
        Quarto quarto = quartoOpt.get();
        
        if (reserva.getAdultos() > quarto.getCapacidadeAdultos() || 
            reserva.getCriancas() > quarto.getCapacidadeCriancas()) {
            return ResponseEntity.badRequest().body("Este quarto não comporta a quantidade de hóspedes informada.");
        }
        
        List<Reserva> conflictingReservations = reservaRepository.findConflictingReservations(
            quarto.getId(), reserva.getDataEntrada(), reserva.getDataSaida());
        
        if (!conflictingReservations.isEmpty()) {
            return ResponseEntity.badRequest().body("Este quarto não está disponível nas datas selecionadas.");
        }
        
        reserva.setQuarto(quarto);
        if (reserva.getStatus() == null || reserva.getStatus().isEmpty()) {
            reserva.setStatus("PENDENTE");
        }
        if (reserva.getDataCriacao() == null) {
            reserva.setDataCriacao(LocalDate.now());
        }
        
        Reserva reservaSalva = reservaRepository.save(reserva);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservaSalva);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarReserva(@PathVariable Integer id, @RequestBody Reserva reservaAtualizada) {
        Optional<Reserva> reservaOpt = reservaRepository.findById(id);
        if (!reservaOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        Reserva reserva = reservaOpt.get();
        
        reserva.setNome(reservaAtualizada.getNome());
        reserva.setEmail(reservaAtualizada.getEmail());
        reserva.setAdultos(reservaAtualizada.getAdultos());
        reserva.setCriancas(reservaAtualizada.getCriancas());
        reserva.setObservacoes(reservaAtualizada.getObservacoes());
        
        if (reservaAtualizada.getDataEntrada() != null && reservaAtualizada.getDataSaida() != null &&
            (reservaAtualizada.getQuarto() != null && reservaAtualizada.getQuarto().getId() != 0 && 
             reservaAtualizada.getQuarto().getId() != reserva.getQuarto().getId() ||
             !reservaAtualizada.getDataEntrada().isEqual(reserva.getDataEntrada()) ||
             !reservaAtualizada.getDataSaida().isEqual(reserva.getDataSaida()))) {
            
            if (reservaAtualizada.getDataSaida().isBefore(reservaAtualizada.getDataEntrada()) || 
                reservaAtualizada.getDataSaida().isEqual(reservaAtualizada.getDataEntrada())) {
                return ResponseEntity.badRequest().body("A data de saída deve ser posterior à data de entrada.");
            }
            
            Integer quartoId = reservaAtualizada.getQuarto() != null && reservaAtualizada.getQuarto().getId() != 0 ? 
                               reservaAtualizada.getQuarto().getId() : reserva.getQuarto().getId();
            
            Optional<Quarto> quartoOpt = quartoRepository.findById(quartoId);
            if (!quartoOpt.isPresent()) {
                return ResponseEntity.badRequest().body("Quarto não encontrado.");
            }
            
            Quarto quarto = quartoOpt.get();
            
            if (reservaAtualizada.getAdultos() > quarto.getCapacidadeAdultos() || 
                reservaAtualizada.getCriancas() > quarto.getCapacidadeCriancas()) {
                return ResponseEntity.badRequest().body("Este quarto não comporta a quantidade de hóspedes informada.");
            }
            
            List<Reserva> conflictingReservations = reservaRepository.findConflictingReservations(
                quartoId, reservaAtualizada.getDataEntrada(), reservaAtualizada.getDataSaida());
            
            conflictingReservations.removeIf(r -> r.getId() == id);
            
            if (!conflictingReservations.isEmpty()) {
                return ResponseEntity.badRequest().body("Este quarto não está disponível nas datas selecionadas.");
            }
            
            reserva.setQuarto(quarto);
            reserva.setDataEntrada(reservaAtualizada.getDataEntrada());
            reserva.setDataSaida(reservaAtualizada.getDataSaida());
        }
        
        if (reservaAtualizada.getStatus() != null && !reservaAtualizada.getStatus().isEmpty()) {
            reserva.setStatus(reservaAtualizada.getStatus());
        }
        
        Reserva reservaSalva = reservaRepository.save(reserva);
        return ResponseEntity.ok(reservaSalva);
    }
    
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> atualizarStatusReserva(@PathVariable Integer id, @RequestParam String status) {
        Optional<Reserva> reservaOpt = reservaRepository.findById(id);
        if (!reservaOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        Reserva reserva = reservaOpt.get();
        reserva.setStatus(status);
        
        Reserva reservaSalva = reservaRepository.save(reserva);
        return ResponseEntity.ok(reservaSalva);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirReserva(@PathVariable Integer id) {
        Optional<Reserva> reservaExistente = reservaRepository.findById(id);
        if (reservaExistente.isPresent()) {
            reservaRepository.delete(reservaExistente.get());
            return ResponseEntity.noContent().build();  
        } else {
            return ResponseEntity.notFound().build();  
        }
    }
}