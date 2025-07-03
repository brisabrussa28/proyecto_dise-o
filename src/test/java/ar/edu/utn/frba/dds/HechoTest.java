package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ar.edu.utn.frba.dds.domain.filtro.FiltroIgualHecho;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

public class HechoTest {

  @Test
  public void seCreaHechoCorrectamente() {
    FuenteDinamica fuente = new FuenteDinamica("Fuente X", null);
    PuntoGeografico ubicacion = new PuntoGeografico(33.0, 44.0);
    List<String> etiquetas = List.of("#robo", "#violencia");
    LocalDateTime fechaSuceso = LocalDateTime.now()
                                             .minusDays(5);

    Hecho hecho = fuente.crearHecho(
        "Robo",
        "Robo a mano armada",
        "DELITO",
        "Calle falsa 123",
        ubicacion,
        fechaSuceso,
        etiquetas
    );

    assertEquals("Robo", hecho.getTitulo());
    assertEquals("Robo a mano armada", hecho.getDescripcion());
    assertEquals("DELITO", hecho.getCategoria());
    assertEquals("Calle falsa 123", hecho.getDireccion());
    assertEquals(ubicacion, hecho.getUbicacion());
    assertEquals(fechaSuceso, hecho.getFechaSuceso());
    assertEquals(etiquetas, hecho.getEtiquetas());
    assertEquals(Origen.PROVISTO_CONTRIBUYENTE, hecho.getOrigen());
    assertNotNull(hecho.getFechaCarga());
  }

  @Test
  public void filtroDetectaHechoIdentico() {
    LocalDateTime fecha = LocalDateTime.now()
                                       .minusDays(2);
    PuntoGeografico ubicacion = new PuntoGeografico(1.0, 1.0);
    List<String> etiquetas = List.of("#etiqueta");

    Hecho original = new Hecho(
        "titulo",
        "desc",
        "categoria",
        "direccion",
        ubicacion,
        fecha,
        fecha,
        Origen.PROVISTO_CONTRIBUYENTE,
        etiquetas
    );
    Hecho copia = new Hecho(
        "titulo",
        "descX",
        "categoria",
        "direccion",
        ubicacion,
        fecha,
        fecha,
        Origen.PROVISTO_CONTRIBUYENTE,
        List.of("#otra")
    );

    FiltroIgualHecho filtro = new FiltroIgualHecho(original);

    List<Hecho> filtrados = filtro.filtrar(List.of(copia));

    assertEquals(1, filtrados.size());
    assertEquals(copia, filtrados.get(0));
  }

  @Test
  public void filtroNoDetectaHechoDistinto() {
    LocalDateTime fecha = LocalDateTime.now()
                                       .minusDays(2);
    PuntoGeografico ubicacion = new PuntoGeografico(1.0, 1.0);

    Hecho original = new Hecho(
        "Robo",
        "desc",
        "DELITO",
        "Calle X",
        ubicacion,
        fecha,
        fecha,
        Origen.PROVISTO_CONTRIBUYENTE,
        List.of()
    );
    Hecho distinto = new Hecho(
        "Incendio",
        "otra",
        "ACCIDENTE",
        "Calle Y",
        ubicacion,
        fecha,
        fecha,
        Origen.PROVISTO_CONTRIBUYENTE,
        List.of()
    );

    FiltroIgualHecho filtro = new FiltroIgualHecho(original);

    List<Hecho> resultado = filtro.filtrar(List.of(distinto));

    assertTrue(resultado.isEmpty());
  }

  @Test
  public void lanzaExcepcionSiFechaSucesoEsPosteriorAFechaCarga() {
    PuntoGeografico ubicacion = new PuntoGeografico(1.0, 1.0);
    List<String> etiquetas = List.of("#test");
    LocalDateTime fechaSuceso = LocalDateTime.now();
    LocalDateTime fechaCarga = fechaSuceso.minusDays(1); // incorrecto

    assertThrows(
        RuntimeException.class,
        () -> new Hecho(
            "t",
            "d",
            "c",
            "dir",
            ubicacion,
            fechaSuceso,
            fechaCarga,
            Origen.PROVISTO_CONTRIBUYENTE,
            etiquetas
        )
    );
  }

  @Test
  public void lanzaExcepcionSiFechaSucesoEsFutura() {
    PuntoGeografico ubicacion = new PuntoGeografico(1.0, 1.0);
    List<String> etiquetas = List.of("#test");
    LocalDateTime fechaFutura = LocalDateTime.now()
                                             .plusDays(1);

    assertThrows(
        RuntimeException.class,
        () -> new Hecho(
            "t",
            "d",
            "c",
            "dir",
            ubicacion,
            fechaFutura,
            LocalDateTime.now(),
            Origen.PROVISTO_CONTRIBUYENTE,
            etiquetas
        )
    );
  }

  @Test
  public void lanzaExcepcionSiFechaCargaEsFutura() {
    PuntoGeografico ubicacion = new PuntoGeografico(1.0, 1.0);
    List<String> etiquetas = List.of("#test");
    LocalDateTime fechaFutura = LocalDateTime.now()
                                             .plusDays(1);

    assertThrows(
        RuntimeException.class,
        () -> new Hecho(
            "t",
            "d",
            "c",
            "dir",
            ubicacion,
            LocalDateTime.now(),
            fechaFutura,
            Origen.PROVISTO_CONTRIBUYENTE,
            etiquetas
        )
    );
  }

  @Test
  public void hechoValidoNoLanzaExcepcion() {
    PuntoGeografico ubicacion = new PuntoGeografico(1.0, 1.0);
    List<String> etiquetas = List.of("#test");
    assertDoesNotThrow(() -> {
      new Hecho(
          "ok",
          "desc",
          "cat",
          "dir",
          ubicacion,
          LocalDateTime.now()
                       .minusDays(2),
          LocalDateTime.now()
                       .minusDays(1),
          Origen.PROVISTO_CONTRIBUYENTE,
          etiquetas
      );
    });
  }


  @Test
  public void esEditableAntesDeUnaSemana() {
    LocalDateTime hace3Dias = LocalDateTime.now().minusDays(3);
    Hecho hecho = new Hecho(
        "titulo",
        "desc",
        "cat",
        "dir",
        new PuntoGeografico(1,1),
        hace3Dias,
        hace3Dias,
        Origen.DATASET,
        List.of("etiqueta")
    );

    assertTrue(hecho.esEditable(), "El hecho debería ser editable antes de una semana");
  }

  @Test
  public void noEsEditablePasadaUnaSemana() {
    LocalDateTime hace10Dias = LocalDateTime.now().minusDays(10);
    Hecho hecho = new Hecho(
        "titulo",
        "desc",
        "cat",
        "dir",
        new PuntoGeografico(1,1),
        hace10Dias,
        hace10Dias,
        Origen.DATASET,
        List.of("etiqueta")
    );

    assertFalse(hecho.esEditable(), "El hecho NO debería ser editable después de una semana");
  }

}