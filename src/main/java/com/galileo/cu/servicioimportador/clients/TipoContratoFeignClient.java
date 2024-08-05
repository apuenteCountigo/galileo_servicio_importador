package com.galileo.cu.servicioimportador.clients;

import com.galileo.cu.commons.models.TipoContrato;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name="servicio-tipocontratos")
public interface TipoContratoFeignClient {

	@GetMapping(value = "/tipocontratos/search/findFirstByDescripcion")
	TipoContrato tipocontratoFeign(@RequestParam("descripcion") String descripcion);
}
