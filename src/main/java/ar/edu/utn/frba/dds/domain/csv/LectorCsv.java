package ar.edu.utn.frba.dds.domain.csv;

import ar.edu.utn.frba.dds.domain.exceptions.ArchivoVacioException;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteEstatica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Lector CSV.
 */
public class LectorCsv {

  private final List<Hecho> hechosImportados = new ArrayList<>();

  /**
   * Fuente Estatica.
   *
   * @param nombreFuente Nombre de la fuente
   * @param rutaCsv      Ruta al archivo CSV
   * @param separador    Separador de campos
   */
  public FuenteEstatica importar(String rutaCsv, String separador, String nombreFuente) {
    try (BufferedReader br = new BufferedReader(
        new InputStreamReader(new FileInputStream(rutaCsv), StandardCharsets.UTF_8))) {

      String headerLine = br.readLine();
      if (headerLine == null) {
        throw new ArchivoVacioException("El archivo se encuentra vacío.");
      }

      String[] columnas = headerLine.split(separador);
      Set<String> columnasSet = new HashSet<>(Arrays.asList(columnas));

      // Columnas obligatorias
      List<String> columnasNecesarias = List.of(
          "titulo", "descripcion", "latitud", "longitud", "fechaSuceso", "categoria"
      );

      for (String col : columnasNecesarias) {
        if (!columnasSet.contains(col)) {
          throw new IllegalArgumentException("Falta la columna obligatoria: " + col);
        }
      }

      String linea;
      int filaNumero = 1;
      while ((linea = br.readLine()) != null) {
        filaNumero++;
        String[] valores = linea.split(separador, -1);
        Map<String, String> filaMap = new HashMap<>();
        for (int i = 0; i < columnas.length; i++) {
          filaMap.put(columnas[i], i < valores.length ? valores[i] : "");
        }

        for (String col : columnasNecesarias) {
          String valor = filaMap.get(col);
          if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("Campo vacío " + col + " en fila " + filaNumero);
          }
        }

        double latitud;
        double longitud;
        try {
          latitud = Double.parseDouble(filaMap.get("latitud"));
          longitud = Double.parseDouble(filaMap.get("longitud"));
        } catch (NumberFormatException e) {
          throw new IllegalArgumentException("Latitud o longitud inválida en fila " + filaNumero);
        }

        PuntoGeografico ubicacion = new PuntoGeografico(latitud, longitud);

        String titulo = filaMap.get("titulo");
        String descripcion = filaMap.get("descripcion");
        String direccion = columnasSet.contains("direccion") ? filaMap.get("direccion") : null;
        String categoria = filaMap.get("categoria");
        String fechaSuceso = filaMap.get("fechaSuceso");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate fecha = LocalDate.parse(fechaSuceso, formatter);
        LocalDateTime fechaHora = fecha.atStartOfDay();

        List<String> etiquetasVacias = new ArrayList<>();

        Hecho hecho = new Hecho(
            titulo,
            descripcion,
            categoria,
            direccion,
            ubicacion,
            fechaHora,
            LocalDateTime.now(),
            Origen.DATASET,
            etiquetasVacias
        );

        hechosImportados.add(hecho);
      }

      if (hechosImportados.isEmpty()) {
        throw new IllegalStateException("No se creó ningún hecho.");
      }

      return new FuenteEstatica(nombreFuente, hechosImportados);

    } catch (IOException e) {
      throw new RuntimeException("Error al leer el archivo CSV: " + e.getMessage(), e);
    }
  }

  public List<Hecho> getHechosImportados() {
    return Collections.unmodifiableList(hechosImportados);
  }
}
