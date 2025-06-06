package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import ar.edu.utn.frba.dds.domain.exceptions.RazonInvalidaException;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import ar.edu.utn.frba.dds.domain.reportes.Solicitud;
import ar.edu.utn.frba.dds.usuario.Usuario;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

public class SolicitudTest {

  PuntoGeografico pg = new PuntoGeografico(33.0, 44.0);
  LocalDateTime hora = LocalDateTime.now();
  List<String> etiquetas = List.of("#robo");
  Hecho hechoValido = new Hecho(
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
  Usuario solicitante = mock(Usuario.class);


  @Test
  public void crearSolicitudValida() {
    String motivoLargo = "a".repeat(500);
    Solicitud solicitud = new Solicitud(solicitante, hechoValido, motivoLargo);
    assertNotNull(solicitud);
  }

  @Test
  public void lanzaExcepcionSiRazonEsMuyCorta() {
    assertThrows(
        RazonInvalidaException.class, () -> new Solicitud(solicitante, hechoValido, "corta")
    );
  }

  @Test
  public void lanzaExcepcionSiRazonEsNula() {
    assertThrows(
        RazonInvalidaException.class, () -> new Solicitud(solicitante, hechoValido, null)
    );
  }

  @Test
  public void lanzaExcepcionSiRazonEstaVacia() {
    assertThrows(
        RazonInvalidaException.class, () -> new Solicitud(solicitante, hechoValido, "     ")
    );
  }
}