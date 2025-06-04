package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.domain.detectorspam.DetectorSpam;
import ar.edu.utn.frba.dds.domain.exceptions.RazonInvalidaException;
import ar.edu.utn.frba.dds.domain.exceptions.SolicitudInexistenteException;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import ar.edu.utn.frba.dds.domain.reportes.GestorDeReportes;
import ar.edu.utn.frba.dds.domain.reportes.Solicitud;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SolicitudTest {
  PuntoGeografico pgAux = new PuntoGeografico(33.39627891281455, 44.48695991794239);
  LocalDateTime horaAux = LocalDateTime.of(2025, 5, 6, 20, 9);

  List<String> etiquetasAux = List.of(
      "#ancianita",
      "#robo_a_mano_armada",
      "#violencia",
      "#leyDeProtecciónALasAncianitas",
      "#NOalaVIOLENCIAcontraABUELITAS"
  );
  private GestorDeReportes gestor;
  private DetectorSpam detectorSpam;

  @BeforeEach
  void initFileSystem() {
    detectorSpam = mock(DetectorSpam.class);
    gestor = new GestorDeReportes(detectorSpam);
  }

  @Test
  public void solicitarEliminacionDeHechoCorrectamente() {
    when(detectorSpam.esSpam(anyString())).thenReturn(false);
    String motivo = "a".repeat(600);
    Hecho hecho = new Hecho(
        "titulo",
        "desc",
        "Robos",
        "direccion",
        pgAux,
        horaAux,
        horaAux,
        Origen.PROVISTO_CONTRIBUYENTE,
        etiquetasAux
    );
    FuenteDinamica fuente = new FuenteDinamica("MiFuente", null);
    fuente.agregarHecho(hecho);

    Solicitud solicitud = new Solicitud(null, hecho, motivo);
    gestor.agregarSolicitud(solicitud);

    assertEquals(1, gestor.cantidadSolicitudes());
  }

  @Test
  public void gestorDeReportesNoObtieneSolicitudes() {
    when(detectorSpam.esSpam(anyString())).thenReturn(false);
    String motivo = "a".repeat(5); // motivo inválido
    Hecho hecho = new Hecho(
        "titulo",
        "desc",
        "Robos",
        "direccion",
        pgAux,
        horaAux,
        horaAux,
        Origen.PROVISTO_CONTRIBUYENTE,
        etiquetasAux
    );
    FuenteDinamica fuente = new FuenteDinamica("MiFuente", null);
    fuente.agregarHecho(hecho);

    assertThrows(
        RazonInvalidaException.class, () -> new Solicitud(null, hecho, motivo)
    );
  }

  @Test
  public void gestorDeReportesNoTieneSolicitud() {
    when(detectorSpam.esSpam(anyString())).thenReturn(false);
    String motivo = "perú es clave".repeat(50);
    Solicitud solicitud = new Solicitud(null, null, motivo);

    assertThrows(
        SolicitudInexistenteException.class, () -> gestor.gestionarSolicitud(solicitud, true)
    );
  }

  @Test
  public void solicitarEliminacionDeHechoConMotivoSpam() {
    when(detectorSpam.esSpam(anyString())).thenReturn(true);
    String motivo = "Este motivo contiene spam";
    Hecho hecho = new Hecho(
        "titulo",
        "desc",
        "Robos",
        "direccion",
        pgAux,
        horaAux,
        horaAux,
        Origen.PROVISTO_CONTRIBUYENTE,
        etiquetasAux
    );

    assertThrows(
        RazonInvalidaException.class, () -> new Solicitud(null, hecho, motivo)
    );
  }
}
