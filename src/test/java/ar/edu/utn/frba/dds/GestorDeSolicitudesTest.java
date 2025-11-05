package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.model.hecho.HechoBuilder;
import ar.edu.utn.frba.dds.model.hecho.Origen;
import ar.edu.utn.frba.dds.model.info.PuntoGeografico;
import ar.edu.utn.frba.dds.model.reportes.AceptarSolicitud;
import ar.edu.utn.frba.dds.model.reportes.GestorDeSolicitudes;
import ar.edu.utn.frba.dds.model.reportes.Solicitud;
import ar.edu.utn.frba.dds.model.reportes.detectorspam.DetectorSpam;
import ar.edu.utn.frba.dds.repositories.HechoRepository;
import ar.edu.utn.frba.dds.repositories.SolicitudesRepository;
import io.github.flbulgarelli.jpa.extras.test.SimplePersistenceTest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.TimeZone;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// 1. Implementamos la interfaz
public class GestorDeSolicitudesTest implements SimplePersistenceTest {

  private Hecho hecho;
  private GestorDeSolicitudes gestor;
  private SolicitudesRepository repositorio;
  private HechoRepository repoHechos;
  private DetectorSpam detectorSpam;
  private final String motivoLargo = "Este es un motivo válido con más de 500 caracteres ".repeat(20);

  @BeforeAll
  public static void setGlobalTimeZone() {
    TimeZone.setDefault(TimeZone.getTimeZone("America/Argentina/Buenos_Aires"));
  }

  @BeforeEach
  public void setUp() {
    // 2. Ya no necesitamos setear la TimeZone aquí (va en VM Options)
    repositorio = new SolicitudesRepository();
    repoHechos = new HechoRepository();
    gestor = new GestorDeSolicitudes(repositorio);
    detectorSpam = mock(DetectorSpam.class);
    when(detectorSpam.esSpam(anyString())).thenReturn(false);

    // SimplePersistenceTest envuelve esto en una transacción
    hecho = crearHechoCompleto("Hecho de prueba principal");
    repoHechos.save(hecho);
  }

  // 3. ELIMINAMOS por completo el método @AfterEach
  //    SimplePersistenceTest hace ROLLBACK automáticamente.

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
    var antes = gestor.getSolicitudesPendientes().size();
    gestor.crearSolicitud(hecho, motivoLargo, detectorSpam);
    assertEquals(
        antes + 1,
        gestor.getSolicitudesPendientes()
              .size()
    );
  }

  @Test
  @DisplayName("Una solicitud detectada como spam no se añade a pendientes")
  public void noAgregaSolicitudAPendientesSiEsSpam() {
    var antes = gestor.getSolicitudesPendientes().size();
    when(detectorSpam.esSpam(anyString())).thenReturn(true);
    gestor.crearSolicitud(hecho, motivoLargo, detectorSpam);
    var depois = gestor.getSolicitudesPendientes().size();
    assertEquals(antes, depois);
  }

  @Test
  @DisplayName("Aceptar una solicitud mueve el hecho a la lista de eliminados")
  public void hechoAceptadoApareceEnListaDeEliminados() {
    gestor.crearSolicitud(hecho, motivoLargo, detectorSpam);

    // 4. AÑADIMOS FLUSH: Forzamos el INSERT antes del SELECT
    entityManager().flush();

    Solicitud solicitudPendiente = repositorio.buscarPorHechoYRazon(hecho, motivoLargo)
                                              .orElseThrow();

    List<Hecho> eliminados = gestor.obtenerHechosEliminados();

    gestor.gestionarSolicitud(solicitudPendiente, AceptarSolicitud.ACEPTAR);

    assertEquals(eliminados.size() + 1, gestor.obtenerHechosEliminados().size());
    assertTrue(gestor.obtenerHechosEliminados().contains(hecho));
  }

  @Test
  @DisplayName("Rechazar una solicitud la quita de pendientes y no elimina el hecho")
  public void rechazarSolicitudNoEliminaHecho() {
    gestor.crearSolicitud(hecho, motivoLargo, detectorSpam);

    // 4. AÑADIMOS FLUSH: Forzamos el INSERT antes del SELECT
    entityManager().flush();

    Solicitud solicitudPendiente = repositorio.buscarPorHechoYRazon(hecho, motivoLargo)
                                              .orElseThrow();
    gestor.gestionarSolicitud(solicitudPendiente, AceptarSolicitud.RECHAZAR);
    assertFalse(gestor.obtenerHechosEliminados()
                      .contains(hecho));
  }

  @Test
  @DisplayName("El filtro excluyente funciona con un hecho eliminado")
  public void filtroExcluyenteNoIncluyeHechosEliminados() {
    Hecho hecho1 = crearHechoCompleto("Hecho a eliminar");
    Hecho hecho2 = crearHechoCompleto("Hecho que permanece");
    List<Hecho> hechosOriginales = List.of(hecho1, hecho2);
    repoHechos.save(hecho1);
    repoHechos.save(hecho2);

    gestor.crearSolicitud(hecho1, motivoLargo, detectorSpam);

    // 4. AÑADIMOS FLUSH: Forzamos el INSERT antes del SELECT
    entityManager().flush();

    Solicitud solicitudParaAceptar = repositorio.buscarPorHechoYRazon(hecho1, motivoLargo)
                                                .orElseThrow();
    gestor.gestionarSolicitud(solicitudParaAceptar, AceptarSolicitud.ACEPTAR);

    List<Hecho> filtrados = gestor.filtroExcluyenteDeHechosEliminados()
                                  .filtrar(hechosOriginales);

    assertFalse(filtrados.contains(hecho1));
    assertTrue(filtrados.contains(hecho2));
  }

  @Test
  @DisplayName("El filtro no excluye nada si no hay hechos eliminados")
  public void filtroSinEliminadosNoExcluyeNada() {
    Hecho hecho1 = crearHechoCompleto("Hecho de prueba 1");
    Hecho hecho2 = crearHechoCompleto("Hecho de prueba 2");
    repoHechos.save(hecho1);
    repoHechos.save(hecho2);

    List<Hecho> hechosOriginales = List.of(hecho1, hecho2);

    gestor.crearSolicitud(hecho1, motivoLargo, detectorSpam);

    // (Aquí no hay 'buscarPorHechoYRazon', no se necesita flush)

    List<Hecho> filtrados = gestor.filtroExcluyenteDeHechosEliminados()
                                  .filtrar(hechosOriginales);
    assertEquals(2, filtrados.size());
  }

  @Test
  @DisplayName("El filtro excluye múltiples hechos eliminados correctamente")
  public void filtroExcluyeMultiplesHechos() {
    Hecho hecho1 = crearHechoCompleto("h1");
    Hecho hecho2 = crearHechoCompleto("h2");
    Hecho hecho3 = crearHechoCompleto("h3");
    repoHechos.save(hecho1);
    repoHechos.save(hecho2);
    repoHechos.save(hecho3);

    List<Hecho> hechosOriginales = List.of(hecho1, hecho2, hecho3);

    gestor.crearSolicitud(hecho1, motivoLargo, detectorSpam);
    // 4. AÑADIMOS FLUSH
    entityManager().flush();
    Solicitud solicitud1 = repositorio.buscarPorHechoYRazon(hecho1, motivoLargo)
                                      .orElseThrow();
    gestor.gestionarSolicitud(solicitud1, AceptarSolicitud.ACEPTAR);

    gestor.crearSolicitud(hecho3, motivoLargo, detectorSpam);
    // 4. AÑADIMOS FLUSH
    entityManager().flush();
    Solicitud solicitud3 = repositorio.buscarPorHechoYRazon(hecho3, motivoLargo)
                                      .orElseThrow();
    gestor.gestionarSolicitud(solicitud3, AceptarSolicitud.ACEPTAR);

    List<Hecho> filtrados = gestor.filtroExcluyenteDeHechosEliminados()
                                  .filtrar(hechosOriginales);

    assertFalse(filtrados.contains(hecho1));
    assertTrue(filtrados.contains(hecho2));
    assertFalse(filtrados.contains(hecho3));
  }
}