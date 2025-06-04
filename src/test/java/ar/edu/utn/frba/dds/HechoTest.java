package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import ar.edu.utn.frba.dds.domain.rol.Rol;
import ar.edu.utn.frba.dds.main.Usuario;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class HechoTest {

  Usuario contribuyenteA = new Usuario(null, null, Set.of(Rol.CONTRIBUYENTE));
  Usuario UsuarioRegistrado = new Usuario("Juan", "juan@mail.com");
  LocalDateTime hace3Dias = LocalDateTime.now()
                                         .minusDays(3);
  LocalDateTime hace10Dias = LocalDateTime.now()
                                          .minusDays(10);
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
    LocalDateTime fechaSuceso = LocalDateTime.from(LocalDateTime.now()
                                                                .minusDays(5)
                                                                .atZone(ZoneId.systemDefault()));
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
    assertEquals(
        hechoTest.getFechaCarga()
                 .getHour(), fechaCarga.getHour()
    );
    assertEquals(
        hechoTest.getFechaCarga()
                 .getMinute(), fechaCarga.getMinute()
    );
    assertEquals(
        hechoTest.getEtiquetas()
                 .size(), etiquetasAux.size()
    );
    assertEquals(Origen.PROVISTO_CONTRIBUYENTE, hechoTest.getOrigen());
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
        etiquetasAux
    );
    assertFalse(hecho.sucedioEn("Mozart 2300"));
  }

  @Test
  public void unHechoEsEditableAntesDeLaSemana() {
    Hecho hecho = new Hecho(
        "Choque",
        "Choque en autopista",
        "Transito",
        "Av. Siempreviva",
        pgAux,
        hace3Dias,
        hace3Dias,
        Origen.PROVISTO_CONTRIBUYENTE,
        etiquetasAux,
        UsuarioRegistrado.getID()
    );

    boolean editable = hecho.esEditablePor(UsuarioRegistrado.getID());
    assertTrue(editable, "Debería ser editable dentro de la primera semana.");
  }

  @Test
  public void unHechoNoEsEditablePasadaLaSemana() {
    Hecho hecho = new Hecho(
        "Incendio",
        "Incendio en fábrica",
        "Emergencia",
        "Calle Falsa 123",
        pgAux,
        hace10Dias,
        hace10Dias,
        Origen.PROVISTO_CONTRIBUYENTE,
        etiquetasAux,
        UsuarioRegistrado.getID()
    );

    boolean editable = hecho.esEditablePor(UsuarioRegistrado.getID());
    assertFalse(editable, "No debería ser editable pasada una semana desde su carga.");
  }

  @Test
  public void unHechoNoEsEditablePorUsuarioQueNoLoCreo() {
    Hecho hecho = new Hecho(
        "Corte de luz",
        "Zona sin energía eléctrica",
        "Servicios",
        "Barrio Norte",
        pgAux,
        hace3Dias,
        hace3Dias,
        Origen.PROVISTO_CONTRIBUYENTE,
        etiquetasAux,
        UsuarioRegistrado.getID()
    );

    boolean editable = hecho.esEditablePor(contribuyenteA.getID());
    assertFalse(editable, "No debería ser editable por otro usuario que no lo creó.");
  }

  @Test
  public void unHechoSeEditoCorrectamente() {
    Hecho hecho = new Hecho(
        "Inundación",
        "Calles anegadas por tormenta",
        "Clima",
        "Zona Sur",
        pgAux,
        hace3Dias,
        hace3Dias,
        Origen.PROVISTO_CONTRIBUYENTE,
        etiquetasAux,
        UsuarioRegistrado.getID()
    );

    String nuevoTitulo = "Inundación grave";
    String nuevaDescripcion = "Calles completamente inundadas";
    String nuevaCategoria = "Emergencia";
    String nuevaDireccion = "Zona Sur extendida";
    PuntoGeografico nuevaUbicacion = new PuntoGeografico(10.0, 20.0);
    List<String> nuevasEtiquetas = Arrays.asList("clima", "alerta");
    LocalDateTime nuevaFechaSuceso = LocalDateTime.now();

    boolean editado = hecho.editarHecho(
        UsuarioRegistrado.getID(),
        nuevoTitulo,
        nuevaDescripcion,
        nuevaCategoria,
        nuevaDireccion,
        nuevaUbicacion,
        nuevasEtiquetas,
        nuevaFechaSuceso
    );

    assertTrue(editado, "El hecho debería haberse editado correctamente.");
    assertEquals(nuevoTitulo, hecho.getTitulo());
    assertEquals(nuevaDescripcion, hecho.getDescripcion());
    assertEquals(nuevaCategoria, hecho.getCategoria());
    assertEquals(nuevaDireccion, hecho.getDireccion());
    assertEquals(nuevaUbicacion, hecho.getUbicacion());
    assertEquals(nuevasEtiquetas, hecho.getEtiquetas());
    assertEquals(nuevaFechaSuceso, hecho.getFechaSuceso());
  }
}