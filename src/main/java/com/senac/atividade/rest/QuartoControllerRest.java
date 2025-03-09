package com.senac.atividade.rest;
import com.senac.atividade.model.Quarto;
import com.senac.atividade.repository.QuartoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/quartos")
public class QuartoControllerRest {

    @Autowired
    private QuartoRepository quartoRepository;

    @GetMapping
    public List<Quarto> listarQuartos() {
        return quartoRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Quarto> buscarQuarto(@PathVariable Integer id) {
        Optional<Quarto> quarto = quartoRepository.findById(id);
        return quarto.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @GetMapping("/tipo/{tipo}")
    public List<Quarto> buscarPorTipo(@PathVariable String tipo) {
        return quartoRepository.findByTipo(tipo);
    }
    
    @GetMapping("/disponibilidade")
    public List<Quarto> buscarPorCapacidade(
            @RequestParam int adultos,
            @RequestParam int criancas) {
        return quartoRepository.findByCapacidadeAdultosGreaterThanEqualAndCapacidadeCriancasGreaterThanEqual(adultos, criancas);
    }

    @PostMapping
    public Quarto adicionarQuarto(@RequestBody Quarto quarto) {
        return quartoRepository.save(quarto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Quarto> atualizarQuarto(@PathVariable Integer id, @RequestBody Quarto quartoAtualizado) {
        return quartoRepository.findById(id).map(quarto -> {
            quarto.setTipo(quartoAtualizado.getTipo());
            quarto.setDescricao(quartoAtualizado.getDescricao());
            quarto.setValorDiaria(quartoAtualizado.getValorDiaria());
            quarto.setCapacidadeAdultos(quartoAtualizado.getCapacidadeAdultos());
            quarto.setCapacidadeCriancas(quartoAtualizado.getCapacidadeCriancas());
            quarto.setImagemUrl(quartoAtualizado.getImagemUrl());
            Quarto quartoSalvo = quartoRepository.save(quarto);
            return ResponseEntity.ok(quartoSalvo);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirQuarto(@PathVariable Integer id) {
        Optional<Quarto> quartoExistente = quartoRepository.findById(id);
        if (quartoExistente.isPresent()) {
            quartoRepository.delete(quartoExistente.get());
            return ResponseEntity.noContent().build();  
        } else {
            return ResponseEntity.notFound().build();  
        }
    }
}