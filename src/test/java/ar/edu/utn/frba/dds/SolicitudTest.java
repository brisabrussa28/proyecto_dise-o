package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ar.edu.utn.frba.dds.domain.exceptions.RazonInvalidaException;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.HechoBuilder;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import ar.edu.utn.frba.dds.domain.reportes.Solicitud;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class SolicitudTest {

  PuntoGeografico pg = new PuntoGeografico(33.0, 44.0);
  LocalDateTime hora = LocalDateTime.now();
  List<String> etiquetas = List.of("#robo");
  Hecho hechoValido = new HechoBuilder()
      .conTitulo("titulo")
      .conDescripcion("desc")
      .conCategoria("Robos")
      .conDireccion("direccion")
      .conProvincia("CABA")
      .conUbicacion(pg)
      .conFechaSuceso(hora)
      .conFechaCarga(hora)
      .conFuenteOrigen(Origen.PROVISTO_CONTRIBUYENTE)
      .conEtiquetas(etiquetas)
      .build();


  @Test
  public void crearSolicitudValida() {
    String motivoLargo = "a".repeat(500);
    Solicitud solicitud = new Solicitud(UUID.randomUUID(), hechoValido, motivoLargo);
    assertNotNull(solicitud);
  }

  @Test
  public void lanzaExcepcionSiRazonEsMuyCorta() {
    assertThrows(
        RazonInvalidaException.class, () -> new Solicitud(UUID.randomUUID(), hechoValido, "corta")
    );
  }

  @Test
  public void lanzaExcepcionSiRazonEsNula() {
    assertThrows(
        RazonInvalidaException.class, () -> new Solicitud(UUID.randomUUID(), hechoValido, null)
    );
  }

  @Test
  public void lanzaExcepcionSiRazonEstaVacia() {
    assertThrows(
        RazonInvalidaException.class, () -> new Solicitud(UUID.randomUUID(), hechoValido, "     ")
    );
  }
}
