package com.galileo.cu.servicioimportador.clients;

import com.galileo.cu.commons.models.Balizas;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name="servicio-balizas")
public interface BalizaFeignClient {

	@PostMapping("/balizas")
	void saveBalizasExcel(@RequestBody Balizas balizas,  @RequestHeader("Authorization") String header);

	@GetMapping("/balizas/search/findFirstByClave")
	Balizas findFirstByClave(@RequestParam("nombre_baliza") String nombre_baliza,  @RequestHeader("Authorization") String header);
}
