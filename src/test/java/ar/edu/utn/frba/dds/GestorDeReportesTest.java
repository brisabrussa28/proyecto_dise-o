package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.domain.detectorspam.DetectorSpam;
import ar.edu.utn.frba.dds.domain.exceptions.SolicitudInexistenteException;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import ar.edu.utn.frba.dds.domain.reportes.GestorDeReportes;
import ar.edu.utn.frba.dds.domain.reportes.Solicitud;
import ar.edu.utn.frba.dds.usuario.Usuario;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GestorDeReportesTest {

  PuntoGeografico pg = new PuntoGeografico(33.0, 44.0);
  LocalDateTime hora = LocalDateTime.now();
  List<String> etiquetas = List.of("#robo");
  Hecho hecho = new Hecho(
      "titulo",
      "desc",
      "Robos",
      "direccion",
      pg,
      hora,
      hora,
      Origen.PROVISTO_CONTRIBUYENTE,
      etiquetas
  );
  private GestorDeReportes gestor;
  private Usuario solicitante;
  DetectorSpam detector;

  @BeforeEach
  public void setUp() {
    detector = mock(DetectorSpam.class);
    gestor = new GestorDeReportes(detector);
    solicitante = mock(Usuario.class);
  }

  @Test
  public void agregarYContarSolicitudes() {
    Solicitud solicitud = new Solicitud(solicitante, hecho, "motivo".repeat(100));
    gestor.agregarSolicitud(solicitud);
    assertEquals(1, gestor.cantidadSolicitudes());
  }

  @Test
  public void agregarDosSolicitudesDistintas() {
    gestor.agregarSolicitud(new Solicitud(solicitante, hecho, "motivo1".repeat(100)));
    gestor.agregarSolicitud(new Solicitud(solicitante, hecho, "motivo2".repeat(100)));
    assertEquals(2, gestor.cantidadSolicitudes());
  }

  @Test
  public void agregarDosSolicitudesIgualesSoloAgregaUna() {
    Solicitud solicitud = new Solicitud(solicitante, hecho, "motivo".repeat(100));
    gestor.agregarSolicitud(solicitud);
    gestor.agregarSolicitud(solicitud);
    assertEquals(1, gestor.cantidadSolicitudes());
  }

  @Test
  public void noAgregaSolicitudSiEsSpam() {
    when(detector.esSpam(anyString())).thenReturn(true);
    Solicitud solicitudSpam = new Solicitud(solicitante, hecho, "motivo".repeat(100));
    gestor.agregarSolicitud(solicitudSpam);
    verify(detector).esSpam(anyString());

    assertEquals(0, gestor.cantidadSolicitudes());
  }

  @Test
  public void agregarSolicitudNullLanzaExcepcion() {
    assertThrows(
        NullPointerException.class, () -> gestor.agregarSolicitud(null)
    );
  }

  @Test
  public void gestionarSolicitudInexistenteLanzaExcepcion() {
    Solicitud solicitudFalsa = mock(Solicitud.class);
    assertThrows(
        SolicitudInexistenteException.class, () -> gestor.gestionarSolicitud(solicitudFalsa, true)
    );
  }

  @Test
  public void marcarComoEliminadoNullLanzaExcepcion() {
    assertThrows(
        NullPointerException.class, () -> gestor.marcarComoEliminado(null)
    );
  }

  @Test
  public void hechoMarcadoComoEliminadoApareceEnListaDeEliminados() {
    gestor.marcarComoEliminado(hecho);
    List<Hecho> eliminados = gestor.hechosEliminados();
    assertEquals(1, eliminados.size());
    assertTrue(eliminados.contains(hecho));
  }

  @Test
  public void filtroExcluyenteNoIncluyeHechosEliminados() {
    Hecho hecho1 = new Hecho("t1", "d1", "c1", "dir1", pg, hora, hora, Origen.PROVISTO_CONTRIBUYENTE, etiquetas);
    Hecho hecho2 = new Hecho("t2", "d2", "c2", "dir2", pg, hora, hora, Origen.PROVISTO_CONTRIBUYENTE, etiquetas);

    gestor.marcarComoEliminado(hecho1);

    List<Hecho> hechos = List.of(hecho1, hecho2);
    List<Hecho> filtrados = gestor.filtroExcluyente()
                                  .filtrar(hechos);

    assertFalse(filtrados.contains(hecho1));
    assertTrue(filtrados.contains(hecho2));
    assertEquals(1, filtrados.size());
  }

}