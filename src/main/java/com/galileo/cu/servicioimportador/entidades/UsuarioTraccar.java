package com.galileo.cu.servicioimportador.entidades;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioTraccar {

   private Integer id;
   private String name;
   private String email;
   private String phone;
   private Boolean readonly;
   private Boolean administrator;
   private String map;
   private Integer latitude;
   private Integer longitude;
   private Integer zoom;
   private String password;
   private Boolean twelveHourFormat;
   private String coordinateFormat;
   private Boolean disabled;
   private Date expirationTime;
   private Integer deviceLimit;
   private Integer userLimit;
   private Boolean deviceReadonly;
   private Boolean limitCommands;
   private String poiLayer;
   private String token;
}
