package ar.edu.utn.frba.dds.domain.csv;

import java.util.Map;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import com.opencsv.bean.CsvToBeanBuilder;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import ar.edu.utn.frba.dds.domain.hecho.CampoHecho;
import java.util.List;
import java.time.ZoneId;
import java.time.LocalDateTime;
import java.util.Date;



public class UltraBindLector {

  public static List<Hecho> importar(String path, char separador, String formatoFecha, Map<CampoHecho, List<String>> mapeoColumnas) {
    List<Hecho> hechosImportados = new ArrayList<>();
    try {
      List<BindCsv> csvBeanReader = new CsvToBeanBuilder(new FileReader(path))
          .withSeparator(separador).withIgnoreEmptyLine(true).withType(BindCsv.class).build().parse();

      for (BindCsv cr : csvBeanReader) {
        float latitud = obtenerValorNumerico(cr, mapeoColumnas.get(CampoHecho.LATITUD));
        float longitud = obtenerValorNumerico(cr, mapeoColumnas.get(CampoHecho.LONGITUD));

        SimpleDateFormat formato = new SimpleDateFormat(formatoFecha);
        Date fechaSuceso = null;
        try {
          String fechaStr = obtenerValorTexto(cr, mapeoColumnas.get(CampoHecho.FECHA_SUCESO), "/");
          if (fechaStr != null) {
            fechaSuceso = formato.parse(fechaStr);
          }
        } catch (ParseException e) {
          throw new RuntimeException(e);
        }

        PuntoGeografico ubicacion = new PuntoGeografico(latitud, longitud);
        List<String> etiquetasVacias = new ArrayList<>();
        String direccion = obtenerValorTexto(cr, mapeoColumnas.get(CampoHecho.DIRECCION), ", ");

        Hecho hecho = new Hecho(
            obtenerValorTexto(cr, mapeoColumnas.get(CampoHecho.TITULO), " "),
            obtenerValorTexto(cr, mapeoColumnas.get(CampoHecho.DESCRIPCION), " "),
            obtenerValorTexto(cr, mapeoColumnas.get(CampoHecho.CATEGORIA), " "),
            direccion,
            ubicacion,
            fechaSuceso,
            Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()),
            Origen.DATASET,
            etiquetasVacias
        );

        hechosImportados.add(hecho);
      }
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
    return hechosImportados;
  }

  private static float obtenerValorNumerico(BindCsv cr, List<String> columnas) {
    String valor = obtenerValorTexto(cr, columnas, "");
    if (valor != null && !valor.isBlank()) {
      return Float.parseFloat(valor);
    }
    return 0;
  }

  private static String obtenerValorTexto(BindCsv cr, List<String> columnas, String separador) {
    return cr.concatenarColumnas(columnas, separador);
  }
}