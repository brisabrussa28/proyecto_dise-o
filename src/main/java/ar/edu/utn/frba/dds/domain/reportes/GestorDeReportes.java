package ar.edu.utn.frba.dds.domain.reportes;

    import ar.edu.utn.frba.dds.domain.hecho.Hecho;
    import ar.edu.utn.frba.dds.domain.exceptions.SolicitudInexistenteException;
    import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
    import java.util.ArrayList;
    import java.util.List;

    public class GestorDeReportes {
      private static GestorDeReportes instancia; // Singleton instance
      private List<Solicitud> solicitudes;

      private GestorDeReportes() {
        this.solicitudes = new ArrayList<>();
      }

      public static GestorDeReportes getInstancia() {
        if (instancia == null) {
          instancia = new GestorDeReportes();
        }
        return instancia;
      }

      public void agregarSolicitud(Solicitud solicitud) {
        this.solicitudes.add(solicitud);
      }

      public Solicitud obtenerSolicitudPorPosicion(int posicion) {
        if (posicion < 0 || posicion >= solicitudes.size()) {
          throw new SolicitudInexistenteException("La posición es inválida o no existe en el gestor.");
        }
        return solicitudes.get(posicion);
      }

      public Solicitud obtenerSolicitud() {
        return this.obtenerSolicitudPorPosicion(0);
      }



      public void gestionarSolicitud(Solicitud solicitud, boolean aceptarSolicitud) {
        if (!solicitudes.contains(solicitud)) {
          throw new SolicitudInexistenteException("La solicitud no existe en el gestor.");
        }

        solicitudes.remove(solicitud);

        if (aceptarSolicitud) {
          eliminarHecho(solicitud.getHechoSolicitado(), solicitud.getFuente());
        }
      }

      private void eliminarHecho(Hecho hecho, Fuente fuente) {
        fuente.eliminarHecho(hecho);
      }

      public List<Solicitud> getSolicitudes() {
        return solicitudes;
      }
    }