document.addEventListener('DOMContentLoaded', function() {
    let quartos = [];
    let quartoSelecionado = null;
    
    const hoje = new Date();
    const amanha = new Date(hoje);
    amanha.setDate(amanha.getDate() + 1);
    
    const dataEntradaPicker = flatpickr("#dataEntrada", {
        locale: "pt",
        dateFormat: "Y-m-d",
        altInput: true,
        altFormat: "d/m/Y",
        minDate: "today",
        defaultDate: hoje,
        onChange: function(selectedDates, dateStr) {
            const dataSaidaPicker = document.getElementById("dataSaida")._flatpickr;
            const novaMinimaDataSaida = new Date(selectedDates[0]);
            novaMinimaDataSaida.setDate(novaMinimaDataSaida.getDate() + 1);
            
            dataSaidaPicker.set("minDate", novaMinimaDataSaida);
            
            if (dataSaidaPicker.selectedDates[0] < novaMinimaDataSaida) {
                dataSaidaPicker.setDate(novaMinimaDataSaida);
            }
            resetarDisponibilidade();
        }
    });
    
    const dataSaidaPicker = flatpickr("#dataSaida", {
        locale: "pt",
        dateFormat: "Y-m-d",
        altInput: true,
        altFormat: "d/m/Y",
        minDate: amanha,
        defaultDate: amanha,
        onChange: function() {
            resetarDisponibilidade();
        }
    });
    
    carregarQuartos();
    
    document.getElementById('btnVerificarDisponibilidade').addEventListener('click', verificarDisponibilidade);
    document.getElementById('formReserva').addEventListener('submit', fazerReserva);
    document.getElementById('btnReservarModal').addEventListener('click', function() {
        document.querySelector('#reserva').scrollIntoView({ behavior: 'smooth' });
        const modal = bootstrap.Modal.getInstance(document.getElementById('quartoModal'));
        modal.hide();
    });

    document.getElementById('quartoSelect').addEventListener('change', resetarDisponibilidade);
    document.getElementById('adultos').addEventListener('change', resetarDisponibilidade);
    document.getElementById('criancas').addEventListener('change', resetarDisponibilidade);
    document.getElementById('nome').addEventListener('input', resetarDisponibilidade);
    document.getElementById('email').addEventListener('input', resetarDisponibilidade);
    document.getElementById('observacoes').addEventListener('input', resetarDisponibilidade);
    
    function carregarQuartos() {
        fetch('/api/quartos')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Erro ao carregar os quartos');
                }
                return response.json();
            })
            .then(data => {
                quartos = data;
                const tiposUnicos = [];
                const quartosPorTipo = data.reduce((acc, quarto) => {
                    if (!tiposUnicos.includes(quarto.tipo)) {
                        tiposUnicos.push(quarto.tipo);
                        acc.push(quarto);
                    }
                    return acc;
                }, []);
                renderizarQuartos(quartosPorTipo);
                preencherSelectQuartos(data);
            })
            .catch(error => {
                console.error('Erro:', error);
                mostrarMensagem('error', 'Não foi possível carregar os quartos. Por favor, tente novamente mais tarde.');
            });
    }
    
    function renderizarQuartos(quartos) {
        const container = document.getElementById('listaQuartos');
        container.innerHTML = '';
        
        quartos.forEach(quarto => {
            let imagemUrl = quarto.imagemUrl || '/images/quarto-default.jpg';
            
            const card = document.createElement('div');
            card.className = 'col-md-4 mb-4';
            card.innerHTML = `
                <div class="card quarto-card">
                    <img src="${imagemUrl}" class="card-img-top quarto-img" alt="${quarto.tipo}">
                    <div class="card-body">
                        <h5 class="card-title">${quarto.tipo}</h5>
                        <p class="card-text">${quarto.descricao ? quarto.descricao.substring(0, 100) + '...' : 'Sem descrição'}</p>
                        <p class="card-text text-primary fw-bold">R$ ${quarto.valorDiaria.toFixed(2)} / diária</p>
                        <p class="card-text">
                            <small class="text-muted">
                                Capacidade: ${quarto.capacidadeAdultos} adultos e ${quarto.capacidadeCriancas} crianças
                            </small>
                        </p>
                        <button class="btn btn-outline-primary btn-sm btn-detalhes" data-id="${quarto.id}">Ver Detalhes</button>
                    </div>
                </div>
            `;
            container.appendChild(card);
            
            card.querySelector('.btn-detalhes').addEventListener('click', () => abrirModalQuarto(quarto));
        });
    }
    
    function preencherSelectQuartos(quartos) {
        const select = document.getElementById('quartoSelect');
        select.innerHTML = '<option value="">Selecione um quarto</option>';
        
        quartos.forEach(quarto => {
            const option = document.createElement('option');
            option.value = quarto.id;
            option.textContent = `${quarto.tipo} - Quarto ${quarto.numero} - R$ ${quarto.valorDiaria.toFixed(2)} / diária`;
            select.appendChild(option);
        });
        
        select.addEventListener('change', function() {
            if (this.value) {
                quartoSelecionado = quartos.find(q => q.id == this.value);
            } else {
                quartoSelecionado = null;
            }
        });
    }
    
    function abrirModalQuarto(quarto) {
        const modalLabel = document.getElementById('quartoModalLabel');
        const modalBody = document.getElementById('quartoModalBody');
        
        let imagemUrl = quarto.imagemUrl || '/images/quarto-default.jpg';
        
        modalLabel.textContent = `${quarto.tipo}`;
        modalBody.innerHTML = `
            <div class="row">
                <div class="col-md-6">
                    <img src="${imagemUrl}" class="img-fluid rounded" alt="${quarto.tipo}">
                </div>
                <div class="col-md-6">
                    <h5>Detalhes</h5>
                    <p>${quarto.descricao || 'Sem descrição disponível.'}</p>
                    <ul class="list-group mb-3">
                        <li class="list-group-item d-flex justify-content-between align-items-center">
                            Valor da diária
                            <span class="badge bg-primary rounded-pill">R$ ${quarto.valorDiaria.toFixed(2)}</span>
                        </li>
                        <li class="list-group-item d-flex justify-content-between align-items-center">
                            Capacidade de adultos
                            <span class="badge bg-secondary rounded-pill">${quarto.capacidadeAdultos}</span>
                        </li>
                        <li class="list-group-item d-flex justify-content-between align-items-center">
                            Capacidade de crianças
                            <span class="badge bg-secondary rounded-pill">${quarto.capacidadeCriancas}</span>
                        </li>
                    </ul>
                </div>
            </div>
        `;
        
        document.getElementById('btnReservarModal').setAttribute('data-id', quarto.id);
        
        const modal = new bootstrap.Modal(document.getElementById('quartoModal'));
        modal.show();
    }
    
    function verificarDisponibilidade() {
        const quartoId = document.getElementById('quartoSelect').value;
        const dataEntrada = document.getElementById('dataEntrada').value;
        const dataSaida = document.getElementById('dataSaida').value;
        const adultos = parseInt(document.getElementById('adultos').value);
        const criancas = parseInt(document.getElementById('criancas').value);
        const nome = document.getElementById('nome').value;
        const email = document.getElementById('email').value;
        const observacoes = document.getElementById('observacoes').value; 
        
        if (!quartoId || !dataEntrada || !dataSaida || !nome || !email) {
            mostrarMensagem('error', 'Por favor, preencha todos os campos obrigatórios.');
            return;
        }
        
        const quarto = quartos.find(q => q.id == quartoId);
        if (adultos > quarto.capacidadeAdultos || criancas > quarto.capacidadeCriancas) {
            mostrarMensagem('error', 'Este quarto não comporta a quantidade de hóspedes informada.');
            return;
        }
        
        const inicio = new Date(dataEntrada);
        const fim = new Date(dataSaida);
        const diarias = Math.ceil((fim - inicio) / (1000 * 60 * 60 * 24));
        const valorTotal = diarias * quarto.valorDiaria;
        
        fetch(`/api/reservas/disponibilidade?quartoId=${quartoId}&dataEntrada=${dataEntrada}&dataSaida=${dataSaida}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Erro ao verificar disponibilidade');
                }
                return response.json();
            })
            .then(disponivel => {
                if (disponivel) {
                    const resumo = document.getElementById('resumoReserva');
                    const dataEntradaExibicao = dataEntrada.split('-').reverse().join('/');
                    const dataSaidaExibicao = dataSaida.split('-').reverse().join('/');
                    resumo.innerHTML = `
                        <h5>Resumo da Reserva</h5>
                        <p><strong>Quarto:</strong> ${quarto.tipo} - Quarto ${quarto.numero}</p>
                        <p><strong>Período:</strong> ${dataEntradaExibicao} a ${dataSaidaExibicao} (${diarias} ${diarias > 1 ? 'diárias' : 'diária'})</p>
                        <p><strong>Hóspedes:</strong> ${adultos} ${adultos > 1 ? 'adultos' : 'adulto'} e ${criancas} ${criancas > 1 ? 'crianças' : 'criança'}</p>
                        <p><strong>Valor Total:</strong> R$ ${valorTotal.toFixed(2)}</p>
                        ${observacoes ? `<p><strong>Observações:</strong> ${observacoes}</p>` : ''}
                    `;
                    resumo.classList.remove('d-none');
                    
                    document.getElementById('btnVerificarDisponibilidade').classList.add('d-none');
                    document.getElementById('btnReservar').classList.remove('d-none');
                    
                    mostrarMensagem('success', 'Quarto disponível para as datas selecionadas! Confirme a reserva abaixo!');
                } else {
                    mostrarMensagem('error', 'Este quarto não está disponível nas datas selecionadas. Por favor, escolha outras datas ou outro quarto.');
                }
            })
            .catch(error => {
                console.error('Erro:', error);
                mostrarMensagem('error', 'Não foi possível verificar a disponibilidade. Por favor, tente novamente.');
            });
    }
    
    function fazerReserva(event) {
        event.preventDefault();
        
        const quartoId = document.getElementById('quartoSelect').value;
        const quarto = { id: parseInt(quartoId) };
        const dataEntrada = document.getElementById('dataEntrada').value;
        const dataSaida = document.getElementById('dataSaida').value;
        const adultos = parseInt(document.getElementById('adultos').value);
        const criancas = parseInt(document.getElementById('criancas').value);
        const nome = document.getElementById('nome').value;
        const email = document.getElementById('email').value;
        const observacoes = document.getElementById('observacoes').value;
        
        const dadosReserva = {
            quarto: quarto,
            nome: nome,
            email: email,
            dataEntrada: dataEntrada,
            dataSaida: dataSaida,
            adultos: adultos,
            criancas: criancas,
            observacoes: observacoes,
            status: "PENDENTE"
        };
        
        fetch('/api/reservas', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(dadosReserva)
        })
        .then(response => {
            if (!response.ok) {
                return response.json().then(err => {
                    throw new Error(err.message || 'Erro ao fazer a reserva');
                });
            }
            return response.json();
        })
        .then(data => {
            document.getElementById('formReserva').reset();
            document.getElementById('resumoReserva').classList.add('d-none');
            document.getElementById('btnReservar').classList.add('d-none');
            document.getElementById('btnVerificarDisponibilidade').classList.remove('d-none');
            
            mostrarMensagem('success', 'Sua reserva foi realizada com sucesso! Enviamos um email com os detalhes.');
        })
        .catch(error => {
            console.error('Erro:', error);
            mostrarMensagem('error', error.message);
        });
    }
    
    function resetarDisponibilidade() {
        const resumo = document.getElementById('resumoReserva');
        const btnVerificar = document.getElementById('btnVerificarDisponibilidade');
        const btnReservar = document.getElementById('btnReservar');
        const alertSuccess = document.getElementById('alertSuccess');
        const alertError = document.getElementById('alertError');
        
        resumo.classList.add('d-none');
        btnVerificar.classList.remove('d-none');
        btnReservar.classList.add('d-none');
        alertSuccess.classList.add('d-none');
        alertError.classList.add('d-none');
    }
    
    function mostrarMensagem(tipo, mensagem) {
        const alertSuccess = document.getElementById('alertSuccess');
        const alertError = document.getElementById('alertError');
        
        if (tipo === 'success') {
            document.getElementById('alertSuccessMessage').textContent = mensagem;
            alertSuccess.classList.remove('d-none');
            alertError.classList.add('d-none');
            
            setTimeout(() => {
                alertSuccess.classList.add('d-none');
            }, 10000);
        } else {
            document.getElementById('alertErrorMessage').textContent = mensagem;
            alertError.classList.remove('d-none');
            alertSuccess.classList.add('d-none');
            
            setTimeout(() => {
                alertError.classList.add('d-none');
            }, 10000);
        }
    }
});