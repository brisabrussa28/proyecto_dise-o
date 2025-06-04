package ar.edu.utn.frba.dds;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.main.Usuario; //ELIMINAR
import java.time.ZoneId;
import java.util.Date;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class HechoTest {

  Usuario contribuyenteA = new Usuario(null, null);
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
    String titulo = "Robo";
    String descripcion = "Hombre blanco asalta ancianita indefensa";
    String categoria = "ROBO";
    String direccion = "Avenida Siempreviva 742";
    Date fechaSuceso = Date.from(LocalDateTime.now().minusDays(5).atZone(ZoneId.systemDefault()).toInstant());
    LocalDate fechaCarga = LocalDate.now(ZoneId.systemDefault());

    Hecho hechoTest = contribuyenteA.crearHecho(
        titulo,
        descripcion,
        categoria,
        direccion,
        pgAux, // Ubicación
        fechaSuceso, // Fecha
        etiquetasAux,  // Etiquetas
        fuenteAuxD // Fuente
    );

    assertEquals(hechoTest.getTitulo(), "Robo");
    assertEquals(hechoTest.getDescripcion(), "Hombre blanco asalta ancianita indefensa");
    assertEquals(hechoTest.getCategoria(), "ROBO");
    assertEquals(hechoTest.getDireccion(), "Avenida Siempreviva 742");
    assertTrue(hechoTest.getUbicacion() == pgAux);
    assertEquals(hechoTest.getFechaSuceso(), fechaSuceso);
    assertEquals(hechoTest.getFechaCarga().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), fechaCarga);
    assertTrue(hechoTest.getEtiquetas().size() == etiquetasAux.size());
    assertTrue(hechoTest.getOrigen() == Origen.PROVISTO_CONTRIBUYENTE);
  }

  @Test
  public void direccionIdentica() {
    Hecho hecho = new Hecho("titulo", "Un día más siendo del conurbano", "Robos", "dire", pgAux, Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()), Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()), null, etiquetasAux);
    assertFalse(hecho.sucedioEn("Mozart 2300"));
  }
}