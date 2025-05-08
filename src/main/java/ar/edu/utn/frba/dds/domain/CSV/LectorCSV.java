package ar.edu.utn.frba.dds.domain.CSV;

import ar.edu.utn.frba.dds.domain.Origen.Origen;
import ar.edu.utn.frba.dds.domain.exceptions.ArchivoVacioException;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteEstatica;
import ar.edu.utn.frba.dds.domain.info.Etiqueta;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

//NOTE: Este lector tiene en cuenta que no piden un campo etiquetas en los csv (enunciado) sino que pide un campo categoria

public class LectorCSV {

  private List<Hecho> hechosImportados = new ArrayList<>();

  public FuenteEstatica importar(String rutaCSV, String separador, String nombreFuente) {
    try (BufferedReader br = new BufferedReader(new FileReader(rutaCSV))) {
      String headerLine = br.readLine();
      if (headerLine == null) throw new ArchivoVacioException("El mensaje se encuentra vacio.");

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
            throw new IllegalArgumentException("Campo vacío en columna '" + col + "' en la fila " + filaNumero);
          }
        }

        double latitud, longitud;
        try {
          latitud = Double.parseDouble(filaMap.get("latitud"));
          longitud = Double.parseDouble(filaMap.get("longitud"));
        } catch (NumberFormatException e) {
          throw new IllegalArgumentException("Latitud o longitud inválida en la fila " + filaNumero);
        }
        PuntoGeografico ubicacion = new PuntoGeografico(latitud, longitud);

        String titulo = filaMap.get("titulo");
        String descripcion = filaMap.get("descripcion");
        String direccion = columnasSet.contains("direccion") ? filaMap.get("direccion") : null;
        String categoria = filaMap.get("categoria");

        LocalDateTime fechaSuceso;
        try {
          fechaSuceso = LocalDateTime.parse(filaMap.get("fechaSuceso")); // O adaptar si es otro formato
        } catch (Exception e) {
          throw new IllegalArgumentException("Fecha inválida en la fila " + filaNumero);
        }

        List<Etiqueta> etiquetasVacias = new ArrayList<>();

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
        throw new IllegalStateException("No se creó ningún hecho. El archivo podría estar vacío o mal formado.");
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
