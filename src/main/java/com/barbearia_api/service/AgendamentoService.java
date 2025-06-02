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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AgendamentoService {
    private final AgendamentoRepository agendamentoRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final AgendamentoServicoService agendamentoServicoService;


    @Autowired
    private UsuarioRepository usuarioRepository;

    public AgendamentoService(
            AgendamentoRepository agendamentoRepository,
            FuncionarioRepository funcionarioRepository,
            UsuarioRepository usuarioRepository,
            AgendamentoServicoService agendamentoServicoService
    ) {
        this.agendamentoRepository = agendamentoRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.usuarioRepository = usuarioRepository;
        this.agendamentoServicoService = agendamentoServicoService;
    }


    public List<AgendamentoVmGeral> listAll() {
        return agendamentoRepository.findAll().stream()
                .map(this::toVm)
                .collect(Collectors.toList());
    }

    public AgendamentoVmGeral listById(Integer id) {
        return agendamentoRepository.findById(id)
                .map(this::toVm)
                .orElse(null);
    }

    public List<AgendamentoVmGeral> listByUsuarioId() {
        Usuario usuario = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer usuarioId = usuario.getId();

        return agendamentoRepository.findByUsuarioId(usuarioId).stream()
                .map(agendamento -> {
                    AgendamentoVmGeral vm = toVm(agendamento);

                    // Busca os serviços vinculados a esse agendamento
                    List<ServicoVmGeral> servicos = agendamentoServicoService.listarPorAgendamento(agendamento.getId());
                    vm.setServicos(servicos);

                    return vm;
                })
                .collect(Collectors.toList());
    }

    public List<AgendamentoVmGeral> listByfuncionarioId(Integer id) {
        return agendamentoRepository.findByFuncionarioId(id).stream()
                .map(this::toVm)
                .collect(Collectors.toList());
    }

    public List<AgendamentoVmGeral> listByfuncionario() {
        Usuario usuario = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer usuarioId = usuario.getId();

        return agendamentoRepository.findByFuncionarioId(usuarioId).stream()
                .map(this::toVm)
                .collect(Collectors.toList());
    }

    public List<AgendamentoVmGeral> listByHorarioAndData(String horario, String dataAgendamento) {
        return agendamentoRepository.findByHorarioAndDataAgendamento(horario, dataAgendamento).stream()
                .map(this::toVm)
                .collect(Collectors.toList());
    }

    public List<AgendamentoVmGeral> listByFuncionarioIdAndDataAndHorario(Integer funcionarioId, String dataAgendamentoIso, String horario) {
        // dataAgendamentoIso é esperado do controller no formato "yyyy-MM-dd"

        String dataAgendamentoParaRepositorio;
        try {
            // Parseia a data ISO "yyyy-MM-dd"
            LocalDate date = LocalDate.parse(dataAgendamentoIso, DateTimeFormatter.ISO_LOCAL_DATE);
            // Formata para "dd/MM/yyyy" que é o formato esperado pelo repositório (baseado no seu DTO de registro e DB)
            dataAgendamentoParaRepositorio = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (DateTimeParseException e) {
            // Se o controller não validar o formato da data, podemos lançar um erro aqui
            throw new RuntimeException("Formato de data inválido fornecido para consulta. Esperado: yyyy-MM-dd. Recebido: " + dataAgendamentoIso, e);
        }

        // Chama o método do repositório que busca pela combinação dos três campos
        List<Agendamento> agendamentosEncontrados = agendamentoRepository.findByFuncionarioIdAndDataAgendamentoAndHorario(
                funcionarioId,
                dataAgendamentoParaRepositorio, // Usa a data formatada para "dd/MM/yyyy"
                horario
        );

        // Mapeia para AgendamentoVmGeral e adiciona os serviços
        return agendamentosEncontrados.stream()
                .map(agendamento -> {
                    AgendamentoVmGeral vm = toVm(agendamento); // toVm deve retornar dataAgendamento como "yyyy-MM-dd"
                    List<ServicoVmGeral> servicos = agendamentoServicoService.listarPorAgendamento(agendamento.getId());
                    vm.setServicos(servicos); // Adiciona os serviços ao ViewModel
                    return vm;
                })
                .collect(Collectors.toList());
    }

    public AgendamentoVmGeral register(AgendamentoRegisterDto agendamentoRegisterDto) {
        Usuario usuario = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer usuarioId = usuario.getId();

        Funcionario funcionario = funcionarioRepository.findById(agendamentoRegisterDto.getFuncionarioId())
                .orElseThrow(() -> new RuntimeException("Funcionário não encontrado."));


        LocalTime horarioAgendamento = LocalTime.parse(agendamentoRegisterDto.getHorario());
        LocalTime horarioInicioFuncionario = LocalTime.parse(funcionario.getHorarioInicio());
        LocalTime horarioFinalFuncionario = LocalTime.parse(funcionario.getHorarioFinal());

        if (horarioAgendamento.isBefore(horarioInicioFuncionario) || horarioAgendamento.isAfter(horarioFinalFuncionario)) {
            throw new RuntimeException("Horário fora do expediente do funcionário.");
        }


        boolean existeConflito = agendamentoRepository.existsByFuncionarioIdAndDataAgendamentoAndHorario(
                agendamentoRegisterDto.getFuncionarioId(),
                agendamentoRegisterDto.getDataAgendamento(),
                agendamentoRegisterDto.getHorario()
        );

        if (existeConflito) {
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

        agendamentoServicoService.salvarServicos(agendamento.getId(), agendamentoRegisterDto.getServicoId());

        // 📤 Retorna o ViewModel do agendamento salvo
        return toVm(agendamento);
    }


    public AgendamentoVmGeral update(AgendamentoEditDto agendamentoEditDto) {
        String crc = "f" + agendamentoEditDto.getFuncionarioId() +
                "-h" + agendamentoEditDto.getHorario() +
                "-d" + agendamentoEditDto.getDataAgendamento();

        Agendamento agendamento = agendamentoRepository.findById(agendamentoEditDto.getId())
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado com o ID: " + agendamentoEditDto.getId()));

        agendamento.setFuncionarioId(agendamentoEditDto.getFuncionarioId());
        agendamento.setHorario(agendamentoEditDto.getHorario());
        agendamento.setDataAgendamento(agendamentoEditDto.getDataAgendamento());
        agendamento.setCrc(crc);

        return toVm(agendamentoRepository.save(agendamento));
    }

    public AgendamentoVmGeral updateStatusAgendamento(AgendamentoEditStatusDto agendamentoEditStatusDto) {
        Agendamento agendamento = agendamentoRepository.findById(agendamentoEditStatusDto.getId())
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado com o ID: " + agendamentoEditStatusDto.getId()));

        agendamento.setStatusAgendamento(agendamentoEditStatusDto.getStatusAgendamento());

        return toVm(agendamentoRepository.save(agendamento));
    }

    private AgendamentoVmGeral toVm(Agendamento agendamento) {
        Usuario usuario = usuarioRepository.findById(agendamento.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado")); // Ajustei a mensagem de erro aqui para ser mais específica

        String dataAgendamentoFormatadaParaApi;
        Object dataOriginalObj = agendamento.getDataAgendamento();

        if (dataOriginalObj instanceof LocalDate) {
            dataAgendamentoFormatadaParaApi = ((LocalDate) dataOriginalObj).toString(); // yyyy-MM-dd
        } else if (dataOriginalObj instanceof String) {
            String dataOriginalStr = (String) dataOriginalObj;
            if (dataOriginalStr.matches("^\\d{2}/\\d{2}/\\d{4}$")) { // Se for "dd/MM/yyyy"
                try {
                    DateTimeFormatter parserDdMmAaaa = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    LocalDate dataLocalDate = LocalDate.parse(dataOriginalStr, parserDdMmAaaa);
                    dataAgendamentoFormatadaParaApi = dataLocalDate.toString(); // Converte para "yyyy-MM-dd"
                } catch (DateTimeParseException e) {
                    System.err.println("AVISO: Erro ao parsear a data do agendamento '" + dataOriginalStr + "' no toVm. Retornando original. Agendamento ID: " + agendamento.getId() + ". Erro: " + e.getMessage());
                    dataAgendamentoFormatadaParaApi = dataOriginalStr;
                }
            } else {
                // Se já estiver em outro formato (idealmente yyyy-MM-dd) ou um formato inesperado
                dataAgendamentoFormatadaParaApi = dataOriginalStr;
            }
        } else {
            System.err.println("AVISO: Tipo de dataAgendamento inesperado ou nulo no toVm. Agendamento ID: " + agendamento.getId());
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
}
