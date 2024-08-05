package com.galileo.cu.servicioimportador.clients;

import com.galileo.cu.commons.models.Operaciones;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name="servicio-operaciones")
public interface OperacionFeignClient {

	@GetMapping(value = "/operaciones/search/findFirstByDescripcion")
	Operaciones operacionesFeign(@RequestParam("descripcion") String descripcion);
}
