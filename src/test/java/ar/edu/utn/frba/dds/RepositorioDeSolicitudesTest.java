package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.domain.reportes.detectorspam.DetectorSpam;
import ar.edu.utn.frba.dds.domain.exceptions.SolicitudInexistenteException;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.HechoBuilder;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.hecho.Origen;
import ar.edu.utn.frba.dds.domain.reportes.AceptarSolicitud;
import ar.edu.utn.frba.dds.domain.reportes.RepositorioDeSolicitudes;
import ar.edu.utn.frba.dds.domain.reportes.Solicitud;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RepositorioDeSolicitudesTest {

  PuntoGeografico pg = new PuntoGeografico(33.0, 44.0);
  LocalDateTime hora = LocalDateTime.now();
  List<String> etiquetas = List.of("#robo");
  Hecho hecho = new HechoBuilder()
      .conTitulo("titulo")
      .conDescripcion("desc")
      .conCategoria("Robos")
      .conDireccion("direccion")
      .conProvincia("Provincia")
      .conUbicacion(pg)
      .conFechaSuceso(hora)
      .conFechaCarga(hora)
      .conFuenteOrigen(Origen.PROVISTO_CONTRIBUYENTE)
      .conEtiquetas(etiquetas)
      .build();
  private RepositorioDeSolicitudes repositorio;
  private UUID solicitante;

  @BeforeEach
  public void setUp() {
    DetectorSpam detector = mock(DetectorSpam.class);
    when(detector.esSpam(anyString())).thenReturn(false);
    repositorio = new RepositorioDeSolicitudes(detector);
    solicitante = UUID.randomUUID();
  }

  @Test
  public void agregarYContarSolicitudes() {
    repositorio.agregarSolicitud(solicitante, hecho, "motivo".repeat(100));
    assertEquals(1, repositorio.cantidadSolicitudes());
  }

  @Test
  public void agregarDosSolicitudesDistintas() {
    repositorio.agregarSolicitud(solicitante, hecho, "motivo1".repeat(100));
    repositorio.agregarSolicitud(solicitante, hecho, "motivo2".repeat(100));
    assertEquals(2, repositorio.cantidadSolicitudes());
  }

  @Test
  public void agregarDosSolicitudesIgualesSoloAgregaUna() {
    repositorio.agregarSolicitud(solicitante, hecho, "motivo".repeat(100));
    repositorio.agregarSolicitud(solicitante, hecho, "motivo".repeat(100));
    assertEquals(1, repositorio.cantidadSolicitudes());
  }

  @Test
  public void noAgregaSolicitudSiEsSpam() {
    DetectorSpam detectorSpamTrue = mock(DetectorSpam.class);
    when(detectorSpamTrue.esSpam(anyString())).thenReturn(true);
    RepositorioDeSolicitudes repositorioSpam = new RepositorioDeSolicitudes(detectorSpamTrue);

    repositorioSpam.agregarSolicitud(solicitante, hecho, "motivo".repeat(100));

    assertEquals(0, repositorioSpam.cantidadSolicitudes());
  }

  @Test
  public void gestionarSolicitudInexistenteLanzaExcepcion() {
    Solicitud solicitudFalsa = mock(Solicitud.class);
    assertThrows(
        SolicitudInexistenteException.class,
        () -> repositorio.gestionarSolicitud(solicitudFalsa, AceptarSolicitud.ACEPTAR)
    );
  }

  @Test
  public void marcarComoEliminadoNullLanzaExcepcion() {
    assertThrows(
        NullPointerException.class, () -> repositorio.marcarComoEliminado(null)
    );
  }

  @Test
  public void hechoMarcadoComoEliminadoApareceEnListaDeEliminados() {
    repositorio.marcarComoEliminado(hecho);
    List<Hecho> eliminados = repositorio.hechosEliminados();
    assertEquals(1, eliminados.size());
    assertTrue(eliminados.contains(hecho));
  }

  @Test
  public void filtroExcluyenteNoIncluyeHechosEliminados() {
    Hecho hecho1 = new HechoBuilder().conTitulo("t1")
                                     .conDescripcion("d1")
                                     .conCategoria("c1")
                                     .conDireccion("dir1")
                                     .conProvincia("p1")
                                     .conUbicacion(pg)
                                     .conFechaSuceso(hora)
                                     .conFechaCarga(hora)
                                     .conFuenteOrigen(Origen.PROVISTO_CONTRIBUYENTE)
                                     .conEtiquetas(etiquetas)
                                     .build();
    Hecho hecho2 = new HechoBuilder().conTitulo("t2")
                                     .conDescripcion("d2")
                                     .conCategoria("c2")
                                     .conDireccion("dir2")
                                     .conProvincia("p2")
                                     .conUbicacion(pg)
                                     .conFechaSuceso(hora)
                                     .conFechaCarga(hora)
                                     .conFuenteOrigen(Origen.PROVISTO_CONTRIBUYENTE)
                                     .conEtiquetas(etiquetas)
                                     .build();

    repositorio.marcarComoEliminado(hecho1);

    List<Hecho> hechos = List.of(hecho1, hecho2);
    List<Hecho> filtrados = repositorio.filtroExcluyente()
                                       .filtrar(hechos);

    assertFalse(filtrados.contains(hecho1));
    assertTrue(filtrados.contains(hecho2));
    assertEquals(1, filtrados.size());
  }

}
