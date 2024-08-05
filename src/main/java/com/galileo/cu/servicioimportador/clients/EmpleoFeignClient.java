package com.galileo.cu.servicioimportador.clients;

import com.galileo.cu.commons.models.Empleos;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name="servicio-empleos")
public interface EmpleoFeignClient {

	@GetMapping(value = "/empleos/search/findFirstByDescripcion")
	Empleos empleoFeign(@RequestParam("descripcion") String descripcion);
}
