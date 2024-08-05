package com.galileo.cu.servicioimportador.clients;

import com.galileo.cu.commons.models.TipoBaliza;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name="servicio-tipobalizas")
public interface TipoBalizaFeignClient {

	@GetMapping(value = "/tipobalizas/search/findFirstByDescripcion")
	TipoBaliza tipoBalizaFeign(@RequestParam("descripcion") String descripcion);
}
