package com.barbearia_api.repositories;

import com.barbearia_api.model.Agendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Integer> {
    List<Agendamento> findByHorarioAndDataAgendamento(String horario, String dataAgendamento);
    List<Agendamento> findByUsuarioId(Integer usuarioId);
    List<Agendamento> findByFuncionarioId(Integer funcionarioId);
    boolean existsByFuncionarioIdAndDataAgendamentoAndHorario(Integer funcionarioId, String dataAgendamento, String horario);
    List<Agendamento> findByFuncionarioIdAndDataAgendamentoAndHorario(Integer funcionarioId, String dataAgendamento, String horario);
}
