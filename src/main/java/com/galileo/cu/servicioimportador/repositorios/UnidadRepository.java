package com.galileo.cu.servicioimportador.repositorios;

import com.galileo.cu.commons.models.*;
import com.galileo.cu.servicioimportador.clients.UnidadesFeignClient;
import com.galileo.cu.servicioimportador.entidades.ErroresImportador;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j2
@Repository
public class UnidadRepository {

    private final UnidadesFeignClient unidadesFeignClient;
    private final ProvinciaRepository provinciaRepository;

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,6}$");


    @Autowired
    public UnidadRepository(UnidadesFeignClient unidadesFeignClient, ProvinciaRepository provinciaRepository) {
        this.provinciaRepository = provinciaRepository;
        this.unidadesFeignClient = unidadesFeignClient;
    }


    public ResponseEntity<List<ErroresImportador>> cargarExcelUnidades(InputStream is, String token) {
        BufferedInputStream buffStream = new BufferedInputStream(is);

        int importacionesCorrectas = 0;
        int importacionesIncorrectas = 0;
        ArrayList<ErroresImportador> resultadoImportacion = new ArrayList<>();


        try {

            Workbook workbook = WorkbookFactory.create(buffStream);

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            int rowNumber = 0;
            while (rows.hasNext()) {

                Row currentRow = rows.next();

                Iterator<Cell> cellsInRow = currentRow.iterator();
                Provincias provincia = new Provincias();

                if (rowNumber == 0) {
                    if (cellsInRow.next().getStringCellValue().contains("DENOMICACION")){
                        rowNumber++;
                        continue;
                    }else {
                        return new ResponseEntity("Introduzca un excel de unidades válido para importar....", HttpStatus.BAD_REQUEST);
                    }
                }

                SALTO:
                try {

                    Unidades unidad = new Unidades();

                    boolean allCellsEmpty = true;
                    for (Cell cell : currentRow) {
                        if (cell.getCellType() != CellType.BLANK) {
                            allCellsEmpty = false;
                            break;
                        }
                    }

                    if (allCellsEmpty) {
                        break SALTO;
                    }

                    while (cellsInRow.hasNext()) {

                        Cell currentCell = cellsInRow.next();

                        int columnIndex = currentCell.getColumnIndex();
                        switch (columnIndex) {
                            case 0 -> {
                                String nombre_unidad = currentCell.getStringCellValue();

                                try {
                                    unidad = unidadesFeignClient.findUnidadByDescripcion(nombre_unidad, "Bearer " + token);
                                    if (nombre_unidad.equals(unidad.getDenominacion())) {
                                        importacionesIncorrectas++;
                                        resultadoImportacion.add(new ErroresImportador("Error con el nombre de la unidad.", "  Ya existe una unidad con esta descripción registrada:" + currentCell, importacionesCorrectas, importacionesIncorrectas));
                                        break SALTO;
                                    }
                                } catch (Exception exception) {
                                    log.info("NO EXISTE UNIDAD CON ESE NOMBRE");
                                }

                                unidad.setDenominacion(nombre_unidad);
                            }
                            case 1 -> unidad.setResponsable(currentCell.getStringCellValue());
                            case 2 -> {
                                String numero_telefono = currentCell.getStringCellValue();

                                if (!numero_telefono.trim().isEmpty()){
                                    Pattern pattern = Pattern.compile("^\\d{8,11}$");
                                    Matcher matcherTelefono = pattern.matcher(numero_telefono);

                                    if (!matcherTelefono.find()) {
                                        importacionesIncorrectas++;
                                        resultadoImportacion.add(new ErroresImportador("Teléfono no válido", "  El número de teléfono introducido no es valido:" + currentCell, importacionesCorrectas, importacionesIncorrectas));
                                        break SALTO;
                                    }
                                }

                                unidad.setTelefono(numero_telefono);
                            }
                            case 3 -> unidad.setGroupWise(currentCell.getStringCellValue());
                            case 4 -> {

                                String email = currentCell.getStringCellValue();

                                if (!email.trim().isEmpty()){
                                    Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(email);
                                    if (!matcher.find()){
                                        importacionesIncorrectas++;
                                        resultadoImportacion.add(new ErroresImportador("Formato de correo electrónico no válido", "Introdusca un correo válido:" + currentCell, importacionesCorrectas, importacionesIncorrectas));
                                        break SALTO;
                                    }
                                }

                                unidad.setEmail(email);
                            }
                            case 5 -> unidad.setDireccion(currentCell.getStringCellValue());

                            case 6 -> {
                                String codigoPostal = currentCell.getStringCellValue();
                                Pattern pattern = Pattern.compile("^\\d{5}$");
                                Matcher matcherCodigo = pattern.matcher(codigoPostal);
                                if (!codigoPostal.equals("")){
                                    if (!matcherCodigo.find()) {
                                        importacionesIncorrectas++;
                                        resultadoImportacion.add(new ErroresImportador("Error en el código postal", " Introduzca un código postal con formato correcto de 5 cifras: " + currentCell, importacionesCorrectas, importacionesIncorrectas));
                                        break SALTO;
                                    }
                            }
                                unidad.setCodigoPostal(codigoPostal);
                            }

                            case 7 -> unidad.setLocalidad(currentCell.getStringCellValue());
                            case 8 -> unidad.setNotas(currentCell.getStringCellValue());
                            case 9 -> {
                                String provincia_entrada = currentCell.getStringCellValue();
                                provincia = provinciaRepository.findProvinciasByDescripcion(provincia_entrada);

                                if (provincia !=null) unidad.setProvincia(provincia);
                            }

                            default -> {
                            }
                        }
                    }

                    if(unidad.getDenominacion() == null
                            || unidad.getDenominacion().equals("")
                            || unidad.getResponsable() == null
                            || unidad.getResponsable().equals("")
                            || unidad.getTelefono() == null
                            || unidad.getTelefono().equals("")){
                        importacionesIncorrectas++;
                        resultadoImportacion.add(new ErroresImportador("Datos incompletos", "Debe completar los datos de llenado obligatorios del excel marcados con * en el registro en la unidad: "+unidad.getDenominacion()+".", importacionesCorrectas, importacionesIncorrectas));
                        break SALTO;
                    }

                    unidadesFeignClient.saveUnidadesExcel(unidad, "Bearer " + token);
                    ++importacionesCorrectas;


                }catch (Exception exception) {
                    log.error(exception.getMessage());
                    return new ResponseEntity("Error importando excel de unidades.....", HttpStatus.BAD_REQUEST);
                }
            }
            workbook.close();

        } catch (IOException e) {
            log.error("Error importando unidades desde el excel.. "+e.getMessage());
            return new ResponseEntity("Error importando excel de unidades.....", HttpStatus.BAD_REQUEST);
        }
        if (importacionesIncorrectas > 0){
            resultadoImportacion.add(new ErroresImportador("","",importacionesCorrectas, importacionesIncorrectas));
            return ResponseEntity.badRequest().body(resultadoImportacion);
        }
        return new ResponseEntity("Excel importado correctamente.....", HttpStatus.OK);
    }
}

