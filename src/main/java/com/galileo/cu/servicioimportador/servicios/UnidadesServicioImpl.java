package com.galileo.cu.servicioimportador.servicios;

import com.galileo.cu.servicioimportador.entidades.ErroresImportador;
import com.galileo.cu.servicioimportador.repositorios.UnidadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;


@Service
public class UnidadesServicioImpl implements UnidadesServicio{

    @Autowired
    private UnidadRepository unidadRepository;


    @Override
    public ResponseEntity<List<ErroresImportador>> cargarUnidadesExcelRepo(MultipartFile file, String token) {
        ResponseEntity<List<ErroresImportador>> responseEntity = null;

        try {
            responseEntity = unidadRepository.cargarExcelUnidades(file.getInputStream(), token);
        } catch (IOException e) {
            return new  ResponseEntity("ERROR IMPORTANDO EXCEL DE UNIDADES...."+e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return responseEntity;
    }
}
