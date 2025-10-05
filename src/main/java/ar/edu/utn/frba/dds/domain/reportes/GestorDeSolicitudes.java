package ar.edu.utn.frba.dds.domain.reportes;

import ar.edu.utn.frba.dds.domain.exceptions.SolicitudInexistenteException;
import ar.edu.utn.frba.dds.domain.filtro.Filtro;
import ar.edu.utn.frba.dds.domain.filtro.condiciones.CondicionPredicado;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.reportes.detectorspam.DetectorSpam;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Gestor de Solicitudes. Orquesta la lógica de negocio para crear,
 * clasificar y procesar las solicitudes de eliminación de hechos.
 */
public class GestorDeSolicitudes {

  private final RepositorioDeSolicitudes repositorio;

  public GestorDeSolicitudes(RepositorioDeSolicitudes repositorio) {
    this.repositorio = repositorio;
  }

  /**
   * Crea una nueva solicitud, la clasifica usando el detector de spam
   * y la guarda en el repositorio.
   *
   * @param hecho          El hecho a solicitar eliminación.
   * @param motivo         La razón de la solicitud.
   * @param detectorSpam   El detector de spam a utilizar. (Inyectado por método)
   */
  public void crearSolicitud(Hecho hecho, String motivo, DetectorSpam detectorSpam) {
    Solicitud nuevaSolicitud = new Solicitud(hecho, motivo);

    if (detectorSpam.esSpam(nuevaSolicitud.getRazonEliminacion())) {
      nuevaSolicitud.marcarComoSpam();
    }
    // Si no es spam, se queda como PENDIENTE por defecto.

    repositorio.guardar(nuevaSolicitud);
  }

  /**
   * Procesa una solicitud existente para aceptarla o rechazarla.
   *
   * @param solicitud   La solicitud a gestionar.
   * @param decision    La decisión (ACEPTAR o RECHAZAR).
   */
  public void gestionarSolicitud(Solicitud solicitud, AceptarSolicitud decision) {
    // Verificamos que la solicitud exista en estado PENDIENTE
    if (!repositorio.obtenerPorEstado(EstadoSolicitud.PENDIENTE).contains(solicitud)) {
      throw new SolicitudInexistenteException("La solicitud no existe o no está pendiente.");
    }

    if (decision == AceptarSolicitud.ACEPTAR) {
      solicitud.aceptar();
    } else {
      solicitud.rechazar();
    }

    repositorio.guardar(solicitud); // Guardamos la solicitud con su nuevo estado
  }

  /**
   * Devuelve un conteo de las solicitudes marcadas como spam.
   *
   * @return El número de solicitudes de spam.
   */
  public long cantidadDeSpamDetectado() {
    return repositorio.obtenerPorEstado(EstadoSolicitud.SPAM).size();
  }

  public List<Solicitud> getSolicitudesPendientes() {
    return repositorio.obtenerPorEstado(EstadoSolicitud.PENDIENTE);
  }

  /**
   * Devuelve una lista de todos los hechos que fueron eliminados
   * (es decir, cuyas solicitudes fueron ACEPTADAS).
   *
   * @return Lista de hechos eliminados.
   */
  public List<Hecho> obtenerHechosEliminados() {
    return repositorio.obtenerPorEstado(EstadoSolicitud.ACEPTADA)
                      .stream()
                      .map(Solicitud::getHechoSolicitado)
                      .collect(Collectors.toList());
  }

  /**
   * Devuelve un filtro que puede ser usado para excluir los hechos eliminados
   * de otros reportes.
   *
   * @return Un Filtro que excluye los hechos eliminados.
   */
  public Filtro filtroExcluyenteDeHechosEliminados() {
    List<Hecho> hechosEliminados = this.obtenerHechosEliminados();
    return new Filtro(new CondicionPredicado(h -> !hechosEliminados.contains(h)));
  }
}