package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.*;

import ar.edu.utn.frba.dds.model.exceptions.RazonInvalidaException;
import ar.edu.utn.frba.dds.model.hecho.Estado;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.model.hecho.HechoBuilder;
import ar.edu.utn.frba.dds.model.info.PuntoGeografico;
import ar.edu.utn.frba.dds.model.hecho.Origen;
import ar.edu.utn.frba.dds.model.reportes.EstadoSolicitud;
import ar.edu.utn.frba.dds.model.reportes.Solicitud;
import ar.edu.utn.frba.dds.model.usuario.Rol;
import ar.edu.utn.frba.dds.model.usuario.Usuario;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class SolicitudTest {

  private Hecho hechoValido;
  private Usuario usuarioValido;
  private final String motivoValido = "a".repeat(600); // dentro del rango
  private final String motivoCorto = "a".repeat(100);
  private final String motivoLargo = "a".repeat(1100);

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

    usuarioValido = new Usuario("mail@test.com", "usuarioTest", null, "password123", Rol.CONTRIBUYENTE);
  }

  @Test
  @DisplayName("Se puede crear una solicitud válida y su estado inicial es PENDIENTE")
  public void crearSolicitudValida() {
    Solicitud solicitud = new Solicitud(hechoValido, motivoValido, usuarioValido);
    assertNotNull(solicitud);
    assertEquals(EstadoSolicitud.PENDIENTE, solicitud.getEstado());
    assertNotNull(solicitud.getFechaReporte());
  }

  @Test
  @DisplayName("Lanza excepción si la razón es muy corta")
  public void lanzaExcepcionSiRazonEsMuyCorta() {
    assertThrows(RazonInvalidaException.class,
                 () -> new Solicitud(hechoValido, motivoCorto, usuarioValido));
  }

  @Test
  @DisplayName("Lanza excepción si la razón es muy larga")
  public void lanzaExcepcionSiRazonEsMuyLarga() {
    assertThrows(RazonInvalidaException.class,
                 () -> new Solicitud(hechoValido, motivoLargo, usuarioValido));
  }

  @Test
  @DisplayName("Lanza excepción si la razón es nula")
  public void lanzaExcepcionSiRazonEsNula() {
    assertThrows(RazonInvalidaException.class,
                 () -> new Solicitud(hechoValido, null, usuarioValido));
  }

  @Test
  @DisplayName("Lanza excepción si el hecho es nulo")
  public void lanzaExcepcionSiHechoEsNulo() {
    assertThrows(NullPointerException.class,
                 () -> new Solicitud(null, motivoValido, usuarioValido));
  }

  @Test
  @DisplayName("Lanza excepción si el usuario es nulo")
  public void lanzaExcepcionSiUsuarioEsNulo() {
    assertThrows(IllegalArgumentException.class,
                 () -> new Solicitud(hechoValido, motivoValido, null));
  }

  @Test
  @DisplayName("Marcar como spam cambia el estado a SPAM")
  public void marcarComoSpamCambiaElEstado() {
    Solicitud solicitud = new Solicitud(hechoValido, motivoValido, usuarioValido);
    solicitud.marcarComoSpam();
    assertEquals(EstadoSolicitud.SPAM, solicitud.getEstado());
  }

  @Test
  @DisplayName("Aceptar la solicitud cambia el estado a ACEPTADA y el hecho a ELIMINADO")
  public void aceptarCambiaElEstadoYHecho() {
    Solicitud solicitud = new Solicitud(hechoValido, motivoValido, usuarioValido);
    solicitud.aceptar();
    assertEquals(EstadoSolicitud.ACEPTADA, solicitud.getEstado());
    assertEquals(Estado.ELIMINADO, hechoValido.getEstado());
  }

  @Test
  @DisplayName("Rechazar la solicitud cambia el estado a RECHAZADA")
  public void rechazarCambiaElEstado() {
    Solicitud solicitud = new Solicitud(hechoValido, motivoValido, usuarioValido);
    solicitud.rechazar();
    assertEquals(EstadoSolicitud.RECHAZADA, solicitud.getEstado());
  }

  @Test
  @DisplayName("Dos solicitudes con mismo hecho y motivo son iguales")
  public void equalsFuncionaCorrectamente() {
    Solicitud s1 = new Solicitud(hechoValido, motivoValido, usuarioValido);
    Solicitud s2 = new Solicitud(hechoValido, motivoValido, usuarioValido);
    assertEquals(s1, s2);
    assertEquals(s1.hashCode(), s2.hashCode());
  }
}