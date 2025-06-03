package com.barbearia_api.service;

import com.barbearia_api.dto.agendamento.AgendamentoEditDto;
import com.barbearia_api.dto.agendamento.AgendamentoEditStatusDto;
import com.barbearia_api.dto.agendamento.AgendamentoRegisterDto;
import com.barbearia_api.model.Agendamento;
import com.barbearia_api.model.Funcionario;
import com.barbearia_api.model.Usuario;
import com.barbearia_api.repositories.AgendamentoRepository;
import com.barbearia_api.repositories.FuncionarioRepository;
import com.barbearia_api.repositories.UsuarioRepository;
import com.barbearia_api.viewmodel.AgendamentoVmGeral;
import com.barbearia_api.viewmodel.ServicoVmGeral;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importado

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AgendamentoService {
    private static final Logger logger = LoggerFactory.getLogger(AgendamentoService.class); // Logger

    private final AgendamentoRepository agendamentoRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final AgendamentoServicoService agendamentoServicoService;

    // @Autowired // A injeção via construtor já é suficiente
    private final UsuarioRepository usuarioRepository;

    // Injeção de dependência via construtor é preferível
    public AgendamentoService(
            AgendamentoRepository agendamentoRepository,
            FuncionarioRepository funcionarioRepository,
            UsuarioRepository usuarioRepository, // Mantido aqui pois estava no original
            AgendamentoServicoService agendamentoServicoService
    ) {
        this.agendamentoRepository = agendamentoRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.usuarioRepository = usuarioRepository;
        this.agendamentoServicoService = agendamentoServicoService;
    }

    @Transactional(readOnly = true) // Adicionado
    public List<AgendamentoVmGeral> listAll() {
        // Este método atualmente não carrega os serviços detalhados.
        // Para carregar, adicione a lógica de buscar e setar os serviços como em listByUsuarioId.
        return agendamentoRepository.findAll().stream()
                .map(this::toVmSimple) // Usando um toVm que não busca usuário para cada item
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true) // Adicionado
    public AgendamentoVmGeral listById(Integer id) {
        // Este método atualmente não carrega os serviços detalhados.
        // Para carregar, adicione a lógica de buscar e setar os serviços.
        return agendamentoRepository.findById(id)
                .map(agendamento -> {
                    AgendamentoVmGeral vm = toVm(agendamento); // toVm com detalhes do usuário
                    // Opcional: Carregar serviços se necessário aqui também
                    // List<ServicoVmGeral> servicos = agendamentoServicoService.listarPorAgendamento(agendamento.getId());
                    // vm.setServicos(servicos);
                    return vm;
                })
                .orElse(null);
    }

    @Transactional(readOnly = true) // Adicionado
    public List<AgendamentoVmGeral> listByUsuarioId() {
        Usuario usuario = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer usuarioId = usuario.getId();
        logger.info("Listando agendamentos para o usuário ID: {}", usuarioId);

        return agendamentoRepository.findByUsuarioId(usuarioId).stream()
                .map(agendamento -> {
                    AgendamentoVmGeral vm = toVm(agendamento);
                    List<ServicoVmGeral> servicos = agendamentoServicoService.listarPorAgendamento(agendamento.getId());
                    vm.setServicos(servicos);
                    logger.debug("Agendamento ID: {} - Serviços carregados: {}", agendamento.getId(), servicos.size());
                    return vm;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AgendamentoVmGeral> listByfuncionarioId(Integer id) {
        logger.info("Listando agendamentos para o funcionário ID: {}", id);
        return agendamentoRepository.findByFuncionarioId(id).stream()
                .map(agendamento -> {
                    AgendamentoVmGeral vm = toVm(agendamento); // Converte para o ViewModel básico

                    // Agora, explicitamente buscamos e setamos os serviços para este agendamento
                    List<ServicoVmGeral> servicos = agendamentoServicoService.listarPorAgendamento(agendamento.getId());
                    vm.setServicos(servicos);

                    logger.debug("Agendamento ID: {} para funcionário ID: {} - Serviços carregados: {}", agendamento.getId(), id, servicos.size());
                    return vm;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true) // Adicionado
    public List<AgendamentoVmGeral> listByfuncionario() {
        // Este método atualmente não carrega os serviços detalhados.
        // Para carregar, adicione a lógica de buscar e setar os serviços.
        Usuario usuario = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // Supondo que o ID do usuário logado seja o ID do funcionário a ser buscado.
        // Se a lógica for diferente (ex: buscar funcionário associado ao usuário), ajuste aqui.
        Integer funcionarioId = usuario.getId(); // Precisa confirmar se o ID do usuário é o ID do funcionário

        Funcionario funcionario = funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> new RuntimeException("Funcionário não encontrado para o usuário logado."));


        logger.info("Listando agendamentos para o funcionário ID: {}", funcionario.getId());
        return agendamentoRepository.findByFuncionarioId(funcionario.getId()).stream()
                .map(agendamento -> {
                    AgendamentoVmGeral vm = toVm(agendamento);
                    // Opcional: Carregar serviços se necessário aqui também
                    // List<ServicoVmGeral> servicos = agendamentoServicoService.listarPorAgendamento(agendamento.getId());
                    // vm.setServicos(servicos);
                    return vm;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true) // Adicionado
    public List<AgendamentoVmGeral> listByHorarioAndData(String horario, String dataAgendamento) {
        // Este método atualmente não carrega os serviços detalhados.
        // Para carregar, adicione a lógica de buscar e setar os serviços.
        return agendamentoRepository.findByHorarioAndDataAgendamento(horario, dataAgendamento).stream()
                .map(this::toVm)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true) // Adicionado
    public List<AgendamentoVmGeral> listByFuncionarioIdAndDataAndHorario(Integer funcionarioId, String dataAgendamentoIso, String horario) {
        String dataAgendamentoParaRepositorio;
        try {
            LocalDate date = LocalDate.parse(dataAgendamentoIso, DateTimeFormatter.ISO_LOCAL_DATE);
            dataAgendamentoParaRepositorio = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (DateTimeParseException e) {
            logger.error("Formato de data inválido: {}. Esperado: yyyy-MM-dd.", dataAgendamentoIso, e);
            throw new RuntimeException("Formato de data inválido fornecido para consulta. Esperado: yyyy-MM-dd. Recebido: " + dataAgendamentoIso, e);
        }

        logger.info("Listando agendamentos para funcionário ID: {}, Data: {}, Horário: {}", funcionarioId, dataAgendamentoParaRepositorio, horario);
        List<Agendamento> agendamentosEncontrados = agendamentoRepository.findByFuncionarioIdAndDataAgendamentoAndHorario(
                funcionarioId,
                dataAgendamentoParaRepositorio,
                horario
        );

        return agendamentosEncontrados.stream()
                .map(agendamento -> {
                    AgendamentoVmGeral vm = toVm(agendamento);
                    List<ServicoVmGeral> servicos = agendamentoServicoService.listarPorAgendamento(agendamento.getId());
                    vm.setServicos(servicos);
                    logger.debug("Agendamento ID: {} - Serviços carregados: {}", agendamento.getId(), servicos.size());
                    return vm;
                })
                .collect(Collectors.toList());
    }

    @Transactional // Adicionado (não é readOnly pois modifica dados)
    public AgendamentoVmGeral register(AgendamentoRegisterDto agendamentoRegisterDto) {
        Usuario usuario = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer usuarioId = usuario.getId();
        logger.info("Registrando novo agendamento para usuário ID: {} por funcionário ID: {}", usuarioId, agendamentoRegisterDto.getFuncionarioId());

        Funcionario funcionario = funcionarioRepository.findById(agendamentoRegisterDto.getFuncionarioId())
                .orElseThrow(() -> new RuntimeException("Funcionário não encontrado com ID: " + agendamentoRegisterDto.getFuncionarioId()));

        LocalTime horarioAgendamento = LocalTime.parse(agendamentoRegisterDto.getHorario());
        LocalTime horarioInicioFuncionario = LocalTime.parse(funcionario.getHorarioInicio());
        LocalTime horarioFinalFuncionario = LocalTime.parse(funcionario.getHorarioFinal());

        if (horarioAgendamento.isBefore(horarioInicioFuncionario) || horarioAgendamento.isAfter(horarioFinalFuncionario)) {
            logger.warn("Tentativa de agendamento fora do expediente: {} para funcionário {}", horarioAgendamento, funcionario.getId());
            throw new RuntimeException("Horário fora do expediente do funcionário.");
        }

        boolean existeConflito = agendamentoRepository.existsByFuncionarioIdAndDataAgendamentoAndHorario(
                agendamentoRegisterDto.getFuncionarioId(),
                agendamentoRegisterDto.getDataAgendamento(),
                agendamentoRegisterDto.getHorario()
        );

        if (existeConflito) {
            logger.warn("Conflito de agendamento detectado para funcionário ID: {}, Data: {}, Horário: {}",
                    agendamentoRegisterDto.getFuncionarioId(), agendamentoRegisterDto.getDataAgendamento(), agendamentoRegisterDto.getHorario());
            throw new RuntimeException("Já existe um agendamento nesse horário para esse funcionário.");
        }

        String crc = "f" + agendamentoRegisterDto.getFuncionarioId() +
                "-h" + agendamentoRegisterDto.getHorario() +
                "-d" + agendamentoRegisterDto.getDataAgendamento();

        Agendamento agendamento = new Agendamento(
                null,
                agendamentoRegisterDto.getFuncionarioId(),
                usuarioId,
                agendamentoRegisterDto.getHorario(),
                agendamentoRegisterDto.getDataAgendamento(),
                Agendamento.StatusAgendamento.ESPERA,
                crc
        );

        agendamento = agendamentoRepository.save(agendamento);
        logger.info("Agendamento ID: {} salvo.", agendamento.getId());

        if (agendamentoRegisterDto.getServicoId() != null && !agendamentoRegisterDto.getServicoId().isEmpty()) {
            agendamentoServicoService.salvarServicos(agendamento.getId(), agendamentoRegisterDto.getServicoId());
            logger.info("Serviços associados ao agendamento ID: {}", agendamento.getId());
        } else {
            logger.info("Nenhum serviço para associar ao agendamento ID: {}", agendamento.getId());
        }

        AgendamentoVmGeral vm = toVm(agendamento);
        List<ServicoVmGeral> servicos = agendamentoServicoService.listarPorAgendamento(agendamento.getId());
        vm.setServicos(servicos); // Popula os serviços no retorno
        logger.debug("Retornando AgendamentoVmGeral com {} serviços para agendamento ID: {}", servicos.size(), agendamento.getId());

        return vm;
    }

    @Transactional // Adicionado
    public AgendamentoVmGeral update(AgendamentoEditDto agendamentoEditDto) {
        logger.info("Atualizando agendamento ID: {}", agendamentoEditDto.getId());
        Agendamento agendamento = agendamentoRepository.findById(agendamentoEditDto.getId())
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado com o ID: " + agendamentoEditDto.getId()));

        // Validações adicionais (ex: horário, conflito) podem ser necessárias aqui como no register
        // e se os serviços também podem ser editados.

        String crc = "f" + agendamentoEditDto.getFuncionarioId() +
                "-h" + agendamentoEditDto.getHorario() +
                "-d" + agendamentoEditDto.getDataAgendamento();

        agendamento.setFuncionarioId(agendamentoEditDto.getFuncionarioId());
        agendamento.setHorario(agendamentoEditDto.getHorario());
        agendamento.setDataAgendamento(agendamentoEditDto.getDataAgendamento());
        agendamento.setCrc(crc);
        // Se os serviços puderem ser alterados na edição, adicione a lógica aqui:
        // agendamentoServicoService.removerServicosAntigos(agendamento.getId());
        // agendamentoServicoService.salvarServicos(agendamento.getId(), agendamentoEditDto.getServicoIds());

        agendamento = agendamentoRepository.save(agendamento);
        AgendamentoVmGeral vm = toVm(agendamento);

        // Popula os serviços no retorno se relevante para a atualização
        List<ServicoVmGeral> servicos = agendamentoServicoService.listarPorAgendamento(agendamento.getId());
        vm.setServicos(servicos);
        return vm;
    }

    @Transactional // Adicionado
    public AgendamentoVmGeral updateStatusAgendamento(AgendamentoEditStatusDto agendamentoEditStatusDto) {
        logger.info("Atualizando status do agendamento ID: {} para {}", agendamentoEditStatusDto.getId(), agendamentoEditStatusDto.getStatusAgendamento());
        Agendamento agendamento = agendamentoRepository.findById(agendamentoEditStatusDto.getId())
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado com o ID: " + agendamentoEditStatusDto.getId()));

        agendamento.setStatusAgendamento(agendamentoEditStatusDto.getStatusAgendamento());
        agendamento = agendamentoRepository.save(agendamento);

        AgendamentoVmGeral vm = toVm(agendamento);
        // Popula os serviços no retorno se relevante para a atualização de status
        List<ServicoVmGeral> servicos = agendamentoServicoService.listarPorAgendamento(agendamento.getId());
        vm.setServicos(servicos);
        return vm;
    }

    // Este método agora busca o usuário e formata os dados do agendamento.
    // Não busca os serviços, isso é feito separadamente onde necessário.
    private AgendamentoVmGeral toVm(Agendamento agendamento) {
        Usuario usuario = usuarioRepository.findById(agendamento.getUsuarioId())
                .orElseThrow(() -> {
                    logger.error("Usuário não encontrado com ID: {} para agendamento ID: {}", agendamento.getUsuarioId(), agendamento.getId());
                    return new RuntimeException("Usuário não encontrado para o agendamento.");
                });

        String dataAgendamentoFormatadaParaApi;
        Object dataOriginalObj = agendamento.getDataAgendamento();

        if (dataOriginalObj instanceof LocalDate) {
            dataAgendamentoFormatadaParaApi = ((LocalDate) dataOriginalObj).toString(); // yyyy-MM-dd
        } else if (dataOriginalObj instanceof String) {
            String dataOriginalStr = (String) dataOriginalObj;
            if (dataOriginalStr.matches("^\\d{2}/\\d{2}/\\d{4}$")) { // Se for "dd/MM/yyyy"
                try {
                    LocalDate dataLocalDate = LocalDate.parse(dataOriginalStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    dataAgendamentoFormatadaParaApi = dataLocalDate.toString(); // Converte para "yyyy-MM-dd"
                } catch (DateTimeParseException e) {
                    logger.warn("Erro ao parsear a data do agendamento '{}' no toVm. Retornando original. Agendamento ID: {}. Erro: {}", dataOriginalStr, agendamento.getId(), e.getMessage());
                    dataAgendamentoFormatadaParaApi = dataOriginalStr;
                }
            } else {
                dataAgendamentoFormatadaParaApi = dataOriginalStr; // Assume que já está no formato desejado ou é um fallback
            }
        } else {
            logger.warn("Tipo de dataAgendamento inesperado ou nulo no toVm para agendamento ID: {}. Tipo: {}", agendamento.getId(), dataOriginalObj != null ? dataOriginalObj.getClass().getName() : "null");
            dataAgendamentoFormatadaParaApi = null;
        }

        return new AgendamentoVmGeral(
                agendamento.getId(),
                agendamento.getFuncionarioId(),
                agendamento.getUsuarioId(),
                agendamento.getHorario(),
                dataAgendamentoFormatadaParaApi,
                agendamento.getStatusAgendamento(),
                agendamento.getCrc(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getTelefone()
        );
    }

    // Um toVm mais simples que não busca dados do usuário, ideal para listas grandes onde o usuário pode ser repetido
    // ou não é necessário imediatamente.
    private AgendamentoVmGeral toVmSimple(Agendamento agendamento) {
        String dataAgendamentoFormatadaParaApi;
        Object dataOriginalObj = agendamento.getDataAgendamento();

        if (dataOriginalObj instanceof LocalDate) {
            dataAgendamentoFormatadaParaApi = ((LocalDate) dataOriginalObj).toString();
        } else if (dataOriginalObj instanceof String) {
            String dataOriginalStr = (String) dataOriginalObj;
            if (dataOriginalStr.matches("^\\d{2}/\\d{2}/\\d{4}$")) {
                try {
                    LocalDate dataLocalDate = LocalDate.parse(dataOriginalStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    dataAgendamentoFormatadaParaApi = dataLocalDate.toString();
                } catch (DateTimeParseException e) {
                    dataAgendamentoFormatadaParaApi = dataOriginalStr;
                }
            } else {
                dataAgendamentoFormatadaParaApi = dataOriginalStr;
            }
        } else {
            dataAgendamentoFormatadaParaApi = null;
        }
        // Cria o VM sem os dados do usuário. Estes podem ser carregados separadamente se necessário.
        AgendamentoVmGeral vm = new AgendamentoVmGeral();
        vm.setId(agendamento.getId());
        vm.setFuncionarioId(agendamento.getFuncionarioId());
        vm.setUsuarioId(agendamento.getUsuarioId());
        vm.setHorario(agendamento.getHorario());
        vm.setDataAgendamento(dataAgendamentoFormatadaParaApi);
        vm.setStatusAgendamento(agendamento.getStatusAgendamento());
        vm.setCrc(agendamento.getCrc());
        return vm;
    }
}