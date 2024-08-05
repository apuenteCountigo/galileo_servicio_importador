package com.galileo.cu.servicioimportador.servicios;

import com.galileo.cu.servicioimportador.entidades.ErroresImportador;
import com.galileo.cu.servicioimportador.repositorios.BalizaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;


@Service
public class BalizasServicioImpl implements BalizasServicio{

    private final BalizaRepository balizaRepository;

    @Autowired
    public BalizasServicioImpl(BalizaRepository balizaRepository) {
        this.balizaRepository = balizaRepository;
    }

    @Override
        public ResponseEntity<List<ErroresImportador>> cargarBalizasExcelRepo(MultipartFile file, String token) {

        ResponseEntity<List<ErroresImportador>> responseEntity = null;

        try {
            responseEntity = balizaRepository.cargarExcelBalizas(file.getInputStream(), token);
        } catch (IOException e) {
            return new ResponseEntity("ERROR IMPORTANDO EXCEL DE BALIZAS...." + e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return responseEntity;
    }
}
