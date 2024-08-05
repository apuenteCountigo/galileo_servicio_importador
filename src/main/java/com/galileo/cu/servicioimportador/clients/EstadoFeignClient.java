package com.galileo.cu.servicioimportador.clients;

import com.galileo.cu.commons.models.Estados;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name="servicio-estados")
public interface EstadoFeignClient {

	@GetMapping(value = "/estados/search/findFirstByDescripcion")
	Estados estadoFeign(@RequestParam("descripcion") String descripcion);
}
