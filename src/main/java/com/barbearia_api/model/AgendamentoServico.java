package com.barbearia_api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "agendamento_servico")
public class AgendamentoServico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "agendamento_id", nullable = false)
    private Integer agendamentoId;

    @Column(name = "servico_id", nullable = false)
    private Integer servicoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servico_id", insertable = false, updatable = false)
    private Servico servico;

    // Getters e Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAgendamentoId() {
        return agendamentoId;
    }

    public void setAgendamentoId(Integer agendamentoId) {
        this.agendamentoId = agendamentoId;
    }

    public Integer getServicoId() {
        return servicoId;
    }

    public void setServicoId(Integer servicoId) {
        this.servicoId = servicoId;
    }

    public Servico getServico() {
        return servico;
    }

    public void setServico(Servico servico) {
        this.servico = servico;
    }

    public String getDescricao() {
        return servico != null ? servico.getDescricao() : null;
    }

}
