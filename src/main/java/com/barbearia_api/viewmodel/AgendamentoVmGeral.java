package com.barbearia_api.viewmodel;

import com.barbearia_api.model.Agendamento.StatusAgendamento; // Corrigido para o caminho completo
import java.util.ArrayList; // Importação adicionada
import java.util.List;

public class AgendamentoVmGeral {

    private Integer id;
    private Integer funcionarioId;
    private Integer usuarioId;
    private String horario;
    private String dataAgendamento;
    private StatusAgendamento statusAgendamento;
    private String crc;

    private String nomeUsuario;
    private String emailUsuario;
    private String telefoneUsuario;

    // Melhoria: Inicializar a lista para evitar NullPointerException
    private List<ServicoVmGeral> servicos = new ArrayList<>();

    public AgendamentoVmGeral() {
        // O construtor padrão agora implicitamente tem 'servicos' inicializado como uma lista vazia.
    }

    public AgendamentoVmGeral(Integer id, Integer funcionarioId, Integer usuarioId, String horario, String dataAgendamento,
                              StatusAgendamento statusAgendamento, String crc,
                              String nomeUsuario, String emailUsuario, String telefoneUsuario) {
        this.id = id;
        this.funcionarioId = funcionarioId;
        this.usuarioId = usuarioId;
        this.horario = horario;
        this.dataAgendamento = dataAgendamento;
        this.statusAgendamento = statusAgendamento;
        this.crc = crc;
        this.nomeUsuario = nomeUsuario;
        this.emailUsuario = emailUsuario;
        this.telefoneUsuario = telefoneUsuario;
        // 'servicos' já está inicializado como new ArrayList<>() pela declaração do campo.
        // Se você quisesse permitir que este construtor também definisse os serviços,
        // você adicionaria 'List<ServicoVmGeral> servicos' como parâmetro e o atribuiria aqui.
    }

    // Getters e Setters (permanecem os mesmos)

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getFuncionarioId() { return funcionarioId; }
    public void setFuncionarioId(Integer funcionarioId) { this.funcionarioId = funcionarioId; }

    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }

    public String getHorario() { return horario; }
    public void setHorario(String horario) { this.horario = horario; }

    public String getDataAgendamento() { return dataAgendamento; }
    public void setDataAgendamento(String dataAgendamento) { this.dataAgendamento = dataAgendamento; }

    public StatusAgendamento getStatusAgendamento() { return statusAgendamento; }
    public void setStatusAgendamento(StatusAgendamento statusAgendamento) { this.statusAgendamento = statusAgendamento; }

    public String getCrc() { return crc; }
    public void setCrc(String crc) { this.crc = crc; }

    public String getNomeUsuario() { return nomeUsuario; }
    public void setNomeUsuario(String nomeUsuario) { this.nomeUsuario = nomeUsuario; }

    public String getEmailUsuario() { return emailUsuario; }
    public void setEmailUsuario(String emailUsuario) { this.emailUsuario = emailUsuario; }

    public String getTelefoneUsuario() { return telefoneUsuario; }
    public void setTelefoneUsuario(String telefoneUsuario) { this.telefoneUsuario = telefoneUsuario; }

    public List<ServicoVmGeral> getServicos() { return servicos; }
    public void setServicos(List<ServicoVmGeral> servicos) {
        // Você pode adicionar uma verificação aqui se quiser garantir que nunca seja setado para null
        // Ex: this.servicos = (servicos != null) ? servicos : new ArrayList<>();
        // Mas geralmente, se alguém está setando, espera-se que forneça uma lista válida ou uma nova lista vazia.
        this.servicos = servicos;
    }
}