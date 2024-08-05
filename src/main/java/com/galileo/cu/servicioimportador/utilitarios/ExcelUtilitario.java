package com.galileo.cu.servicioimportador.utilitarios;
import org.springframework.web.multipart.MultipartFile;

public class ExcelUtilitario {


  public static String TYPE_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
  public static String TYPE_XLS = "application/vnd.ms-excel";

  public static boolean esUnExcel(MultipartFile file) {
    return (TYPE_XLSX.equals(file.getContentType()) || TYPE_XLS.equals(file.getContentType()));
  }

}
