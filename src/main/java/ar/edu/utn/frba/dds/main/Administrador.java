package ar.edu.utn.frba.dds.main;

import ar.edu.utn.frba.dds.domain.Coleccion;
import ar.edu.utn.frba.dds.domain.Hecho;
import ar.edu.utn.frba.dds.domain.Solicitud;
import java.util.ArrayList;
import java.util.List;

public class Administrador extends Persona {
  public Administrador(String nombre, String email) {
    super(nombre, email);
  }

  public Coleccion crearColeccion(String titulo, String descripcion) {
    return new Coleccion(titulo, descripcion);
  }

  public List<Hecho> importarDesdeCSV(String rutaCSV) {
    // TODO lectura de CSV
    return new ArrayList<>();
  }

  public void consultarSolicitudes() {

  }

  public void procesarSolicitud(Solicitud solicitudEliminacion) {

  }
}

