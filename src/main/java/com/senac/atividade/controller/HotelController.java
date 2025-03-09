package com.senac.atividade.controller;
import com.senac.atividade.model.Quarto;
import com.senac.atividade.model.Reserva;
import com.senac.atividade.repository.QuartoRepository;
import com.senac.atividade.repository.ReservaRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Controller
public class HotelController {

    @Autowired
    private QuartoRepository quartoRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("quartos", quartoRepository.findAll());
        model.addAttribute("reserva", new Reserva());
        return "index";
    }
    
    @PostMapping("/reservar")
    public String processarReserva(@Valid @ModelAttribute Reserva reserva, 
                                  @RequestParam("quartoId") Integer quartoId,
                                  BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("quartos", quartoRepository.findAll());
            model.addAttribute("erro", "Por favor preencha todos os campos corretamente.");
            return "index";
        }
        
        if (reserva.getDataSaida().isBefore(reserva.getDataEntrada()) || 
            reserva.getDataSaida().isEqual(reserva.getDataEntrada())) {
            model.addAttribute("quartos", quartoRepository.findAll());
            model.addAttribute("erro", "A data de saída deve ser posterior à data de entrada.");
            return "index";
        }
        
        Optional<Quarto> quartoOpt = quartoRepository.findById(quartoId);
        if (!quartoOpt.isPresent()) {
            model.addAttribute("quartos", quartoRepository.findAll());
            model.addAttribute("erro", "Quarto não encontrado.");
            return "index";
        }
        
        Quarto quarto = quartoOpt.get();
        
        if (reserva.getAdultos() > quarto.getCapacidadeAdultos() || 
            reserva.getCriancas() > quarto.getCapacidadeCriancas()) {
            model.addAttribute("quartos", quartoRepository.findAll());
            model.addAttribute("erro", "Este quarto não comporta a quantidade de hóspedes informada.");
            return "index";
        }
        
        List<Reserva> conflictingReservations = reservaRepository.findConflictingReservations(
            quartoId, reserva.getDataEntrada(), reserva.getDataSaida());
        
        if (!conflictingReservations.isEmpty()) {
            model.addAttribute("quartos", quartoRepository.findAll());
            model.addAttribute("erro", "Este quarto não está disponível nas datas selecionadas.");
            return "index";
        }
        
        reserva.setQuarto(quarto);
        reserva.setStatus("PENDENTE");
        reserva.setDataCriacao(LocalDate.now());
        
        reservaRepository.save(reserva);
        
        model.addAttribute("sucesso", "Sua solicitação de reserva foi enviada com sucesso! Entraremos em contato em breve.");
        model.addAttribute("reserva", new Reserva());
        model.addAttribute("quartos", quartoRepository.findAll());
        
        return "index";
    }
    
    @GetMapping("/admin")
    public String adminPanel(Model model) {
        model.addAttribute("reservas", reservaRepository.findAll());
        return "admin/dashboard";
    }
    
    @GetMapping("/admin/reservas/{id}")
    public String detalhesReserva(@PathVariable int id, Model model) {
        Optional<Reserva> reserva = reservaRepository.findById(id);
        if (reserva.isPresent()) {
            model.addAttribute("reserva", reserva.get());
            return "admin/detalhes-reserva";
        }
        return "redirect:/admin";
    }
    
    @PostMapping("/admin/reservas/{id}/status")
    public String atualizarStatusReserva(@PathVariable int id, @RequestParam String status) {
        Optional<Reserva> reservaOpt = reservaRepository.findById(id);
        if (reservaOpt.isPresent()) {
            Reserva reserva = reservaOpt.get();
            reserva.setStatus(status);
            reservaRepository.save(reserva);
        }
        return "redirect:/admin/reservas/" + id;
    }
    
    @GetMapping("/admin/quartos")
    public String listarQuartos(Model model) {
        model.addAttribute("quartos", quartoRepository.findAll());
        model.addAttribute("novoQuarto", new Quarto());
        return "admin/quartos";
    }
    
    @PostMapping("/admin/quartos")
    public String adicionarQuarto(@Valid @ModelAttribute("novoQuarto") Quarto quarto, BindingResult result) {
        if (result.hasErrors()) {
            return "admin/quartos";
        }
        quartoRepository.save(quarto);
        return "redirect:/admin/quartos";
    }
    
    @GetMapping("/admin/quartos/{id}")
    public String editarQuarto(@PathVariable int id, Model model) {
        Optional<Quarto> quarto = quartoRepository.findById(id);
        if (quarto.isPresent()) {
            model.addAttribute("quarto", quarto.get());
            return "admin/editar-quarto";
        }
        return "redirect:/admin/quartos";
    }
    
    @PostMapping("/admin/quartos/{id}")
    public String atualizarQuarto(@PathVariable int id, @Valid @ModelAttribute Quarto quartoAtualizado, BindingResult result) {
        if (result.hasErrors()) {
            return "admin/editar-quarto";
        }
        
        Optional<Quarto> quartoOpt = quartoRepository.findById(id);
        if (quartoOpt.isPresent()) {
            Quarto quarto = quartoOpt.get();
            quarto.setTipo(quartoAtualizado.getTipo());
            quarto.setDescricao(quartoAtualizado.getDescricao());
            quarto.setValorDiaria(quartoAtualizado.getValorDiaria());
            quarto.setCapacidadeAdultos(quartoAtualizado.getCapacidadeAdultos());
            quarto.setCapacidadeCriancas(quartoAtualizado.getCapacidadeCriancas());
            quarto.setImagemUrl(quartoAtualizado.getImagemUrl());
            quartoRepository.save(quarto);
        }
        
        return "redirect:/admin/quartos";
    }
}