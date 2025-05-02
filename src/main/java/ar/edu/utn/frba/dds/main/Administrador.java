package ar.edu.utn.frba.dds.main;

import ar.edu.utn.frba.dds.domain.Coleccion;
import ar.edu.utn.frba.dds.domain.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.Hecho;
import ar.edu.utn.frba.dds.domain.Etiqueta;
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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Administrador extends Persona {
  public Administrador(String nombre, String email) {
    super(nombre, email);
  }

  public Coleccion crearColeccion(String titulo, String descripcion, String categoria) {
    return new Coleccion(titulo, descripcion, categoria);
  }

  public List<Hecho> importarDesdeCSV(String rutaCSV) {
    List<Hecho> hechosImportados = new ArrayList<>();
     final String NombreFuenteCSV = "Fuente CSV";
    final String SeparadorColumna = ";";
    final String SeparadorEtiquetas = ",";

    try (Stream<String> lineas = Files.lines(Paths.get(rutaCSV))) {
      lineas
          .skip(1) // salteamos encabezado
          .forEach(linea -> {
            String[] campos = linea.split(SeparadorColumna, -1);

            if (campos.length < 8) return;

            String titulo = campos[0];
            String descripcion = campos[1];
            String categoria = campos[2];
            String direccion = campos[3];
            double latitud = Double.parseDouble(campos[4]);
            double longitud = Double.parseDouble(campos[5]);
            LocalDateTime fecha;

            try {
              fecha = LocalDateTime.parse(campos[6]);
            } catch (Exception e) {
              throw new FechaInvalidaException("Formato de fecha incorrecto en CSV: " + campos[6], e);
            }

            List<Etiqueta> etiquetas = Arrays.stream(campos[7].split(SeparadorEtiquetas)).map(Etiqueta::new).collect(Collectors.toList());

            PuntoGeografico ubicacion = new PuntoGeografico(latitud, longitud);

            FuenteDinamica fuenteTemporal = new FuenteDinamica(NombreFuenteCSV, new ArrayList<>());

            Hecho hecho = new Hecho(
                titulo, descripcion, categoria, direccion, ubicacion, fecha, LocalDateTime.now(), fuenteTemporal, etiquetas
            );

            fuenteTemporal.agregarHecho(hecho);
            hechosImportados.add(hecho);
          });
    } catch (IOException e) {
      throw new ErrorLecturaCSVException("Error al leer el archivo CSV: " + rutaCSV, e);
    }

    return hechosImportados;
  }

  public void aceptarSolicitud(Solicitud solicitud, Coleccion coleccion) {
    coleccion.eliminarHecho(solicitud.getHechoSolicitado());
  }

  public void rechazarSolicitud(Solicitud solicitud) {
    // se deberia hacer algo al rechazar?
  }
}

