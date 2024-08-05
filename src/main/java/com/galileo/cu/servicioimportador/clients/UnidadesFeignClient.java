package com.galileo.cu.servicioimportador.clients;

import com.galileo.cu.commons.models.Unidades;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name="servicio-unidades")
public interface UnidadesFeignClient {

	@PostMapping("/unidades/")
	void saveUnidadesExcel(@RequestBody Unidades unidades, @RequestHeader("Authorization") String header);

	@GetMapping(value = "/unidades/search/findFirstByDenominacion")
	Unidades findUnidadByDescripcion(@RequestParam("descripcion") String descripcion,  @RequestHeader("Authorization") String header);
}
