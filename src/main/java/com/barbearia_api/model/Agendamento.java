package com.barbearia_api.model;

import jakarta.persistence.*;

import java.sql.Date;
import java.sql.Time;

@Entity
@Table(name = "agendamentos")
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "funcionario_id")
    private Integer funcionarioId;

    @Column(name = "usuario_id")
    private Integer usuarioId;

    @Column(name = "horario")
    private String horario;

    @Column(name = "data_agendamento")
    private String dataAgendamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_agendamento", nullable = false)
    private StatusAgendamento statusAgendamento = StatusAgendamento.ESPERA;

    @Column(name = "crc")
    private String crc;

    public Agendamento() {}

    public Agendamento(Integer id, Integer funcionarioId, Integer usuarioId, String horario, String dataAgendamento, StatusAgendamento statusAgendamento, String crc) {
        this.id = id;
        this.funcionarioId = funcionarioId;
        this.usuarioId = usuarioId;
        this.horario = horario;
        this.dataAgendamento = dataAgendamento;
        this.statusAgendamento = statusAgendamento;
        this.crc = crc;
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getFuncionarioId() {
        return funcionarioId;
    }

    public void setFuncionarioId(Integer funcionarioId) {
        this.funcionarioId = funcionarioId;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    public String getDataAgendamento() {
        return dataAgendamento;
    }

    public void setDataAgendamento(String dataAgendamento) {
        this.dataAgendamento = dataAgendamento;
    }

    public StatusAgendamento getStatusAgendamento() {
        return statusAgendamento;
    }

    public void setStatusAgendamento(StatusAgendamento statusAgendamento) {
        this.statusAgendamento = statusAgendamento;
    }

    public String getCrc() {
        return crc;
    }

    public void setCrc(String crc) {
        this.crc = crc;
    }

    public enum StatusAgendamento {
        ATENDIDO, DESMARCADO, ESPERA
    }
}
