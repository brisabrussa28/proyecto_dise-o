package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ar.edu.utn.frba.dds.domain.filtro.FiltroPredicado;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.HechoBuilder;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

public class HechoTest {

  @Test
  public void seCreaHechoCorrectamente() {
    // FuenteDinamica fuente = new FuenteDinamica("Fuente X", null); // Ya no es necesaria para crear el hecho
    PuntoGeografico ubicacion = new PuntoGeografico(33.0, 44.0);
    List<String> etiquetas = List.of("#robo", "#violencia");
    LocalDateTime fechaSuceso = LocalDateTime.now().minusDays(5);
    LocalDateTime fechaCarga = LocalDateTime.now();

    Hecho hecho = new HechoBuilder()
        .conTitulo("Robo")
        .conDescripcion("Robo a mano armada")
        .conCategoria("DELITO")
        .conDireccion("Calle falsa 123")
        // provincia se omite para probar el constructor
        .conUbicacion(ubicacion)
        .conFechaSuceso(fechaSuceso)
        .conFechaCarga(fechaCarga)
        .conFuenteOrigen(Origen.PROVISTO_CONTRIBUYENTE)
        .conEtiquetas(etiquetas)
        .build();

    assertEquals("Robo", hecho.getTitulo());
    assertEquals("Robo a mano armada", hecho.getDescripcion());
    assertEquals("DELITO", hecho.getCategoria());
    assertEquals("Calle falsa 123", hecho.getDireccion());
    assertEquals(ubicacion, hecho.getUbicacion());
    assertEquals(fechaSuceso, hecho.getFechaSuceso());
    assertEquals(Origen.PROVISTO_CONTRIBUYENTE, hecho.getOrigen());
    assertNotNull(hecho.getFechaCarga());
  }

  @Test
  public void filtroDetectaHechoIdentico() {
    LocalDateTime fecha = LocalDateTime.now().minusDays(2);
    PuntoGeografico ubicacion = new PuntoGeografico(1.0, 1.0);

    Hecho original = new HechoBuilder()
        .conTitulo("titulo")
        .conDescripcion("desc")
        .conCategoria("categoria")
        .conDireccion("direccion")
        .conProvincia("prov")
        .conUbicacion(ubicacion)
        .conFechaSuceso(fecha)
        .conFechaCarga(fecha)
        .conFuenteOrigen(Origen.PROVISTO_CONTRIBUYENTE)
        .conEtiquetas(List.of("#etiqueta"))
        .build();

    Hecho copia = new HechoBuilder()
        .conTitulo("titulo")
        .conDescripcion("desc")
        .conCategoria("categoria")
        .conDireccion("direccion")
        .conProvincia("prov")
        .conUbicacion(ubicacion)
        .conFechaSuceso(fecha)
        .conFechaCarga(fecha)
        .conFuenteOrigen(Origen.PROVISTO_CONTRIBUYENTE)
        .conEtiquetas(List.of("#otra"))
        .build();


    FiltroPredicado filtro = new FiltroPredicado(h -> h.equals(original));
    List<Hecho> filtrados = filtro.filtrar(List.of(copia));

    // La lógica de equals de Hecho solo considera titulo, descripcion, categoria, direccion, ubicacion y fechaSuceso.
    // Aunque la descripción y las etiquetas son diferentes, el filtro podría considerarlos "idénticos"
    // si la implementación de FiltroIgualHecho solo compara los campos que se usan en el equals de Hecho.
    // Si la intención es que sean idénticos en todos los aspectos para el filtro, se debería ajustar el mock o la lógica del filtro.
    // Para este test, asumo que el filtro se basa en el método equals de Hecho.
    assertEquals(1, filtrados.size());
    assertEquals(copia, filtrados.get(0));
  }

  @Test
  public void filtroNoDetectaHechoDistinto() {
    LocalDateTime fecha = LocalDateTime.now().minusDays(2);
    PuntoGeografico ubicacion = new PuntoGeografico(1.0, 1.0);

    Hecho original = new HechoBuilder()
        .conTitulo("Robo")
        .conDescripcion("desc")
        .conCategoria("DELITO")
        .conDireccion("Calle X")
        .conProvincia("prov")
        .conUbicacion(ubicacion)
        .conFechaSuceso(fecha)
        .conFechaCarga(fecha)
        .conFuenteOrigen(Origen.PROVISTO_CONTRIBUYENTE)
        .build();

    Hecho distinto = new HechoBuilder()
        .conTitulo("Incendio")
        .conDescripcion("otra")
        .conCategoria("ACCIDENTE")
        .conDireccion("Calle Y")
        .conProvincia("prov")
        .conUbicacion(ubicacion)
        .conFechaSuceso(fecha)
        .conFechaCarga(fecha)
        .conFuenteOrigen(Origen.PROVISTO_CONTRIBUYENTE)
        .build();


    FiltroPredicado filtro = new FiltroPredicado(h -> h.equals(original));
    List<Hecho> resultado = filtro.filtrar(List.of(distinto));

    assertTrue(resultado.isEmpty());
  }

  @Test
  public void lanzaExcepcionSiFechaSucesoEsPosteriorAFechaCarga() {
    LocalDateTime fechaSuceso = LocalDateTime.now();
    LocalDateTime fechaCarga = fechaSuceso.minusDays(1); // incorrecto

    assertThrows(RuntimeException.class, () -> new HechoBuilder()
        .conTitulo("t")
        .conDescripcion("d")
        .conCategoria("c")
        .conDireccion("dir")
        .conProvincia("p")
        .conUbicacion(new PuntoGeografico(1.0, 1.0))
        .conFechaSuceso(fechaSuceso)
        .conFechaCarga(fechaCarga)
        .conFuenteOrigen(Origen.PROVISTO_CONTRIBUYENTE)
        .build()
    );
  }

  @Test
  public void lanzaExcepcionSiFechaSucesoEsFutura() {
    LocalDateTime fechaFutura = LocalDateTime.now().plusDays(1);

    assertThrows(RuntimeException.class, () -> new HechoBuilder()
        .conTitulo("t")
        .conDescripcion("d")
        .conCategoria("c")
        .conDireccion("dir")
        .conProvincia("p")
        .conUbicacion(new PuntoGeografico(1.0, 1.0))
        .conFechaSuceso(fechaFutura)
        .conFechaCarga(LocalDateTime.now())
        .conFuenteOrigen(Origen.PROVISTO_CONTRIBUYENTE)
        .build()
    );
  }

  @Test
  public void lanzaExcepcionSiFechaCargaEsFutura() {
    LocalDateTime fechaFutura = LocalDateTime.now().plusDays(1);

    assertThrows(RuntimeException.class, () -> new HechoBuilder()
        .conTitulo("t")
        .conDescripcion("d")
        .conCategoria("c")
        .conDireccion("dir")
        .conProvincia("p")
        .conUbicacion(new PuntoGeografico(1.0, 1.0))
        .conFechaSuceso(LocalDateTime.now())
        .conFechaCarga(fechaFutura)
        .conFuenteOrigen(Origen.PROVISTO_CONTRIBUYENTE)
        .build()
    );
  }

  @Test
  public void hechoValidoNoLanzaExcepcion() {
    assertDoesNotThrow(() -> new HechoBuilder()
        .conTitulo("ok")
        .conDescripcion("desc")
        .conCategoria("cat")
        .conDireccion("dir")
        .conProvincia("p")
        .conUbicacion(new PuntoGeografico(1.0, 1.0))
        .conFechaSuceso(LocalDateTime.now().minusDays(2))
        .conFechaCarga(LocalDateTime.now().minusDays(1))
        .conFuenteOrigen(Origen.PROVISTO_CONTRIBUYENTE)
        .build()
    );
  }

  @Test
  public void esEditableAntesDeUnaSemana() {
    LocalDateTime hace3Dias = LocalDateTime.now().minusDays(3);
    Hecho hecho = new HechoBuilder()
        .conTitulo("titulo")
        .conDescripcion("desc")
        .conCategoria("cat")
        .conDireccion("dir")
        .conProvincia("p")
        .conUbicacion(new PuntoGeografico(1, 1))
        .conFechaSuceso(hace3Dias)
        .conFechaCarga(hace3Dias)
        .conFuenteOrigen(Origen.DATASET)
        .conEtiquetas(List.of("etiqueta"))
        .build();

    assertTrue(hecho.esEditable(), "El hecho debería ser editable antes de una semana");
  }

  @Test
  public void noEsEditablePasadaUnaSemana() {
    LocalDateTime hace10Dias = LocalDateTime.now().minusDays(10);
    Hecho hecho = new HechoBuilder()
        .conTitulo("titulo")
        .conDescripcion("desc")
        .conCategoria("cat")
        .conDireccion("dir")
        .conProvincia("p")
        .conUbicacion(new PuntoGeografico(1, 1))
        .conFechaSuceso(hace10Dias)
        .conFechaCarga(hace10Dias)
        .conFuenteOrigen(Origen.DATASET)
        .conEtiquetas(List.of("etiqueta"))
        .build();

    assertFalse(hecho.esEditable(), "El hecho NO debería ser editable después de una semana");
  }
}
