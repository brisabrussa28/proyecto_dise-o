package ar.edu.utn.frba.dds.main;

import ar.edu.utn.frba.dds.domain.Coleccion;
import ar.edu.utn.frba.dds.domain.Hecho;
import java.util.ArrayList;
import java.util.List;

public class Administrador extends Persona {
  public Administrador(String nombre, String email) {
    super(nombre, email);
  }

  public Coleccion crearColeccion(String titulo, String descripcion) {
    return new Coleccion(titulo, new ArrayList<>());
  }

  public List<Hecho> importarDesdeCSV(String rutaCSV) {
    // TODO lectura de CSV
    return new ArrayList<>();
  }

  //TODO procesarSolicitud(SolicitudEliminacion solicitud)
}

