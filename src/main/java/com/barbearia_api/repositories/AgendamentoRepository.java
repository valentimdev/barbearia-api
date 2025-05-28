package com.barbearia_api.repositories;

import com.barbearia_api.model.Agendamento;
// Se o seu enum StatusAgendamento estiver em um pacote diferente ou for uma classe separada,
// pode ser necessário um import específico aqui para ele, embora para JPQL o caminho completo seja usado.
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Integer> {

    List<Agendamento> findByHorarioAndDataAgendamento(String horario, String dataAgendamento);

    List<Agendamento> findByUsuarioId(Integer usuarioId);

    List<Agendamento> findByFuncionarioId(Integer funcionarioId);

    // Método original que NÃO considera o status do agendamento
    boolean existsByFuncionarioIdAndDataAgendamentoAndHorario(Integer funcionarioId, String dataAgendamento, String horario);

    List<Agendamento> findByFuncionarioIdAndDataAgendamento(Integer funcionarioId, String dataAgendamento);

    // --- NOVO MÉTODO ADICIONADO ---
    /**
     * Verifica se existe um agendamento para um funcionário específico, em uma data e horário,
     * excluindo aqueles com status DESMARCADO.
     *
     * ❗ IMPORTANTE: Ajuste o caminho 'com.barbearia_api.model.Agendamento.StatusAgendamento.DESMARCADO'
     * para o caminho completo e correto do seu enum StatusAgendamento.
     * Se StatusAgendamento for um enum público estático dentro da classe Agendamento
     * (e Agendamento estiver em com.barbearia_api.model), este caminho estará correto.
     * Se for um enum separado (ex: com.barbearia_api.model.enums.StatusAgendamento), ajuste-o.
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN TRUE ELSE FALSE END " +
            "FROM Agendamento a " +
            "WHERE a.funcionarioId = :funcionarioId " +
            "AND a.dataAgendamento = :dataAgendamento " +
            "AND a.horario = :horario " +
            "AND a.statusAgendamento <> com.barbearia_api.model.Agendamento.StatusAgendamento.DESMARCADO")
    boolean existsActiveByFuncionarioIdAndDataAgendamentoAndHorario(
            @Param("funcionarioId") Integer funcionarioId,
            @Param("dataAgendamento") String dataAgendamento,
            @Param("horario") String horario
    );
}