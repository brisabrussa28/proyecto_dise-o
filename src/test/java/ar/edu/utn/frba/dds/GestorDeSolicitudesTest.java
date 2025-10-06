package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.HechoBuilder;
import ar.edu.utn.frba.dds.domain.hecho.Origen;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.reportes.AceptarSolicitud;
import ar.edu.utn.frba.dds.domain.reportes.EstadoSolicitud;
import ar.edu.utn.frba.dds.domain.reportes.GestorDeSolicitudes;
import ar.edu.utn.frba.dds.domain.reportes.RepositorioDeSolicitudes;
import ar.edu.utn.frba.dds.domain.reportes.Solicitud;
import ar.edu.utn.frba.dds.domain.reportes.detectorspam.DetectorSpam;
import ar.edu.utn.frba.dds.domain.utils.DBUtils;
import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
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

  /**
   * FIX: Se agrega un método de limpieza que se ejecuta después de cada test.
   * Esto asegura que cada prueba se ejecute en un estado aislado,
   * borrando los datos de la tabla de solicitudes para no interferir con el siguiente test.
   */
  @AfterEach
  public void tearDown() {
    EntityManager em = DBUtils.getEntityManager();
    DBUtils.comenzarTransaccion(em);
    em.createQuery("DELETE FROM Solicitud").executeUpdate();
    DBUtils.commit(em);
  }

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

    // FIX: Se busca la solicitud específica que se acaba de crear en lugar de tomar la primera de la lista.
    Solicitud solicitudPendiente = repositorio.buscarPorHechoYRazon(hecho, motivoLargo).orElseThrow();
    gestor.gestionarSolicitud(solicitudPendiente, AceptarSolicitud.ACEPTAR);

    List<Hecho> eliminados = gestor.obtenerHechosEliminados();
    assertEquals(1, eliminados.size());
    assertTrue(eliminados.contains(hecho));
  }

  @Test
  @DisplayName("Rechazar una solicitud la quita de pendientes y no elimina el hecho")
  public void rechazarSolicitudNoEliminaHecho() {
    gestor.crearSolicitud(hecho, motivoLargo, detectorSpam);

    // FIX: Se busca la solicitud específica.
    Solicitud solicitudPendiente = repositorio.buscarPorHechoYRazon(hecho, motivoLargo).orElseThrow();
    gestor.gestionarSolicitud(solicitudPendiente, AceptarSolicitud.RECHAZAR);

    assertTrue(gestor.obtenerHechosEliminados().isEmpty());
    assertTrue(gestor.getSolicitudesPendientes().isEmpty());
  }

  @Test
  @DisplayName("El filtro excluyente funciona con un hecho eliminado")
  public void filtroExcluyenteNoIncluyeHechosEliminados() {
    Hecho hecho1 = crearHechoCompleto("Hecho a eliminar");
    Hecho hecho2 = crearHechoCompleto("Hecho que permanece");
    List<Hecho> hechosOriginales = List.of(hecho1, hecho2);

    gestor.crearSolicitud(hecho1, motivoLargo, detectorSpam);
    // FIX: Se busca la solicitud específica.
    Solicitud solicitudParaAceptar = repositorio.buscarPorHechoYRazon(hecho1, motivoLargo).orElseThrow();
    gestor.gestionarSolicitud(solicitudParaAceptar, AceptarSolicitud.ACEPTAR);

    List<Hecho> filtrados = gestor.filtroExcluyenteDeHechosEliminados().filtrar(hechosOriginales);

    assertFalse(filtrados.contains(hecho1));
    assertTrue(filtrados.contains(hecho2));
  }

  @Test
  @DisplayName("El filtro no excluye nada si no hay hechos eliminados")
  public void filtroSinEliminadosNoExcluyeNada() {
    Hecho hecho1 = crearHechoCompleto("Hecho de prueba 1");
    Hecho hecho2 = crearHechoCompleto("Hecho de prueba 2");
    List<Hecho> hechosOriginales = List.of(hecho1, hecho2);

    gestor.crearSolicitud(hecho1, motivoLargo, detectorSpam);

    List<Hecho> filtrados = gestor.filtroExcluyenteDeHechosEliminados().filtrar(hechosOriginales);
    assertEquals(2, filtrados.size());
  }

  @Test
  @DisplayName("El filtro excluye múltiples hechos eliminados correctamente")
  public void filtroExcluyeMultiplesHechos() {
    Hecho hecho1 = crearHechoCompleto("h1");
    Hecho hecho2 = crearHechoCompleto("h2");
    Hecho hecho3 = crearHechoCompleto("h3");
    List<Hecho> hechosOriginales = List.of(hecho1, hecho2, hecho3);

    // FIX: Se gestiona cada solicitud de forma determinista.
    gestor.crearSolicitud(hecho1, motivoLargo, detectorSpam);
    Solicitud solicitud1 = repositorio.buscarPorHechoYRazon(hecho1, motivoLargo).orElseThrow();
    gestor.gestionarSolicitud(solicitud1, AceptarSolicitud.ACEPTAR);

    gestor.crearSolicitud(hecho3, motivoLargo, detectorSpam);
    Solicitud solicitud3 = repositorio.buscarPorHechoYRazon(hecho3, motivoLargo).orElseThrow();
    gestor.gestionarSolicitud(solicitud3, AceptarSolicitud.ACEPTAR);

    List<Hecho> filtrados = gestor.filtroExcluyenteDeHechosEliminados().filtrar(hechosOriginales);

    assertFalse(filtrados.contains(hecho1));
    assertTrue(filtrados.contains(hecho2));
    assertFalse(filtrados.contains(hecho3));
  }
}

