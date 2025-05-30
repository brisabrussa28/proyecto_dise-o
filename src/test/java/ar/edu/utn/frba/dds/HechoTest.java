package ar.edu.utn.frba.dds;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.main.Contribuyente; //ELIMINAR
import java.time.ZoneId;
import java.util.Date;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class HechoTest {

  Contribuyente contribuyenteA = new Contribuyente(null, null);
  PuntoGeografico pgAux = new PuntoGeografico(33.39627891281455, 44.48695991794239);
  FuenteDinamica fuenteAuxD = new FuenteDinamica("Julio Cesar", null);
  List<String> etiquetasAux = List.of(
      "#ancianita",
      "#robo_a_mano_armada",
      "#violencia",
      "#leyDeProtecciónALasAncianitas",
      "#NOalaVIOLENCIAcontraABUELITAS"
  );

  @Test
  public void hechoCreadoCorrectamente() {
    Hecho hechoTest = contribuyenteA.crearHecho(
        "Robo",
        "Hombre blanco asalta ancianita indefensa",
        "ROBO",
        "Avenida Siempreviva 742",
        pgAux, // Ubicación
        Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()), // Fecha
        etiquetasAux,  // Etiquetas
        fuenteAuxD // Fuente
    );

    boolean igual = (
        Objects.equals(hechoTest.getTitulo(), "Robo") &&
            Objects.equals(hechoTest.getDescripcion(), "Hombre blanco asalta ancianita indefensa") &&
            Objects.equals(hechoTest.getCategoria(), "ROBO") &&
            Objects.equals(hechoTest.getDireccion(), "Avenida Siempreviva 742") &&
            hechoTest.getUbicacion() == pgAux &&
            Objects.equals(hechoTest.getFechaSuceso(), Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant())) &&
            Objects.equals(hechoTest.getFechaCarga(), Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant())) &&
            hechoTest.getEtiquetas().size() == etiquetasAux.size() &&
            hechoTest.getOrigen() == Origen.PROVISTO_CONTRIBUYENTE
    );
    assertTrue(igual); // Si "igual" es true es que están correctos los datos
  }

  @Test
  public void direccionIdentica() {
    Hecho hecho = new Hecho("titulo", "Un día más siendo del conurbano", "Robos", "dire", pgAux, Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()), Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()), null, etiquetasAux);
    assertFalse(hecho.sucedioEn("Mozart 2300"));
  }
}