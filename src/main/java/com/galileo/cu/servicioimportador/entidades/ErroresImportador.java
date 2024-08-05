package com.galileo.cu.servicioimportador.entidades;

import lombok.*;


@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class ErroresImportador {

    private String error;
    private String mensaje;
    private Integer importacionesCorrectas;
    private Integer importacionesIncorrectas;
}
