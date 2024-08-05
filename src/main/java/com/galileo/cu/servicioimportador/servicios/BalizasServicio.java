package com.galileo.cu.servicioimportador.servicios;

import com.galileo.cu.servicioimportador.entidades.ErroresImportador;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Service
public interface BalizasServicio {
    ResponseEntity<List<ErroresImportador>> cargarBalizasExcelRepo(MultipartFile file, String token);
}
