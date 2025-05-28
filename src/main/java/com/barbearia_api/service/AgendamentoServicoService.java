package com.barbearia_api.service;

import com.barbearia_api.model.Agendamento;
import com.barbearia_api.model.AgendamentoServico;
import com.barbearia_api.model.Funcionario;
import com.barbearia_api.repositories.AgendamentoRepository;
import com.barbearia_api.repositories.AgendamentoServicoRepository;
import com.barbearia_api.repositories.FuncionarioRepository;
import com.barbearia_api.viewmodel.ServicoVmGeral;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AgendamentoServicoService {

    private final AgendamentoServicoRepository repository;
    private final FuncionarioRepository funcionarioRepository;
    private final AgendamentoRepository agendamentoRepository;

    public AgendamentoServicoService(
            AgendamentoServicoRepository repository,
            FuncionarioRepository funcionarioRepository,
            AgendamentoRepository agendamentoRepository
    ) {
        this.repository = repository;
        this.funcionarioRepository = funcionarioRepository;
        this.agendamentoRepository = agendamentoRepository;
    }

    public void salvarServicos(Integer agendamentoId, List<Integer> servicoIds) {
        for (Integer servicoId : servicoIds) {
            AgendamentoServico as = new AgendamentoServico();
            as.setAgendamentoId(agendamentoId);
            as.setServicoId(servicoId);
            repository.save(as);
        }
    }

    public List<ServicoVmGeral> listarPorAgendamento(Integer agendamentoId) {
        return repository.findByAgendamentoId(agendamentoId).stream()
                .map(as -> new ServicoVmGeral(
                        as.getServicoId(),
                        as.getDescricao()
                ))
                .collect(Collectors.toList());
    }

    public List<String> listarHorariosDisponiveis(Integer funcionarioId, String dataAgendamento) {
        Funcionario funcionario = funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));

        List<Agendamento> agendamentosNoDia = agendamentoRepository
                .findByFuncionarioIdAndDataAgendamento(funcionarioId, dataAgendamento);

        List<String> horariosOcupados = agendamentosNoDia.stream()
                .map(Agendamento::getHorario)
                .collect(Collectors.toList());

        LocalTime inicio = LocalTime.parse(funcionario.getHorarioInicio());
        LocalTime fim = LocalTime.parse(funcionario.getHorarioFinal());

        List<String> horariosDisponiveis = new ArrayList<>();
        for (LocalTime hora = inicio; hora.isBefore(fim); hora = hora.plusMinutes(30)) {
            String horaFormatada = hora.toString();
            if (!horariosOcupados.contains(horaFormatada)) {
                horariosDisponiveis.add(horaFormatada);
            }
        }

        return horariosDisponiveis;
    }
}
