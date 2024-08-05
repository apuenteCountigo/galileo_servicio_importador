package com.galileo.cu.servicioimportador.repositorios;

import com.galileo.cu.commons.models.*;
import com.galileo.cu.servicioimportador.clients.*;
import com.galileo.cu.servicioimportador.entidades.ErroresImportador;
import com.galileo.cu.servicioimportador.entidades.LicenciaDataMiner;
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
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j2
@Repository
public class BalizaRepository {

    private final TipoBalizaFeignClient tipoBalizaFeignClient;
    private final TipoContratoFeignClient tipoContratoFeignClient;
    private final ConexionFeignClient conexionFeignClient;
    private final BalizaFeignClient balizaFeignClient;
    private final ApisFeignClient apisFeignClient;

    @Autowired
    public BalizaRepository(TipoBalizaFeignClient tipoBalizaFeignClient, TipoContratoFeignClient tipoContratoFeignClient, ConexionFeignClient conexionFeignClient, BalizaFeignClient balizaFeignClient, ApisFeignClient apisFeignClient) {
        this.tipoBalizaFeignClient = tipoBalizaFeignClient;
        this.tipoContratoFeignClient = tipoContratoFeignClient;
        this.conexionFeignClient = conexionFeignClient;
        this.balizaFeignClient = balizaFeignClient;
        this.apisFeignClient = apisFeignClient;
    }


    public ResponseEntity<List<ErroresImportador>> cargarExcelBalizas(InputStream is, String token) {

        log.info("IMPORTAR EXCEL BALIZA V.1");

        BufferedInputStream buffStream = new BufferedInputStream(is);

        int importacionesCorrectas = 0;
        int importacionesIncorrectas = 0;
        int elementosRestantesDataMiner = 0;

        try {
            elementosRestantesDataMiner = elementosRestantesDataMiner();
        }catch (Exception exception){
            if (exception.getMessage().contains("Se ha alcanzado el número máximo de elementos permitidos")){
                return new ResponseEntity("Se ha alcanzado el número máximo de elementos permitidos, por favor contacte con un Superadministrador", HttpStatus.BAD_REQUEST);
            }else  return new ResponseEntity(exception.getMessage(), HttpStatus.BAD_REQUEST);

        }


        ArrayList<ErroresImportador> resultadoImportacion = new ArrayList<>();

        try {
            Workbook workbook = WorkbookFactory.create(buffStream);

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();


            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();

                Iterator<Cell> cellsInRow = currentRow.iterator();
                TipoBaliza tipoBaliza = new TipoBaliza();
                TipoContrato tipoContrato = new TipoContrato();

                if (rowNumber == 0) {
                    if (cellsInRow.next().getStringCellValue().contains("Clave baliza")){
                        rowNumber++;
                        continue;
                    }else {
                        return new ResponseEntity("Introduzca un excel de balizas válido para importar....", HttpStatus.BAD_REQUEST);
                    }
                }


                System.out.println("rowNumber: " + rowNumber);


                SALTO:
                try {

                   Balizas balizas = new Balizas();

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
                                String nombre_baliza = currentCell.getStringCellValue();
                                try {
                                    Balizas baliza = balizaFeignClient.findFirstByClave(nombre_baliza, "Bearer "+token);
                                    if (baliza.getClave().equals(nombre_baliza)){
                                        importacionesIncorrectas++;
                                        resultadoImportacion.add(new ErroresImportador("Error en el nombre de la baliza", " Ya existe una baliza con el nombre o clave con valor:" + currentCell, importacionesCorrectas, importacionesIncorrectas));
                                        break SALTO;
                                    }
                                }catch (Exception exception){
                                    log.trace("NO EXISTE BALIZA CON ESE NOMBRE");
                                }
                                if (nombre_baliza.equals("")){
                                    importacionesIncorrectas++;
                                    resultadoImportacion.add(new ErroresImportador("Error en el nombre de la baliza", "El nombre es obligatorio." , importacionesCorrectas, importacionesIncorrectas));
                                    break SALTO;
                                }
                                balizas.setClave(currentCell.getStringCellValue());
                            }
                            case 1 -> {
                                try {
                                    tipoBaliza = tipoBalizaFeignClient.tipoBalizaFeign(currentCell.getStringCellValue());
                                    balizas.setTipoBaliza(tipoBaliza);
                                }catch (Exception e){
                                    log.error("Error accediendo al servicio tipo de baliza, al importar balizas: "+e.getMessage());
                                    return new ResponseEntity("Error al comunicarse con el servicio tipo baliza, contacte con el administrador", HttpStatus.BAD_REQUEST);
                                }

                            }
                            case 2 -> balizas.setMarca(currentCell.getStringCellValue());
                            case 3 -> balizas.setModelo(currentCell.getStringCellValue());
                            case 4 -> balizas.setNumSerie(currentCell.getStringCellValue());
                            case 5 -> balizas.setTipoCoordenada(currentCell.getStringCellValue());
                            case 6 -> {
                                String imei = currentCell.getStringCellValue();
                                Pattern pattern = Pattern.compile("^\\d{20}$");
                                Matcher matcherImei= pattern.matcher(imei);
                                if (!matcherImei.find()){
                                    importacionesIncorrectas++;
                                    resultadoImportacion.add(new ErroresImportador("Error en el IMEI de la baliza", " Introduzca un IMEI con formato correcto de 20 cifras: " + currentCell, importacionesCorrectas, importacionesIncorrectas));
                                    break SALTO;

                                }
                                balizas.setImei(imei);
                            }
                            case 7 -> {
                                String numero_telefono = currentCell.getStringCellValue();

                                if (!numero_telefono.trim().isEmpty()){
                                    Pattern pattern = Pattern.compile("^\\d{8,11}$");
                                    Matcher matcherTelefono = pattern.matcher(numero_telefono);

                                    if (!matcherTelefono.find()) {
                                        importacionesIncorrectas++;
                                        resultadoImportacion.add(new ErroresImportador("Error en el teléfono de la baliza", " Introduzca un teléfono con formato correcto, valor incorrecto: " + currentCell, importacionesCorrectas, importacionesIncorrectas));
                                        break SALTO;
                                    }
                                }

                                balizas.setTelefono1(numero_telefono);
                            }

                            case 8 -> {
                                tipoContrato = tipoContratoFeignClient.tipocontratoFeign(currentCell.getStringCellValue());
                                balizas.setTipoContrato(tipoContrato);
                            }
                            case 9 -> balizas.setCompania(currentCell.getStringCellValue());
                            case 10 ->{
                                String pin = currentCell.getStringCellValue();
                                if (!pin.trim().isEmpty()){
                                    Pattern pattern = Pattern.compile("^\\d{4}$");
                                    Matcher matcherPin = pattern.matcher(pin);

                                    if (!matcherPin.find()) {
                                        importacionesIncorrectas++;
                                        resultadoImportacion.add(new ErroresImportador("Error en el pin de la baliza", " Introduzca un pin válido de 4 cifras: " + pin, importacionesCorrectas, importacionesIncorrectas));
                                        break SALTO;
                                    }
                                }
                                balizas.setPin1(pin);}
                            case 11 ->{
                                String pin = currentCell.getStringCellValue();
                                if (!pin.trim().isEmpty()){
                                    Pattern pattern = Pattern.compile("^\\d{4}$");
                                    Matcher matcherPin = pattern.matcher(pin);

                                    if (!matcherPin.find()) {
                                        importacionesIncorrectas++;
                                        resultadoImportacion.add(new ErroresImportador("Error en el pin de la baliza", " Introduzca un pin válido de 4 cifras: " + pin, importacionesCorrectas, importacionesIncorrectas));
                                        break SALTO;
                                    }
                                }
                                balizas.setPin2(pin);
                            }
                            case 12 ->{
                                String puk = currentCell.getStringCellValue();
                                if (!puk.trim().isEmpty()){
                                    Pattern pattern = Pattern.compile("^\\d{10}$");
                                    Matcher matcherPuk = pattern.matcher(puk);

                                    if (!matcherPuk.find()) {
                                        importacionesIncorrectas++;
                                        resultadoImportacion.add(new ErroresImportador("Error en el puk de la baliza", " Introduzca un puk válido de 10 cifras: " + currentCell, importacionesCorrectas, importacionesIncorrectas));
                                        break SALTO;
                                    }
                                }
                                balizas.setPuk(puk);}
                            case 13 -> balizas.setIccTarjeta(currentCell.getStringCellValue());

                            case 14 -> {
                                Conexiones conexiones = conexionFeignClient.findConexionByIp(currentCell.getStringCellValue());
                                balizas.setServidor(conexiones);
                            }
                            case 15 -> {
                                String puerto = currentCell.getStringCellValue();
                                if (!puerto.trim().isEmpty()){
                                    Pattern pattern = Pattern.compile("^\\d{1,5}$");
                                    Matcher matcherPuerto = pattern.matcher(puerto);

                                    if (!matcherPuerto.find()) {
                                        importacionesIncorrectas++;
                                        resultadoImportacion.add(new ErroresImportador("Error en el puerto de la baliza", " Introduzca un puerto válido: " + puerto, importacionesCorrectas, importacionesIncorrectas));
                                        break SALTO;
                                    }
                                }
                                balizas.setPuerto(puerto);
                            }
                            case 16 -> balizas.setNotas(currentCell.getStringCellValue());
                            default -> {
                            }
                        }
                    }
                    if(     balizas.getClave() == null ||
                            balizas.getClave().equals("") ||
                            balizas.getMarca() == null ||
                            balizas.getMarca().equals("") ||
                            balizas.getModelo() == null ||
                            balizas.getModelo().equals("") ||
                            balizas.getNumSerie() == null ||
                            balizas.getNumSerie().equals("") ||
                            balizas.getImei() == null ||
                            balizas.getImei().equals("") ||
                            balizas.getCompania() == null ||
                            balizas.getCompania().equals("") ||
                            balizas.getPuerto() == null ||
                            balizas.getPuerto().equals("")){

                        importacionesIncorrectas++;
                        resultadoImportacion.add(new ErroresImportador("Datos incompletos", "Debe completar los datos de llenado obligatorios del excel marcados con * en el registro.", importacionesCorrectas, importacionesIncorrectas));
                        break SALTO;
                    }

                    ++importacionesCorrectas;
                    balizaFeignClient.saveBalizasExcel(balizas, token);

                }catch (Exception exception){
                    log.error("Error importando excel de balizas", exception.getMessage());
                    return new ResponseEntity("Error importando excel de balizas.....", HttpStatus.BAD_REQUEST);
                }

                if (importacionesCorrectas == elementosRestantesDataMiner){
                    resultadoImportacion.add(new ErroresImportador("Error creación de elementos", "Se ha alcanzado el número máximo de elementos permitidos, por favor contacte con un Superadministrador", importacionesCorrectas, importacionesIncorrectas));
                    return ResponseEntity.badRequest().body(resultadoImportacion);
                }
            }
            workbook.close();

        } catch (IOException e) {
            throw new RuntimeException("Error importando archivo: " + e.getMessage());
        }catch (RuntimeException exception){
            log.error("Error importando baliza: "+exception.getMessage());
            return new ResponseEntity("Error importando excel de balizas.....", HttpStatus.BAD_REQUEST);
        }
        if (importacionesIncorrectas > 0){
            resultadoImportacion.add(new ErroresImportador("","",importacionesCorrectas, importacionesIncorrectas));
            return ResponseEntity.badRequest().body(resultadoImportacion);
        }
        return new ResponseEntity("Excel importado correctamente.....", HttpStatus.OK);
    }


    public int elementosRestantesDataMiner(){
        int totalElementos;
        int elementosCreadosDataMiner;

        try {

            ArrayList<LicenciaDataMiner> licenciaDataMiners = apisFeignClient.obtenerLimiteElementosDataMiner().getBody();
            AtomicInteger totalLicencia = new AtomicInteger();
            AtomicInteger elementosCreados = new AtomicInteger();
            Objects.requireNonNull(licenciaDataMiners).forEach(d -> {
                elementosCreados.set(d.getAmountElementsActive());
                totalLicencia.set(d.getAmountElementsMaximum());
            });

            totalElementos = totalLicencia.get();
            elementosCreadosDataMiner = elementosCreados.get();
            if (totalElementos == elementosCreadosDataMiner && totalElementos != 0){
                throw new RuntimeException("Se ha alcanzado el número máximo de elementos permitidos, por favor contacte con un Superadministrador");
            }

        }catch (Exception exception){
            log.error("\"Error obteniendo el limite de dispositivos en el dataminer\": "+exception.getMessage());
            if (exception.getMessage().contains("Se ha alcanzado el número máximo de elementos permitidos")){
                throw new RuntimeException("Se ha alcanzado el número máximo de elementos permitidos, por favor contacte con un Superadministrador");
            }
            throw new RuntimeException("Error obteniendo el limite de dispositivos en el dataminer");
        }
        return totalElementos - elementosCreadosDataMiner;
    }

}

