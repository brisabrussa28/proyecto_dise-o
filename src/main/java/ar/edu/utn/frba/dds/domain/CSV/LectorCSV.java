package ar.edu.utn.frba.dds.domain.CSV;

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

  public FuenteEstatica importar(String rutaCSV, MapeoCSV mapeo, String separador, String nombreFuente) {
    try (BufferedReader br = new BufferedReader(new FileReader(rutaCSV))) {
      String headerLine = br.readLine();
      if (headerLine == null) throw new IllegalArgumentException("El archivo CSV está vacío.");

      String[] columnas = headerLine.split(separador);
      Set<String> columnasSet = new HashSet<>(Arrays.asList(columnas));

      // Columnas obligatorias
      List<String> columnasNecesarias = List.of(
          "titulo", "descripcion", "latitud", "longitud", "fechaSuceso", "categoria"
      );

      // Chequear si faltan columnas obligatorias
      for (String col : columnasNecesarias) {
        if (!columnasSet.contains(col)) {
          throw new IllegalArgumentException("Falta la columna obligatoria: " + col);
        }
      }

      String linea;
      int filaNumero = 1; // para mensajes de error
      while ((linea = br.readLine()) != null) {
        filaNumero++;
        String[] valores = linea.split(separador, -1); // -1 para incluir vacíos
        Map<String, String> filaMap = new HashMap<>();
        for (int i = 0; i < columnas.length; i++) {
          filaMap.put(columnas[i], i < valores.length ? valores[i] : "");
        }

        // Chequear campos obligatorios no vacíos
        for (String col : columnasNecesarias) {
          String valor = filaMap.get(col);
          if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("Campo vacío en columna '" + col + "' en la fila " + filaNumero);
          }
        }

        // Convertir latitud y longitud a double
        double latitud;
        double longitud;
        try {
          latitud = Double.parseDouble(filaMap.get("latitud"));
          longitud = Double.parseDouble(filaMap.get("longitud"));
        } catch (NumberFormatException e) {
          throw new IllegalArgumentException("Latitud o longitud inválida en la fila " + filaNumero);
        }
        PuntoGeografico ubicacion = new PuntoGeografico(latitud, longitud);

        // Mapear usando MapeoCSV
        String titulo = mapeo.obtenerTitulo.apply(filaMap);
        String descripcion = mapeo.obtenerDescripcion.apply(filaMap);
        String direccion = columnasSet.contains("direccion") ? mapeo.obtenerDireccion.apply(filaMap) : null;
        String categoria = mapeo.obtenerCategoria.apply(filaMap);
        LocalDateTime fechaSuceso = mapeo.obtenerFecha.apply(filaMap);

        // Crear lista vacía de etiquetas
        List<Etiqueta> etiquetasVacias = new ArrayList<>();

        Hecho hecho = new Hecho(
            titulo,
            descripcion,
            categoria,
            direccion,
            ubicacion,
            fechaSuceso,
            LocalDateTime.now(), // fecha de carga actual
            nombreFuente,
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
