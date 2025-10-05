package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.domain.hecho.Origen;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.reportes.detectorspam.DetectorSpam;
import ar.edu.utn.frba.dds.domain.exceptions.SolicitudInexistenteException;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.HechoBuilder;
import ar.edu.utn.frba.dds.domain.reportes.AceptarSolicitud;
import ar.edu.utn.frba.dds.domain.reportes.GestorDeSolicitudes;
import ar.edu.utn.frba.dds.domain.reportes.RepositorioDeSolicitudes;
import ar.edu.utn.frba.dds.domain.reportes.Solicitud;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class GestorDeSolicitudesTest {

  private Hecho hecho;
  private GestorDeSolicitudes gestor;
  private RepositorioDeSolicitudes repositorio;
  private DetectorSpam detectorSpam;
  private final String motivoLargo = "Este es un motivo válido con más de 500 caracteres ".repeat(20);

  @BeforeEach
  public void setUp() {
    repositorio = new RepositorioDeSolicitudes();
    gestor = new GestorDeSolicitudes(repositorio);
    detectorSpam = mock(DetectorSpam.class);
    when(detectorSpam.esSpam(anyString())).thenReturn(false);
    hecho = crearHechoCompleto("Hecho de prueba principal");
  }

  // MÉTODO AUXILIAR para crear hechos válidos y evitar errores en el HechoBuilder
  private Hecho crearHechoCompleto(String titulo) {
    return new HechoBuilder()
        .conTitulo(titulo)
        .conDescripcion("Una descripcion valida para el hecho.")
        .conCategoria("Categoria de Prueba")
        .conDireccion("Direccion de Prueba 123")
        .conProvincia("Provincia de Prueba")
        .conUbicacion(new PuntoGeografico(1.0, 1.0))
        .conFechaSuceso(LocalDateTime.now())
        .conFechaCarga(LocalDateTime.now())
        .conFuenteOrigen(Origen.PROVISTO_CONTRIBUYENTE)
        .conEtiquetas(List.of("#test"))
        .build();
  }

  @Test
  @DisplayName("Se puede crear y contar una solicitud válida")
  public void agregarYContarSolicitudes() {
    gestor.crearSolicitud(hecho, motivoLargo, detectorSpam);
    assertEquals(1, gestor.getSolicitudesPendientes().size());
  }

  @Test
  @DisplayName("Una solicitud detectada como spam no se añade a pendientes")
  public void noAgregaSolicitudAPendientesSiEsSpam() {
    when(detectorSpam.esSpam(anyString())).thenReturn(true);
    gestor.crearSolicitud(hecho, motivoLargo, detectorSpam);
    assertEquals(0, gestor.getSolicitudesPendientes().size());
    assertEquals(1, gestor.cantidadDeSpamDetectado());
  }

  @Test
  @DisplayName("Aceptar una solicitud mueve el hecho a la lista de eliminados")
  public void hechoAceptadoApareceEnListaDeEliminados() {
    gestor.crearSolicitud(hecho, motivoLargo, detectorSpam);
    Solicitud solicitudPendiente = gestor.getSolicitudesPendientes().get(0);
    gestor.gestionarSolicitud(solicitudPendiente, AceptarSolicitud.ACEPTAR);
    List<Hecho> eliminados = gestor.obtenerHechosEliminados();
    assertEquals(1, eliminados.size());
    assertTrue(eliminados.contains(hecho));
  }

  @Test
  @DisplayName("Rechazar una solicitud la quita de pendientes y no elimina el hecho")
  public void rechazarSolicitudNoEliminaHecho() {
    gestor.crearSolicitud(hecho, motivoLargo, detectorSpam);
    Solicitud solicitudPendiente = gestor.getSolicitudesPendientes().get(0);
    gestor.gestionarSolicitud(solicitudPendiente, AceptarSolicitud.RECHAZAR);
    assertTrue(gestor.obtenerHechosEliminados().isEmpty());
    assertTrue(gestor.getSolicitudesPendientes().isEmpty());
  }

  @Test
  @DisplayName("El filtro excluyente funciona con un hecho eliminado")
  public void filtroExcluyenteNoIncluyeHechosEliminados() {
    // CORRECCIÓN: Se usa el método auxiliar para crear hechos completos
    Hecho hecho1 = crearHechoCompleto("Hecho a eliminar");
    Hecho hecho2 = crearHechoCompleto("Hecho que permanece");
    List<Hecho> hechosOriginales = List.of(hecho1, hecho2);

    gestor.crearSolicitud(hecho1, motivoLargo, detectorSpam);
    Solicitud solicitudParaAceptar = gestor.getSolicitudesPendientes().get(0);
    gestor.gestionarSolicitud(solicitudParaAceptar, AceptarSolicitud.ACEPTAR);

    List<Hecho> filtrados = gestor.filtroExcluyenteDeHechosEliminados().filtrar(hechosOriginales);

    assertFalse(filtrados.contains(hecho1));
    assertTrue(filtrados.contains(hecho2));
  }

  @Test
  @DisplayName("El filtro no excluye nada si no hay hechos eliminados")
  public void filtroSinEliminadosNoExcluyeNada() {
    // CORRECCIÓN: Se usa el método auxiliar para crear hechos completos
    Hecho hecho1 = crearHechoCompleto("Hecho de prueba 1");
    Hecho hecho2 = crearHechoCompleto("Hecho de prueba 2");
    List<Hecho> hechosOriginales = List.of(hecho1, hecho2);

    gestor.crearSolicitud(hecho1, motivoLargo, detectorSpam); // La solicitud no se acepta

    List<Hecho> filtrados = gestor.filtroExcluyenteDeHechosEliminados().filtrar(hechosOriginales);

    assertEquals(2, filtrados.size());
  }

  @Test
  @DisplayName("El filtro excluye múltiples hechos eliminados correctamente")
  public void filtroExcluyeMultiplesHechos() {
    // CORRECCIÓN: Se usa el método auxiliar para crear hechos completos
    Hecho hecho1 = crearHechoCompleto("h1");
    Hecho hecho2 = crearHechoCompleto("h2");
    Hecho hecho3 = crearHechoCompleto("h3");
    List<Hecho> hechosOriginales = List.of(hecho1, hecho2, hecho3);

    gestor.crearSolicitud(hecho1, motivoLargo, detectorSpam);
    gestor.gestionarSolicitud(repositorio.obtenerPorEstado(ar.edu.utn.frba.dds.domain.reportes.EstadoSolicitud.PENDIENTE).get(0), AceptarSolicitud.ACEPTAR);

    gestor.crearSolicitud(hecho3, motivoLargo, detectorSpam);
    gestor.gestionarSolicitud(repositorio.obtenerPorEstado(ar.edu.utn.frba.dds.domain.reportes.EstadoSolicitud.PENDIENTE).get(0), AceptarSolicitud.ACEPTAR);

    List<Hecho> filtrados = gestor.filtroExcluyenteDeHechosEliminados().filtrar(hechosOriginales);

    assertFalse(filtrados.contains(hecho1));
    assertTrue(filtrados.contains(hecho2));
    assertFalse(filtrados.contains(hecho3));
  }
}

