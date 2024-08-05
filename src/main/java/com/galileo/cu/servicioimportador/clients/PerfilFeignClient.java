package com.galileo.cu.servicioimportador.clients;

import com.galileo.cu.commons.models.Perfiles;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(name="servicio-perfiles")
public interface PerfilFeignClient {

	@GetMapping(value = "/perfiles/search/findFirstByDescripcion")
	Perfiles perfilFeign(@RequestParam("descripcion") String descripcion);
}
