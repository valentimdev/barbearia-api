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

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AgendamentoService {
    private final AgendamentoRepository agendamentoRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final UsuarioRepository usuarioRepository; // Adicionado ao construtor
    private final AgendamentoServicoService agendamentoServicoService;


    // Construtor atualizado para incluir UsuarioRepository se não estava antes,
    // ou mantenha o seu construtor original se já o injetava corretamente.
    @Autowired
    public AgendamentoService(
            AgendamentoRepository agendamentoRepository,
            FuncionarioRepository funcionarioRepository,
            UsuarioRepository usuarioRepository, // Garanta que está sendo injetado
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
                .orElse(null); // Considere lançar uma exceção se não encontrado
    }

    public List<AgendamentoVmGeral> listByUsuarioId(Integer id) {
        return agendamentoRepository.findByUsuarioId(id).stream()
                .map(agendamento -> {
                    AgendamentoVmGeral vm = toVm(agendamento);
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

    public List<AgendamentoVmGeral> listByHorarioAndData(String horario, String dataAgendamento) {
        return agendamentoRepository.findByHorarioAndDataAgendamento(horario, dataAgendamento).stream()
                .map(this::toVm)
                .collect(Collectors.toList());
    }

    public AgendamentoVmGeral register(AgendamentoRegisterDto agendamentoRegisterDto) {
        // Obtém o usuário logado
        Usuario usuarioAuth = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // É uma boa prática buscar o usuário do repositório para garantir que é uma entidade gerenciada
        // e atualizada, especialmente se o objeto 'principal' for uma projeção ou DTO.
        // Se 'usuarioAuth' já é a entidade completa e gerenciada, pode usar usuarioAuth.getId() diretamente.
        Usuario usuario = usuarioRepository.findById(usuarioAuth.getId())
                .orElseThrow(() -> new RuntimeException("Usuário autenticado não encontrado no banco de dados."));
        Integer usuarioId = usuario.getId();


        Funcionario funcionario = funcionarioRepository.findById(agendamentoRegisterDto.getFuncionarioId())
                .orElseThrow(() -> new RuntimeException("Funcionário não encontrado."));


        LocalTime horarioAgendamento = LocalTime.parse(agendamentoRegisterDto.getHorario());
        LocalTime horarioInicioFuncionario = LocalTime.parse(funcionario.getHorarioInicio());
        LocalTime horarioFinalFuncionario = LocalTime.parse(funcionario.getHorarioFinal());

        if (horarioAgendamento.isBefore(horarioInicioFuncionario) || horarioAgendamento.isAfter(horarioFinalFuncionario)) {
            throw new RuntimeException("Horário fora do expediente do funcionário.");
        }

        // --- VERIFICAÇÃO DE CONFLITO ATUALIZADA ---
        boolean existeConflito = agendamentoRepository.existsActiveByFuncionarioIdAndDataAgendamentoAndHorario(
                agendamentoRegisterDto.getFuncionarioId(),
                agendamentoRegisterDto.getDataAgendamento(),
                agendamentoRegisterDto.getHorario()
        );

        if (existeConflito) {
            // Mensagem pode ser mais específica ou mantida genérica para o frontend tratar
            throw new RuntimeException("Horário indisponível. Já existe um agendamento ativo neste horário para o funcionário.");
        }

        // Geração do CRC
        String crc = "f" + agendamentoRegisterDto.getFuncionarioId() +
                "-h" + agendamentoRegisterDto.getHorario() +
                "-d" + agendamentoRegisterDto.getDataAgendamento();

        Agendamento agendamento = new Agendamento(
                null, // ID será gerado automaticamente
                agendamentoRegisterDto.getFuncionarioId(),
                usuarioId,
                agendamentoRegisterDto.getHorario(),
                agendamentoRegisterDto.getDataAgendamento(),
                Agendamento.StatusAgendamento.ESPERA, // Status padrão para novo agendamento
                crc
        );

        agendamento = agendamentoRepository.save(agendamento);

        return toVm(agendamento);
    }


    public AgendamentoVmGeral update(AgendamentoEditDto agendamentoEditDto) {
        // Validações adicionais podem ser necessárias aqui, como verificar se o novo horário não conflita.
        String crc = "f" + agendamentoEditDto.getFuncionarioId() +
                "-h" + agendamentoEditDto.getHorario() +
                "-d" + agendamentoEditDto.getDataAgendamento();

        Agendamento agendamento = agendamentoRepository.findById(agendamentoEditDto.getId())
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado com o ID: " + agendamentoEditDto.getId()));

        // Adicionar lógica para verificar conflito se o horário/data/funcionario mudar.
        // Se (houve mudança em funcionário, data ou horário) E (novo slot está ocupado por OUTRO agendamento)
        // Então lançar exceção.

        agendamento.setFuncionarioId(agendamentoEditDto.getFuncionarioId());
        agendamento.setHorario(agendamentoEditDto.getHorario());
        agendamento.setDataAgendamento(agendamentoEditDto.getDataAgendamento());
        agendamento.setCrc(crc); // CRC deve ser atualizado se os componentes mudarem

        return toVm(agendamentoRepository.save(agendamento));
    }

    public AgendamentoVmGeral updateStatusAgendamento(AgendamentoEditStatusDto agendamentoEditStatusDto) {
        Agendamento agendamento = agendamentoRepository.findById(agendamentoEditStatusDto.getId())
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado com o ID: " + agendamentoEditStatusDto.getId()));

        agendamento.setStatusAgendamento(agendamentoEditStatusDto.getStatusAgendamento());

        return toVm(agendamentoRepository.save(agendamento));
    }

    private AgendamentoVmGeral toVm(Agendamento agendamento) {
        // A injeção de UsuarioRepository no construtor é importante para esta linha.
        Usuario usuario = usuarioRepository.findById(agendamento.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuário associado ao agendamento não encontrado: ID " + agendamento.getUsuarioId()));

        Funcionario funcionario = funcionarioRepository.findById(agendamento.getFuncionarioId())
                .orElseThrow(() -> new RuntimeException("Funcionário associado ao agendamento não encontrado: ID " + agendamento.getFuncionarioId()));

        // Você pode querer adicionar o nome do funcionário ao ViewModel também
        // String nomeFuncionario = funcionario.getUsuario().getNome(); // Supondo que Funcionario tem relação com Usuario

        AgendamentoVmGeral vm = new AgendamentoVmGeral(
                agendamento.getId(),
                agendamento.getFuncionarioId(),
                agendamento.getUsuarioId(),
                agendamento.getHorario(),
                agendamento.getDataAgendamento(),
                agendamento.getStatusAgendamento(),
                agendamento.getCrc(),
                usuario.getNome(),    // Nome do cliente
                usuario.getEmail(),   // Email do cliente
                usuario.getTelefone() // Telefone do cliente
        );

        // Adicionar serviços ao ViewModel, se necessário aqui também, ou garantir que
        // os métodos de listagem que precisam deles os preencham.
        // List<ServicoVmGeral> servicos = agendamentoServicoService.listarPorAgendamento(agendamento.getId());
        // vm.setServicos(servicos);

        return vm;
    }
}