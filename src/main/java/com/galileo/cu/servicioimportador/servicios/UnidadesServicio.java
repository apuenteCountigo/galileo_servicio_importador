package com.galileo.cu.servicioimportador.servicios;

import com.galileo.cu.servicioimportador.entidades.ErroresImportador;
import org.apache.catalina.LifecycleState;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


public interface UnidadesServicio {

    ResponseEntity<List<ErroresImportador>> cargarUnidadesExcelRepo(MultipartFile file, String token);
}
