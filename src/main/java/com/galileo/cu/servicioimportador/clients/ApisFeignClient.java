package com.galileo.cu.servicioimportador.clients;

import com.galileo.cu.servicioimportador.entidades.LicenciaDataMiner;
import com.galileo.cu.servicioimportador.entidades.UsuarioTraccar;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.List;

@FeignClient(name="servicio-apis")
public interface ApisFeignClient {

	@GetMapping("/listarUsuariosTraccar")
	List<UsuarioTraccar> listarUsuariosTraccar();

	@PostMapping("/obtenerLimiteElementosDataMiner")
	ResponseEntity<ArrayList<LicenciaDataMiner>> obtenerLimiteElementosDataMiner();
}
