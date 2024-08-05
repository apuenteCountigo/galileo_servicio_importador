package com.galileo.cu.servicioimportador.servicios;

import com.galileo.cu.servicioimportador.entidades.ErroresImportador;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


public interface UsuarioServicio {

    ResponseEntity<List<ErroresImportador>> cargarUsuariosExcelRepo(MultipartFile file, String token);
}
