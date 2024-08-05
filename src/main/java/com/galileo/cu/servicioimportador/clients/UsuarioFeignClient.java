package com.galileo.cu.servicioimportador.clients;

import com.galileo.cu.commons.models.Usuarios;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name="servicio-usuarios")
public interface UsuarioFeignClient {

	@PostMapping("/usuarios/")
	void saveUsuariosExcel(@RequestBody Usuarios usuarios, @RequestHeader("Authorization") String header);

	@GetMapping("/usuarios/search/buscarTip")
	Usuarios findByTip(@RequestParam String tip,  @RequestHeader("Authorization") String header);

	@PutMapping("/usuarios/{id}")
	void updateUsuario(@PathVariable Long id, @RequestBody Usuarios usuario, @RequestHeader("Authorization") String header);

}
