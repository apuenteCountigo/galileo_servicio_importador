package com.galileo.cu.servicioimportador.repositorios;

import com.galileo.cu.commons.models.*;
import com.galileo.cu.servicioimportador.clients.*;
import com.galileo.cu.servicioimportador.entidades.DecodificarToken;
import com.galileo.cu.servicioimportador.entidades.ErroresImportador;
import com.galileo.cu.servicioimportador.entidades.UsuarioTraccar;
import feign.FeignException;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Repository
@Log4j2
public class UsuarioRepository {

    private final EmpleoFeignClient empleoFeignClient;
    private final EstadoFeignClient estadoFeignClient;
    private final PerfilFeignClient perfilFeignClient;
    private final UsuarioFeignClient usuarioFeignClient;
    private final ApisFeignClient apisFeignClient;
    private final UnidadesFeignClient unidadesFeignClient;
    private final UnidadesUsuarRepository unidadesUsuarRepository;

    @Autowired
    public UsuarioRepository(EmpleoFeignClient empleoFeignClient, EstadoFeignClient estadoFeignClient, PerfilFeignClient perfilFeignClient, UsuarioFeignClient usuarioFeignClient, ApisFeignClient apisFeignClient, UnidadesFeignClient unidadesFeignClient, UnidadesUsuarRepository unidadesUsuarRepository) {
        this.empleoFeignClient = empleoFeignClient;
        this.estadoFeignClient = estadoFeignClient;
        this.perfilFeignClient = perfilFeignClient;
        this.usuarioFeignClient = usuarioFeignClient;
        this.apisFeignClient = apisFeignClient;
        this.unidadesFeignClient = unidadesFeignClient;
        this.unidadesUsuarRepository = unidadesUsuarRepository;
    }

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);



    public ResponseEntity<List<ErroresImportador>> cargarExcelUsuarios(InputStream is, String token) {

        DecodificarToken decodificarToken = null;
        try {
            decodificarToken =  DecodificarToken.decodificarToken(token);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        BufferedInputStream buffStream = new BufferedInputStream(is);
        int importacionesCorrectas = 0;
        int importacionesIncorrectas = 0;
        List<ErroresImportador> resultadoImportacion = new ArrayList<>();

        Usuarios usuario = null;
        String tip = null;

        try {
            Workbook workbook = WorkbookFactory.create(buffStream);

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();

                Iterator<Cell> cellsInRow = currentRow.iterator();


                if (rowNumber == 0) {
                    if (cellsInRow.next().getStringCellValue().contains("TIP")){
                        rowNumber++;
                        continue;
                    }else {
                        return new ResponseEntity("Introduzca un excel de usuarios válido para importar....", HttpStatus.BAD_REQUEST);
                    }
                }
                LocalDateTime fechaExpiracion = null;
                SALTO:
                try {
                    while (cellsInRow.hasNext()) {
                        Perfiles perfil = new Perfiles();
                        Empleos empleo = new Empleos();
                        Estados estado = new Estados();
                        Unidades unidad = new Unidades();
                      

                        Cell currentCell = cellsInRow.next();
                        int columnIndex = currentCell.getColumnIndex();
                        switch (columnIndex) {
                            case 0 -> {
                                tip = currentCell.getStringCellValue();

                                try {
                                    usuario = usuarioFeignClient.findByTip(tip, "Bearer " + token);
                                    if (usuario != null){
                                       ++importacionesIncorrectas;
                                        resultadoImportacion.add(new ErroresImportador("Error al importar parámetro TIP.", "  El TIP ya esta en uso con valor: " + currentCell, importacionesCorrectas, importacionesIncorrectas));
                                        break SALTO;
                                    }
                                } catch (Exception exception) {
                                    log.trace("NO EXISTE EL USUARIO ", exception);
                                    usuario = null;
                                }

                                if (usuario == null) {
                                    usuario = new Usuarios();
                                    usuario.setTip(tip);
                                    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
                                    usuario.setPassword(passwordEncoder.encode(tip));
                                }
                            }
                            case 1 -> {
                                if (tip == null) {
                                    ++importacionesIncorrectas;
                                    resultadoImportacion.add(new ErroresImportador("No se importará el registro:", "  El TIP del usuario, es de llenado obligatorio.", importacionesCorrectas, importacionesIncorrectas));
                                    break SALTO;
                                }
                                if (tip.length() > 7){
                                    ++importacionesIncorrectas;
                                    resultadoImportacion.add(new ErroresImportador("No se importará el registro:", "  El TIP del usuario, debe ser de 7 carácteres.", importacionesCorrectas, importacionesIncorrectas));
                                    break SALTO;
                                }
                                String nombre_usuario = currentCell.getStringCellValue();

                                Pattern patternNombre = Pattern.compile("[a-zA-Z]");
                                Matcher matcherNombre = patternNombre.matcher(nombre_usuario);

                                if (!matcherNombre.find()) {
                                    ++importacionesIncorrectas;
                                    resultadoImportacion.add(new ErroresImportador("No se importará el registro, el nombre debe contener solo letras.", "El nombre de usuario sólo debe contener letras, valor incorrecto:  " + currentCell, importacionesCorrectas, importacionesIncorrectas));
                                    break SALTO;
                                }
                                usuario.setNombre(nombre_usuario);
                            }
                            case 2 -> {
                                String apellidos_usuario = currentCell.getStringCellValue();

                                Pattern patternApe = Pattern.compile("[a-zA-Z]");
                                Matcher matcherApe = patternApe.matcher(apellidos_usuario);

                                if (!matcherApe.find()) {
                                    ++importacionesIncorrectas;
                                    resultadoImportacion.add(new ErroresImportador("No se importará el registro, los apellidos deben contener solo letras.", "El apellido de usuario sólo debe contener letras, valor incorrecto:  " + currentCell, importacionesCorrectas, importacionesIncorrectas));
                                    break SALTO;
                                }
                                usuario.setApellidos(apellidos_usuario);
                            }
                            case 3 -> {
                                String numero_telefono = currentCell.getStringCellValue();
                                Pattern pattern = Pattern.compile("^\\d{8,11}$");
                                Matcher matcherTelefono = pattern.matcher(numero_telefono);

                                if (!matcherTelefono.find()) {
                                    ++importacionesIncorrectas;
                                    resultadoImportacion.add(new ErroresImportador("No se importará el registro.", "Introduzca un teléfono valido, valor incorrecto: " + currentCell, importacionesCorrectas, importacionesIncorrectas));
                                    break SALTO;
                                }
                                usuario.setContacto(numero_telefono);
                            }
                            case 4 -> {
                                String email = currentCell.getStringCellValue();
                                Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(email);
                                if (!matcher.find()) {
                                    ++importacionesIncorrectas;
                                    resultadoImportacion.add(new ErroresImportador("No se importará el registro.", "Valor de email incorrecto introducido: " + currentCell, importacionesCorrectas, importacionesIncorrectas));
                                    break SALTO;
                                }
                                List<UsuarioTraccar> usuariosTraccarList = new ArrayList<>();
                                try {
                                    usuariosTraccarList = apisFeignClient.listarUsuariosTraccar();
                                }catch (Exception exception){
                                    if(exception.getMessage().contains("Error listando usuarios TRACCAR")){
                                       return new ResponseEntity("Error importando excel de usuarios, al parecer no hay conexión con TRACCAR.....", HttpStatus.BAD_REQUEST);
                                    }
                                }

                                for (UsuarioTraccar usuarioTraccar : usuariosTraccarList) {
                                    if (usuarioTraccar.getEmail().equals(email)) {
                                        ++importacionesIncorrectas;
                                        resultadoImportacion.add(new ErroresImportador("No se importará el registro.","Ya existe un usuario en traccar con el correo electrónico introducido con valor:  " + currentCell, importacionesCorrectas, importacionesIncorrectas));
                                        break SALTO;
                                    }
                                }
                                usuario.setEmail(email);
                            }
                            case 5 -> usuario.setObservaciones(currentCell.getStringCellValue());
                            case 6 -> {
                                String perfil_entrada = currentCell.getStringCellValue();
                                if (perfil_entrada.equals("")){
                                    ++importacionesIncorrectas;
                                    resultadoImportacion.add(new ErroresImportador("No se importará el registro.","El usuario con TIP: "+usuario.getTip() + " debe tener perfil.", importacionesCorrectas, importacionesIncorrectas));
                                    break SALTO;
                                }
                                perfil = perfilFeignClient.perfilFeign(perfil_entrada);
                                usuario.setPerfil(perfil);
                            }
                            case 7 -> {

                                empleo = empleoFeignClient.empleoFeign(currentCell.getStringCellValue());
                                usuario.setEmpleos(empleo);
                            }
                            case 8 -> {
                               String estado_entrada = currentCell.getStringCellValue();
                                if (usuario.getPerfil().getDescripcion().equals("Super Administrador") && !estado_entrada.equals("ACTIVO")){
                                    ++importacionesIncorrectas;
                                    resultadoImportacion.add(new ErroresImportador("No se importará el registro.","El usuario con TIP: "+usuario.getTip() + " es un super administrador y no puede ser invitado externo.", importacionesCorrectas, importacionesIncorrectas));
                                    break SALTO;
                                }
                                estado = estadoFeignClient.estadoFeign(estado_entrada);
                                usuario.setEstados(estado);
                            }
                            case 9 -> {
                                String unidad_entrada = null;

                                try {
                                    unidad_entrada = currentCell.getStringCellValue();
                                    if (usuario.getPerfil().getDescripcion().equals("Usuario Final") && unidad_entrada.equals("")){

                                        Usuarios usuarioImportador = usuarioFeignClient.findByTip(decodificarToken.getTip(), "Bearer " + token);
                                        if(usuarioImportador.getUnidad() != null){
                                            usuario.setUnidad(usuarioImportador.getUnidad());
                                        }else {
                                            ++importacionesIncorrectas;
                                            resultadoImportacion.add(new ErroresImportador("No se importará el registro.", "El usuario con TIP: " + usuario.getTip() + " es un usuario final y debe tener una unidad para poder importar.", importacionesCorrectas, importacionesIncorrectas));
                                            break SALTO;
                                        }
                                    }
                                    if (usuario.getEstados().getDescripcion().equals("INVITADO") && unidad_entrada.equals("")){
                                        ++importacionesIncorrectas;
                                        resultadoImportacion.add(new ErroresImportador("No se importará el registro.","El usuario con TIP: "+usuario.getTip() + " es un usuario invitado y debe tener una unidad para poder importar.", importacionesCorrectas, importacionesIncorrectas));
                                        break SALTO;
                                    }
                                    if (!unidad_entrada.equals("")){
                                        try {
                                            unidad = unidadesFeignClient.findUnidadByDescripcion(unidad_entrada, token);
                                        }catch (FeignException.FeignClientException feignClientException){
                                            throw new RuntimeException("Error conectando con el servicio de unidades");
                                        }
                                        usuario.setUnidad(unidad);
                                    }else usuario.setUnidad(null);
                                } catch (Exception exception) {
                                    log.error("Error encontrando unidad en importadores: " + exception.getMessage());
                                    ++importacionesIncorrectas;
                                    resultadoImportacion.add(new ErroresImportador("No se importará el registro de la fila", "No se encuentra la unidad reflejada en el excel:  " + rowNumber, importacionesCorrectas, importacionesIncorrectas));
                                    break SALTO;
                                }
                            }
                            case 10 -> fechaExpiracion = currentCell.getLocalDateTimeCellValue();
                            default -> {
                            }
                        }

                    }
                    if (usuario.getEstados().getDescripcion().equals("INVITADO") && fechaExpiracion == null){
                        ++importacionesIncorrectas;
                        resultadoImportacion.add(new ErroresImportador("No se importará el registro.","El usuario con TIP: "+usuario.getTip() + " es un usuario invitado externo y debe tener una fecha de expiración.", importacionesCorrectas, importacionesIncorrectas));
                        break SALTO;
                    }

                    if (usuario.getTip() == null ||
                            usuario.getNombre() == null ||
                            usuario.getApellidos() == null ||
                            usuario.getContacto() == null ||
                            usuario.getEmail() == null ||
                            usuario.getPerfil() == null ||
                            usuario.getEmpleos() == null ||
                            usuario.getEstados() == null) {
                        ++importacionesIncorrectas;
                        resultadoImportacion.add(new ErroresImportador("Datos incompletos", "Debe completar los datos de llenado obligatorios del excel marcados con * en el registro.", importacionesCorrectas, importacionesIncorrectas));
                        break SALTO;
                    }

                    if (usuario.getPerfil().getId() == 3 && usuario.getUnidad() == null){
                        ++importacionesIncorrectas;
                        resultadoImportacion.add(new ErroresImportador("No se importará el registro.","El usuario con TIP: "+usuario.getTip() + " es un usuario final y debe tener una unidad para poder importar.", importacionesCorrectas, importacionesIncorrectas));
                        break SALTO;
                    }
                    ++importacionesCorrectas;
                    usuario.setCertificado("cert");
                    usuarioFeignClient.saveUsuariosExcel(usuario, token);
                    if (usuario.getUnidad() != null){
                        Usuarios usuario_update = usuarioFeignClient.findByTip(usuario.getTip(), token);
                        UnidadesUsuarios unidadesUsuarios = new UnidadesUsuarios();
                        unidadesUsuarios.setUnidad(usuario.getUnidad());
                        unidadesUsuarios.setUsuario(usuario_update);
                        unidadesUsuarios.setExpira(fechaExpiracion);
                        unidadesUsuarios.setEstado(usuario.getEstados());
                        unidadesUsuarRepository.save(unidadesUsuarios);
                    }

                    usuario = new Usuarios();


                }catch (Exception exception){
                    log.error(exception.getMessage());
                    throw new RuntimeException("Error importando el excel de usuarios");
                }
            }
            workbook.close();

        } catch (IOException e) {
            log.error("Error importando archivo excel de Usuarios.. "+e.getMessage());
            throw new RuntimeException("Error importando archivo: " + e.getMessage());
        }catch (Exception exception){
            log.error("Error importando Usuarios desde el excel.. "+exception.getMessage());
            return new ResponseEntity("Error importando excel de usuarios.....", HttpStatus.BAD_REQUEST);
        }

        if (importacionesIncorrectas > 0){
            resultadoImportacion.add(new ErroresImportador("","",importacionesCorrectas, importacionesIncorrectas));
            return ResponseEntity.badRequest().body(resultadoImportacion);
        }
        return new ResponseEntity("Excel importado correctamente.....", HttpStatus.OK);
    }

}

