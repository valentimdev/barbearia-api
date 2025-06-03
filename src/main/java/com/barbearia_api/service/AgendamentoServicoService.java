package com.barbearia_api.service;

import com.barbearia_api.model.AgendamentoServico;
import com.barbearia_api.model.Servico; // Supondo que você tenha uma entidade Servico
import com.barbearia_api.repositories.AgendamentoServicoRepository;
// import com.barbearia_api.repositories.ServicoRepository; // Poderia ser útil se precisasse buscar detalhes do serviço
import com.barbearia_api.viewmodel.ServicoVmGeral;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importado

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AgendamentoServicoService {

    private static final Logger logger = LoggerFactory.getLogger(AgendamentoServicoService.class); // Logger

    private final AgendamentoServicoRepository repository;
    // private final ServicoRepository servicoRepository; // Opcional

    public AgendamentoServicoService(AgendamentoServicoRepository repository /*, ServicoRepository servicoRepository */) {
        this.repository = repository;
        // this.servicoRepository = servicoRepository;
    }

    @Transactional // Adicionado (não é readOnly pois modifica dados)
    public void salvarServicos(Integer agendamentoId, List<Integer> servicoIds) {
        if (servicoIds == null || servicoIds.isEmpty()) {
            logger.info("Nenhum servicoId fornecido para salvar para o agendamentoId: {}", agendamentoId);
            return;
        }
        logger.info("Salvando {} serviços para o agendamentoId: {}", servicoIds.size(), agendamentoId);
        for (Integer servicoId : servicoIds) {
            // Adicionar verificação se o Serviço com servicoId existe antes de salvar AgendamentoServico
            // Ex: if (!servicoRepository.existsById(servicoId)) { throw new RuntimeException("Serviço não encontrado: " + servicoId); }

            AgendamentoServico as = new AgendamentoServico();
            as.setAgendamentoId(agendamentoId);
            as.setServicoId(servicoId);
            repository.save(as);
        }
    }

    @Transactional(readOnly = true) // Adicionado
    public List<ServicoVmGeral> listarPorAgendamento(Integer agendamentoId) {
        logger.debug("Listando serviços para agendamentoId: {}", agendamentoId);
        List<AgendamentoServico> agendamentoServicos = repository.findByAgendamentoId(agendamentoId);

        if (agendamentoServicos.isEmpty()) {
            logger.debug("Nenhum AgendamentoServico encontrado para agendamentoId: {}", agendamentoId);
            return Collections.emptyList(); // Retorna lista vazia explicitamente
        }

        logger.debug("Encontrados {} AgendamentoServico para agendamentoId: {}", agendamentoServicos.size(), agendamentoId);

        return agendamentoServicos.stream()
                .map(as -> {
                    // A entidade AgendamentoServico deve ter uma referência à entidade Servico
                    // e um método getDescricao() que acessa a descrição através dessa referência.
                    // Ex: as.getServico().getDescricao()
                    // Se AgendamentoServico.getServico() for null (devido a FetchType.LAZY e falta de transação,
                    // ou se o serviço referenciado não existir), getDescricao() deve lidar com isso.
                    String descricao = as.getDescricao(); // Depende da implementação de AgendamentoServico.getDescricao()
                    // e da entidade Servico estar carregada (FetchType.LAZY)
                    if (descricao == null) {
                        logger.warn("Descrição nula para servicoId: {} no agendamentoId: {}. Verifique o carregamento da entidade Servico.", as.getServicoId(), agendamentoId);
                        // Poderia buscar o nome do serviço diretamente aqui se necessário,
                        // mas o ideal é que a entidade AgendamentoServico resolva isso.
                        // Servico servicoDetalhe = servicoRepository.findById(as.getServicoId()).orElse(null);
                        // descricao = (servicoDetalhe != null) ? servicoDetalhe.getDescricao() : "Descrição Indisponível";
                        descricao = "Descrição Indisponível";
                    }
                    logger.trace("Mapeando ServicoVmGeral para servicoId: {}, descricao: '{}'", as.getServicoId(), descricao);
                    return new ServicoVmGeral(
                            as.getServicoId(),
                            descricao
                    );
                })
                .collect(Collectors.toList());
    }
}