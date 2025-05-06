package ar.edu.utn.frba.dds.main;

import ar.edu.utn.frba.dds.domain.Coleccion;
import ar.edu.utn.frba.dds.domain.Hecho;
import ar.edu.utn.frba.dds.domain.fuentes.*;
import ar.edu.utn.frba.dds.domain.CSV.LectorCSV;
import ar.edu.utn.frba.dds.domain.CSV.MapeoCSV;
import ar.edu.utn.frba.dds.domain.Solicitud;
import java.util.List;

public class Administrador extends Persona {
  public Administrador(String nombre, String email) {
    super(nombre, email);
  }

  public Coleccion crearColeccion(String titulo, String descripcion, String categoria) {
    return new Coleccion(titulo, descripcion, categoria);
  }

  public FuenteEstatica importarDesdeCSV(String rutaCSV, MapeoCSV mapeo, String separador, String nombreFuente, String categoria ) {
    LectorCSV lector = new LectorCSV();
    return lector.importar(rutaCSV, mapeo, separador, nombreFuente, categoria);
  }

  public void GestionarSolicitud(Solicitud solicitud, Coleccion coleccion) {
    /// /
  }

  public List<Hecho> EtiquetarHecho(List<Hecho> hechos, String etiqueta) {
    /// /
  }

}

