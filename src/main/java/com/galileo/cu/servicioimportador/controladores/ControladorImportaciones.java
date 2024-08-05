package com.galileo.cu.servicioimportador.controladores;

import com.galileo.cu.servicioimportador.entidades.ErroresImportador;
import com.galileo.cu.servicioimportador.servicios.BalizasServicio;
import com.galileo.cu.servicioimportador.servicios.UnidadesServicio;
import com.galileo.cu.servicioimportador.servicios.UsuarioServicio;
import com.galileo.cu.servicioimportador.utilitarios.ExcelUtilitario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@CrossOrigin
public class ControladorImportaciones {

    private final UsuarioServicio usuarioServicio;
    private final UnidadesServicio unidadesServicio;
    private final BalizasServicio balizasServicio;

    @Autowired
    public ControladorImportaciones(UsuarioServicio usuarioServicio, UnidadesServicio unidadesServicio, BalizasServicio balizasServicio) {
        this.usuarioServicio = usuarioServicio;
        this.unidadesServicio = unidadesServicio;
        this.balizasServicio = balizasServicio;
    }

    //SALVAR ENTIDADES DESDE EXCEL
    @PostMapping("/importarExcel")
    public ResponseEntity<List<ErroresImportador>> importarExcel(@RequestParam("file") MultipartFile file, @RequestParam("destino") String destino, @RequestHeader("Authorization") String token) {

        ResponseEntity<List<ErroresImportador>> messageResponse = null;

        if (ExcelUtilitario.esUnExcel(file)) {

            if(destino.equals("usuarios")) {
                messageResponse = usuarioServicio.cargarUsuariosExcelRepo(file, token);
            }
            if(destino.equals("unidades")) {
                messageResponse = unidadesServicio.cargarUnidadesExcelRepo(file, token);
            }
            if(destino.equals("balizas")) {
                messageResponse = balizasServicio.cargarBalizasExcelRepo(file, token);
            }

        }
        return messageResponse;
    }
}

