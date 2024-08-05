package com.galileo.cu.servicioimportador.clients;

import com.galileo.cu.commons.models.Conexiones;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("servicio-conexiones")
public interface ConexionFeignClient {

    @GetMapping("/conexiones/search/findConexionesByIpServicio")
    Conexiones findConexionByIp(@RequestParam String ip_servicio);
}
