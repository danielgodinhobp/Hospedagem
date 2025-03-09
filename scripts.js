document.addEventListener('DOMContentLoaded', function() {
    
    const formularioReserva = document.getElementById('formularioReserva');
    
    if (formularioReserva) {
        formularioReserva.addEventListener('submit', function(event) {

            event.preventDefault();
            
            const nome = document.getElementById('campoNome').value;
            const email = document.getElementById('campoEmail').value;
            const dataEntrada = document.getElementById('campoDataEntrada').value;
            const dataSaida = document.getElementById('campoDataSaida').value;
            const adultos = document.getElementById('campoAdultos').value;
            const criancas = document.getElementById('campoCriancas').value;
            const observacoes = document.getElementById('campoObservacoes').value;
            
            if (new Date(dataSaida) <= new Date(dataEntrada)) {
                alert('A data de saída deve ser posterior à data de entrada.');
                return;
            }
            
            console.log('Dados da reserva:', {
                nome,
                email,
                dataEntrada,
                dataSaida,
                adultos,
                criancas,
                observacoes
            });
            
            alert('Sua solicitação de reserva foi enviada com sucesso! Entraremos em contato em breve.');
            
            formularioReserva.reset();
        });
    }
});