package ar.edu.utn.frba.dds.main;

import ar.edu.utn.frba.dds.domain.Coleccion;
import ar.edu.utn.frba.dds.domain.ErrorCSV;
import ar.edu.utn.frba.dds.domain.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.Hecho;
import ar.edu.utn.frba.dds.domain.Etiqueta;
import ar.edu.utn.frba.dds.domain.LectorCSV;
import ar.edu.utn.frba.dds.domain.MapeoCSV;
import ar.edu.utn.frba.dds.domain.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.Solicitud;
import ar.edu.utn.frba.dds.domain.exceptions.ErrorLecturaCSVException;
import ar.edu.utn.frba.dds.domain.exceptions.FechaInvalidaException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Administrador extends Persona {
  public Administrador(String nombre, String email) {
    super(nombre, email);
  }

  public Coleccion crearColeccion(String titulo, String descripcion, String categoria) {
    return new Coleccion(titulo, descripcion, categoria);
  }

  public List<Hecho> importarDesdeCSV(String rutaCSV, MapeoCSV mapeo, String separador, String nombreFuente, String categoria ) {
    LectorCSV lector = new LectorCSV();
    lector.importar(rutaCSV, mapeo, separador, nombreFuente, categoria);

    System.out.println("Importación completada. Se importaron " + lector.getHechosImportados().size() + " hechos.");

    if (lector.hasErrores()) {
      System.out.println("Se encontraron " + lector.getErrores().size() + " errores durante la importación:");
      //for (ErrorCSV error : lector.getErrores()) {
      //System.out.println("  Línea " + error.getNumeroFila() + ": " + error.getMensaje());
      //}

      // Agrupar errores por mensaje
      Map<String, List<Integer>> erroresAgrupados = new LinkedHashMap<>();
      for (ErrorCSV error : lector.getErrores()) {
        erroresAgrupados
            .computeIfAbsent(error.getMensaje(), k -> new ArrayList<>())
            .add(error.getNumeroFila());
      }

      // Imprimir errores agrupados
      for (Map.Entry<String, List<Integer>> entry : erroresAgrupados.entrySet()) {
        String mensaje = entry.getKey();
        List<Integer> lineas = entry.getValue();
        System.out.println("Mensaje: " + mensaje);
        System.out.println("  Líneas: " + lineas.stream()
            .map(String::valueOf)
            .collect(Collectors.joining(", ")));
      }
    }

    return lector.getHechosImportados();
  }

  public void aceptarSolicitud(Solicitud solicitud, Coleccion coleccion) {
    coleccion.eliminarHecho(solicitud.getHechoSolicitado());
  }

  public void rechazarSolicitud(Solicitud solicitud) {
    // se deberia hacer algo al rechazar?
  }
}

