package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ar.edu.utn.frba.dds.domain.exceptions.RazonInvalidaException;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.HechoBuilder;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.hecho.Origen;
import ar.edu.utn.frba.dds.domain.reportes.EstadoSolicitud;
import ar.edu.utn.frba.dds.domain.reportes.Solicitud;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class SolicitudTest {

  private Hecho hechoValido;
  private final String motivoLargo = "a".repeat(501);

  @BeforeEach
  public void setUp() {
    hechoValido = new HechoBuilder()
        .conTitulo("titulo")
        .conDescripcion("desc")
        .conCategoria("Robos")
        .conDireccion("direccion")
        .conProvincia("CABA")
        .conUbicacion(new PuntoGeografico(33.0, 44.0))
        .conFechaSuceso(LocalDateTime.now())
        .conFechaCarga(LocalDateTime.now())
        .conFuenteOrigen(Origen.PROVISTO_CONTRIBUYENTE)
        .conEtiquetas(List.of("#robo"))
        .build();
  }

  @Test
  @DisplayName("Se puede crear una solicitud válida y su estado inicial es PENDIENTE")
  public void crearSolicitudValida() {
    // AHORA: El constructor ya no requiere un UUID
    Solicitud solicitud = new Solicitud(hechoValido, motivoLargo);
    assertNotNull(solicitud);
    assertEquals(EstadoSolicitud.PENDIENTE, solicitud.getEstado());
  }

  @Test
  @DisplayName("Lanza excepción si la razón es muy corta")
  public void lanzaExcepcionSiRazonEsMuyCorta() {
    assertThrows(
        RazonInvalidaException.class, () -> new Solicitud(hechoValido, "corta")
    );
  }

  @Test
  @DisplayName("Lanza excepción si la razón es nula")
  public void lanzaExcepcionSiRazonEsNula() {
    // La validación de null ahora está separada en el constructor
    assertThrows(
        RazonInvalidaException.class, () -> new Solicitud(hechoValido, null)
    );
  }

  @Test
  @DisplayName("Lanza excepción si el hecho es nulo")
  public void lanzaExcepcionSiHechoEsNulo() {
    assertThrows(
        NullPointerException.class, () -> new Solicitud(null, motivoLargo)
    );
  }

  // --- Tests de cambio de estado ---

  @Test
  @DisplayName("Marcar como spam cambia el estado a SPAM")
  public void marcarComoSpamCambiaElEstado() {
    Solicitud solicitud = new Solicitud(hechoValido, motivoLargo);
    solicitud.marcarComoSpam();
    assertEquals(EstadoSolicitud.SPAM, solicitud.getEstado());
  }

  @Test
  @DisplayName("Aceptar la solicitud cambia el estado a ACEPTADA")
  public void aceptarCambiaElEstado() {
    Solicitud solicitud = new Solicitud(hechoValido, motivoLargo);
    solicitud.aceptar();
    assertEquals(EstadoSolicitud.ACEPTADA, solicitud.getEstado());
  }

  @Test
  @DisplayName("Rechazar la solicitud cambia el estado a RECHAZADA")
  public void rechazarCambiaElEstado() {
    Solicitud solicitud = new Solicitud(hechoValido, motivoLargo);
    solicitud.rechazar();
    assertEquals(EstadoSolicitud.RECHAZADA, solicitud.getEstado());
  }
}
