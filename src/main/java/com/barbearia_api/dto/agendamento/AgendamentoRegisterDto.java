package com.barbearia_api.dto.agendamento;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class AgendamentoRegisterDto {

    @NotNull(message = "Campo invalido.")
    private Integer funcionarioId;

    @Pattern(regexp = "^\\d{2}:\\d{2}$", message = "Formato de horário inválido. Use HH:mm")
    private String horario;

    @Pattern(regexp = "^\\d{2}/\\d{2}/\\d{4}$", message = "Formato de horário inválido. Use dd/mm/aaaa")
    private String dataAgendamento;

    public  AgendamentoRegisterDto() {}

    public AgendamentoRegisterDto(Integer funcionarioId, String horario, String dataAgendamento) {
        this.funcionarioId = funcionarioId;
        this.horario = horario;
        this.dataAgendamento = dataAgendamento;
    }

    public Integer getFuncionarioId() {
        return funcionarioId;
    }

    public void setFuncionarioId(Integer funcionarioId) {
        this.funcionarioId = funcionarioId;
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

}
