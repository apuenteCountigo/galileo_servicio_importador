package com.galileo.cu.servicioimportador.servicios;

import com.galileo.cu.servicioimportador.entidades.ErroresImportador;
import com.galileo.cu.servicioimportador.repositorios.UsuarioRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Log4j2
@Service
public class UsuarioServicioImpl implements UsuarioServicio{

    @Autowired
    private UsuarioRepository usuarioRepositorySave;

    @Override
    public ResponseEntity<List<ErroresImportador>> cargarUsuariosExcelRepo(MultipartFile file, String token) {
        ResponseEntity<List<ErroresImportador>> responseEntity = null;

        try {
            responseEntity = usuarioRepositorySave.cargarExcelUsuarios(file.getInputStream(), token);
        } catch (IOException e) {
            return new  ResponseEntity("ERROR IMPORTANDO EXCEL DE USUARIO...."+e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return responseEntity;
    }}
