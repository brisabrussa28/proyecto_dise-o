package ar.edu.utn.frba.dds.domain.csv;

import com.opencsv.*;
import com.opencsv.exceptions.CsvValidationException;
import ar.edu.utn.frba.dds.domain.exceptions.ArchivoVacioException;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.CampoHecho;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class LectorCsv {

  public List<Hecho> importar(String rutaCsv, char separador, Map<CampoHecho, List<String>> mapeoColumnas, String formatoFecha) {
    List<Hecho> hechosImportados = new ArrayList<>();

    try (CSVReader reader = new CSVReaderBuilder(new FileReader(rutaCsv))
        .withCSVParser(new CSVParserBuilder().withSeparator(separador).build())
        .build()) {
      String[] columnas = reader.readNext();
      if (columnas == null) {
        throw new ArchivoVacioException("El archivo se encuentra vacío.");
      }

      // Validar columnas obligatorias
      for (CampoHecho campo : mapeoColumnas.keySet()) {
        for (String columna : mapeoColumnas.get(campo)) {
          if (!List.of(columnas).contains(columna)) {
            throw new IllegalArgumentException("Falta la columna: " + columna);
          }
        }
      }

      String[] valores;

      while ((valores = reader.readNext()) != null) {
        int indiceLatitud = List.of(columnas).indexOf("latitud");


        double latitud = (mapeoColumnas.containsKey(CampoHecho.LATITUD) && valores[List.of(columnas).indexOf("latitud")] != "")
            ? Double.parseDouble(valores[indiceLatitud].trim())
            : 0.0;

        int indiceLongitud = List.of(columnas).indexOf("longitud");
        double longitud = (mapeoColumnas.containsKey(CampoHecho.LONGITUD) && valores[indiceLongitud] != "")
            ? Double.parseDouble(valores[indiceLongitud].trim())
            : 0.0;

        PuntoGeografico ubicacion = new PuntoGeografico(latitud, longitud);

        String titulo = mapeoColumnas.containsKey(CampoHecho.TITULO)
            ? concatenarColumnas(mapeoColumnas.get(CampoHecho.TITULO), columnas, valores, " ")
            : "";

        String descripcion = mapeoColumnas.containsKey(CampoHecho.DESCRIPCION)
            ? concatenarColumnas(mapeoColumnas.get(CampoHecho.DESCRIPCION), columnas, valores, " ")
            : "";

        String categoria = mapeoColumnas.containsKey(CampoHecho.CATEGORIA)
            ? concatenarColumnas(mapeoColumnas.get(CampoHecho.CATEGORIA), columnas, valores, " ")
            : "";

        String fechaSuceso = mapeoColumnas.containsKey(CampoHecho.FECHA_SUCESO) && !valores[List.of(columnas).indexOf("fechaSuceso")].isBlank()
            ? concatenarColumnas(mapeoColumnas.get(CampoHecho.FECHA_SUCESO), columnas, valores, "/")
            : null;

        String direccion = mapeoColumnas.containsKey(CampoHecho.DIRECCION)
            ? concatenarColumnas(mapeoColumnas.get(CampoHecho.DIRECCION), columnas, valores, ", ")
            : "";
        // agregar esta validacion para todos los demas campos
        // el formatter no tiene hora
        LocalDateTime fechaHora = null;
        Date fecha = null;
        if (fechaSuceso != null && !fechaSuceso.isBlank()) {
          SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
          fecha = formato.parse(fechaSuceso);
        }
        List<String> etiquetasVacias = new ArrayList<>();

        Hecho hecho = new Hecho(
            titulo,
            descripcion,
            categoria,
            direccion,
            ubicacion,
            fecha,
            LocalDateTime.now(),
            Origen.DATASET,
            etiquetasVacias
        );

        hechosImportados.add(hecho);
      }

    } catch (IOException | CsvValidationException e) {
      throw new RuntimeException("Error al leer el archivo CSV: " + e.getMessage(), e);
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }

    if (hechosImportados.isEmpty()) {
      throw new IllegalStateException("No se creó ningún hecho.");
    }

    return hechosImportados;
  }

  private String concatenarColumnas(List<String> columnasMapeadas, String[] columnas, String[] valores, String separadorConcatenacion) {
    StringBuilder resultado = new StringBuilder();
    for (String columna : columnasMapeadas) {
      int indice = List.of(columnas).indexOf(columna);
      if (indice != -1 && !valores[indice].isBlank()) {
        if (resultado.length() > 0) {
          resultado.append(separadorConcatenacion);
        }
        resultado.append(valores[indice]);
      }
    }
    return resultado.toString();
  }
}