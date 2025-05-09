package ar.edu.utn.frba.dds.domain.csv;

import ar.edu.utn.frba.dds.domain.exceptions.ArchivoVacioException;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteEstatica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

//NOTE: Este lector tiene en cuenta que no piden un campo etiquetas en los csv (enunciado)
// sino que pide un campo categoria

/**
 * Lector CSV.
 */
public class LectorCsv {

  private final List<Hecho> hechosImportados = new ArrayList<>();

  /**
   * Fuente Estacica.
   *
   * @param nombreFuente String
   * @param rutaCsv      String
   * @param separador    String
   */
  public FuenteEstatica importar(String rutaCsv, String separador, String nombreFuente) {
    try (BufferedReader br = new BufferedReader(new FileReader(rutaCsv))) {
      String headerLine = br.readLine();
      if (headerLine == null) {
        throw new ArchivoVacioException("El mensaje se encuentra vacio.");
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
            throw new IllegalArgumentException("Campo vacío '" + col + filaNumero);
          }
        }

        double latitud;
        double longitud;
        try {
          latitud = Double.parseDouble(filaMap.get("latitud"));
          longitud = Double.parseDouble(filaMap.get("longitud"));
        } catch (NumberFormatException e) {
          throw new IllegalArgumentException("Latitud o longitud inválida" + filaNumero);
        }
        PuntoGeografico ubicacion = new PuntoGeografico(latitud, longitud);

        String titulo = filaMap.get("titulo");
        String descripcion = filaMap.get("descripcion");
        String direccion = columnasSet.contains("direccion") ? filaMap.get("direccion") : null;
        String categoria = filaMap.get("categoria");

        LocalDateTime fechaSuceso;
        try {
          fechaSuceso = LocalDateTime.parse(filaMap.get("fechaSuceso"));

        } catch (Exception e) {
          throw new IllegalArgumentException("Fecha inválida en la fila " + filaNumero);
        }

        List<String> etiquetasVacias = new ArrayList<>();

        Hecho hecho = new Hecho(
            titulo,
            descripcion,
            categoria,
            direccion,
            ubicacion,
            fechaSuceso,
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
    return hechosImportados;
  }
}
