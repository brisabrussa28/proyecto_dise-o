package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import ar.edu.utn.frba.dds.main.Usuario;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;

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
    LocalDateTime fechaSuceso = LocalDateTime.from(LocalDateTime.now().minusDays(5).atZone(ZoneId.systemDefault()).toInstant());
    LocalDateTime fechaCarga = LocalDateTime.now();

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

    assertEquals("Robo", hechoTest.getTitulo());
    assertEquals("Hombre blanco asalta ancianita indefensa", hechoTest.getDescripcion());
    assertEquals("ROBO", hechoTest.getCategoria());
    assertEquals("Avenida Siempreviva 742", hechoTest.getDireccion());
    assertEquals(hechoTest.getUbicacion(), pgAux);
    assertEquals(hechoTest.getFechaSuceso(), fechaSuceso);
    assertEquals(hechoTest.getFechaCarga().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(), fechaCarga);
    assertEquals(hechoTest.getEtiquetas().size(), etiquetasAux.size());
    assertEquals(hechoTest.getOrigen(), Origen.PROVISTO_CONTRIBUYENTE);
  }

  @Test
  public void direccionIdentica() {
    Hecho hecho = new Hecho(
        "titulo",
        "Un día más siendo del conurbano",
        "Robos",
        "dire",
        pgAux,
        LocalDateTime.now(),/*Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()), Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant())*/
        LocalDateTime.now(),
        null,
        etiquetasAux);
    assertFalse(hecho.sucedioEn("Mozart 2300"));
  }
}