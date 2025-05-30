package ar.edu.utn.frba.dds.domain.csv;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

import ar.edu.utn.frba.dds.domain.hecho.CampoHecho;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;

import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

public class UltraBindLector {

  public List<Hecho> importar(String path, char separator, String dateFormatStr, Map<CampoHecho, List<String>> mapeoColumnas) {
    List<Hecho> hechos = new ArrayList<>();
    SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatStr);
    dateFormat.setLenient(false); // ⛔ Esto lo vuelve estricto


    try (CSVReader reader = new CSVReaderBuilder(new FileReader(path))
        .withCSVParser(new CSVParserBuilder().withSeparator(separator).build())
        .build()) {

      String[] headers = reader.readNext();
      if (headers == null || headers.length == 0) {
        throw new IllegalArgumentException("El archivo CSV no contiene encabezados");
      }

      Set<String> columnasPresentes = new HashSet<>(Arrays.asList(headers));

      String[] row;
      int linea = 1;
      while ((row = reader.readNext()) != null) {
        linea++;
        Map<String, String> fila = new HashMap<>();
        for (int i = 0; i < headers.length && i < row.length; i++) {
          fila.put(headers[i], row[i] != null ? row[i].trim() : null);
        }

        String titulo = extraerCampo(fila, mapeoColumnas.get(CampoHecho.TITULO), columnasPresentes);
        String descripcion = extraerCampo(fila, mapeoColumnas.get(CampoHecho.DESCRIPCION), columnasPresentes);
        String categoria = extraerCampo(fila, mapeoColumnas.get(CampoHecho.CATEGORIA), columnasPresentes);
        String direccion = extraerCampo(fila, mapeoColumnas.get(CampoHecho.DIRECCION), columnasPresentes);

        String latStr = extraerCampo(fila, mapeoColumnas.get(CampoHecho.LATITUD), columnasPresentes);
        String lonStr = extraerCampo(fila, mapeoColumnas.get(CampoHecho.LONGITUD), columnasPresentes);
        Double latitud = parseDouble(latStr);
        Double longitud = parseDouble(lonStr);
        PuntoGeografico ubicacion = (latitud != null && longitud != null) ? new PuntoGeografico(latitud, longitud) : null;

        String fechaStr = extraerCampo(fila, mapeoColumnas.get(CampoHecho.FECHA_SUCESO), columnasPresentes);
        Date fechaSuceso = parseFecha(fechaStr, dateFormat);

        boolean datosValidos = titulo != null && fechaSuceso != null;

        if (datosValidos) {
          Hecho hecho = new Hecho(
              titulo,
              descripcion,
              categoria,
              direccion,
              ubicacion,
              fechaSuceso,
              new Date(),
              Origen.DATASET,
              List.of()
          );
          hechos.add(hecho);
        } else {
          System.err.println("[Línea " + linea + "] Fila descartada por datos insuficientes");
        }
      }

    } catch (IOException | CsvException e) {
      throw new RuntimeException("Error al leer el archivo CSV", e);
    }

    return hechos;
  }

  private String extraerCampo(Map<String, String> fila, List<String> columnas, Set<String> columnasPresentes) {
    if (columnas == null) return null;
    return columnas.stream()
        .peek(col -> {
          if (!columnasPresentes.contains(col)) {
            System.err.println("Advertencia: columna "+ col +" no encontrada en CSV");
          }
        })
        .map(fila::get)
        .filter(s -> s != null && !s.trim().isEmpty())
        .reduce((a, b) -> a + " " + b)
        .orElse(null);
  }

  private Double parseDouble(String valor) {
    try {
      return valor != null ? Double.parseDouble(valor.trim()) : null;
    } catch (Exception e) {
      System.err.println("Error al parsear double: '" + valor + "'");
      return null;
    }
  }

  private Date parseFecha(String valor, SimpleDateFormat dateFormat) {
    try {
      return valor != null ? dateFormat.parse(valor.trim()) : null;
    } catch (ParseException e) {
      System.err.println("Error al parsear fecha: '" + valor + "'");
      return null;
    }
  }
}
