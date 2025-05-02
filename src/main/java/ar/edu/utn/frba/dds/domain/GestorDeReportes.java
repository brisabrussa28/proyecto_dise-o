package ar.edu.utn.frba.dds.domain;

import ar.edu.utn.frba.dds.domain.exceptions.SolicitudInexistenteException;
import java.util.ArrayList;
import java.util.List;

public class GestorDeReportes {
  private List<Solicitud> solicitudes;

  public GestorDeReportes() {
    this.solicitudes = new ArrayList<>();
  }

  public void agregarSolicitud(Solicitud solicitud) {
    this.solicitudes.add(solicitud);
  }

  public void gestionarSolicitud(Solicitud solicitud, boolean aceptarSolicitud) { //TODO: Preguntar si esta bien usar un if para manejo de errores sobre una coleccion.
    if (!solicitudes.contains(solicitud)) {
      throw new SolicitudInexistenteException("La solicitud no existe en el gestor.");
    }

    solicitudes.remove(solicitud); // siempre se elimina

    if (aceptarSolicitud) {
      eliminarHechoColeccion(solicitud.getHechoSolicitado());
    }
  }

  private void eliminarHechoColeccion(Hecho hecho) {
    // Acá se implementaría la lógica real para eliminar el hecho
    // Habria que agregar el atributo "coleccion" al hecho para que este conozca a que coleccion pertenece y asi pedirle que lo saque.
    // Preguntarle a hecho su fuente y buscar la coleccion que tenga esa fuente???
    // Consultar qué colección tiene a la fuente del hecho

    Fuente fuente = hecho.getOrigen();
    List<Coleccion> coleccionesConFuente = colecciones.filter(coleccion -> coleccion.contiene(fuente)); /* 👀 */
    coleccionesConFuente.stream().map(coleccion -> coleccion.eliminar(hecho));
    System.out.println("Hecho eliminado: " + hecho.getTitulo());
  }

  public List<Solicitud> getSolicitudes() {
    return solicitudes;
  }
}