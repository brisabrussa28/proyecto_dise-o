package ar.edu.utn.frba.dds.domain;

import ar.edu.utn.frba.dds.domain.exceptions.ErrorLecturaCSVException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class LectorCSV {
  private List<ErrorCSV> errores = new ArrayList<>();
  private List<Hecho> hechosImportados = new ArrayList<>();

  public void importar(String rutaCSV, MapeoCSV mapeo, String separador, String nombreFuente, String categoria) {
    //List<Hecho> hechosImportados = new ArrayList<>();
    //final String nombreFuente = "Fuente CSV";

    try (Stream<String> lineas = Files.lines(Paths.get(rutaCSV))) {
      Iterator<String> iter = lineas.iterator();

      if (!iter.hasNext()) return;
      String[] encabezados = iter.next().split(separador, -1);

      int numeroFila = 1;
      while (iter.hasNext()) {
        String linea = iter.next();
        String[] valores = linea.split(separador, -1);

        if (valores.length != encabezados.length) {
          errores.add(new ErrorCSV(numeroFila, "Error al parsear fila. No coinciden cantidad de datos con las columnas"));
          continue;
        }

        Map<String, String> fila = new HashMap<>();
        for (int i = 0; i < encabezados.length; i++) {
          fila.put(encabezados[i], valores[i]);
        }

        String titulo = null, descripcion = null, direccion = null;
        LocalDateTime fecha = null;
        PuntoGeografico ubicacion = null;
        List<Etiqueta> etiquetas = null;

        try {
          titulo = mapeo.obtenerTitulo.apply(fila);
        } catch (Exception e) {
          errores.add(new ErrorCSV(numeroFila, "Error al obtener título: " + e.getMessage()));
        }

        try {
          descripcion = mapeo.obtenerDescripcion.apply(fila);
        } catch (Exception e) {
          errores.add(new ErrorCSV(numeroFila, "Error al obtener descripción: " + e.getMessage()));
        }

        try {
          direccion = mapeo.obtenerDireccion.apply(fila);
        } catch (Exception e) {
          errores.add(new ErrorCSV(numeroFila, "Error al obtener dirección: " + e.getMessage()));
        }

        try {
          fecha = mapeo.obtenerFecha.apply(fila);
        } catch (Exception e) {
          errores.add(new ErrorCSV(numeroFila, "Error al obtener fecha: " + e.getMessage()));
        }

        try {
          ubicacion = mapeo.obtenerUbicacion.apply(fila);
        } catch (Exception e) {
          errores.add(new ErrorCSV(numeroFila, "Error al obtener ubicación: " + e.getMessage()));
        }

        try {
          etiquetas = mapeo.obtenerEtiquetas.apply(fila);
        } catch (Exception e) {
          errores.add(new ErrorCSV(numeroFila, "Error al obtener etiquetas: " + e.getMessage()));
        }

        FuenteDinamica fuenteTemporal = new FuenteDinamica(nombreFuente, new ArrayList<>());
        Hecho hecho = new Hecho(titulo, descripcion, categoria, direccion, ubicacion, fecha, LocalDateTime.now(), fuenteTemporal, etiquetas);
        fuenteTemporal.agregarHecho(hecho);
        this.hechosImportados.add(hecho);

        numeroFila++;
      }

    } catch (IOException e) {
      errores.add(new ErrorCSV(0, "Error al leer el archivo: " + e.getMessage()));
    }
  }

  public List<Hecho> getHechosImportados() {
    return this.hechosImportados;
  }

  public boolean hasErrores() {
    return !errores.isEmpty();
  }

  public List<ErrorCSV> getErrores() {
    return errores;
  }
}
