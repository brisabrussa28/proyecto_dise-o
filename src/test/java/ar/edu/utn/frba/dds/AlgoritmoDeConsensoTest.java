package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.domain.coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.coleccion.algoritmosconsenso.Absoluta;
import ar.edu.utn.frba.dds.domain.coleccion.algoritmosconsenso.AlgoritmoDeConsenso;
import ar.edu.utn.frba.dds.domain.coleccion.algoritmosconsenso.MayoriaSimple;
import ar.edu.utn.frba.dds.domain.coleccion.algoritmosconsenso.MultiplesMenciones;
import ar.edu.utn.frba.dds.domain.filtro.Filtro;
import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDeAgregacion;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.HechoBuilder;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AlgoritmoDeConsensoTest {
  private Filtro filtroExcluyente;
  private AlgoritmoDeConsenso absoluta;
  private AlgoritmoDeConsenso mayoriaSimple;
  private AlgoritmoDeConsenso multiplesMenciones;
  private static final LocalDateTime fecha = LocalDateTime.of(2023, 1, 1, 0, 0);

  @BeforeEach
  public void setUp() {
    filtroExcluyente = mock(Filtro.class);
    when(filtroExcluyente.filtrar(anyList()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    absoluta = new Absoluta();
    mayoriaSimple = new MayoriaSimple();
    multiplesMenciones = new MultiplesMenciones();
  }

  private Hecho crearHecho(String titulo) {
    return new HechoBuilder()
        .conTitulo(titulo)
        .conFechaSuceso(fecha)
        .build();
  }

  /**
   * Método de ayuda para crear una FuenteDeAgregacion con fuentes mockeadas.
   * Ahora usa el constructor simple de FuenteDeAgregacion.
   */
  private FuenteDeAgregacion crearAgregadorConFuentes(List<Hecho>... listasDeHechos) {
    FuenteDeAgregacion agregador = new FuenteDeAgregacion("AgregadorDeTest");

    for (List<Hecho> lista : listasDeHechos) {
      Fuente f = mock(Fuente.class);
      when(f.getHechos()).thenReturn(lista);
      agregador.agregarFuente(f);
    }
    return agregador;
  }

  @Test
  public void AlgoritmoAbsolutaHechosConsensuados() {
    Hecho h1 = crearHecho("H1");
    Hecho h2 = crearHecho("H2");
    FuenteDeAgregacion agregador = crearAgregadorConFuentes(
        List.of(h1, h2),
        List.of(h1, h2)
    );

    Coleccion coleccion = new Coleccion(
        "AbsolutaOk", agregador, "Desc", "Categoria");
    coleccion.setAlgoritmoDeConsenso(absoluta);
    coleccion.recalcularHechosConsensuados(filtroExcluyente);
    List<Hecho> result = coleccion.getHechosConsensuados();

    assertEquals(2, result.size());
    assertTrue(result.contains(h1));
    assertTrue(result.contains(h2));
  }

  @Test
  public void AlgoritmoAbsolutaHechosNoConsensuados() {
    Hecho h1 = crearHecho("H1");
    Hecho h2 = crearHecho("H2");
    FuenteDeAgregacion agregador = crearAgregadorConFuentes(
        List.of(h1, h2),
        List.of(h1) // h2 falta en esta fuente
    );

    Coleccion coleccion = new Coleccion(
        "AbsolutaNoOk", agregador, "Desc", "Categoria");
    coleccion.setAlgoritmoDeConsenso(absoluta);
    coleccion.recalcularHechosConsensuados(filtroExcluyente);
    List<Hecho> result = coleccion.getHechosConsensuados();

    assertEquals(1, result.size());
    assertTrue(result.contains(h1));
    assertFalse(result.contains(h2));
  }

  @Test
  public void AlgoritmoMayoriaSimpleConsensuado() {
    Hecho h1 = crearHecho("H1");
    FuenteDeAgregacion agregador = crearAgregadorConFuentes(
        List.of(h1), // Aparece en 2 de 3 fuentes
        List.of(h1),
        List.of()
    );

    Coleccion coleccion = new Coleccion(
        "MayoriaOk", agregador, "Desc", "Categoria");
    coleccion.setAlgoritmoDeConsenso(mayoriaSimple);
    coleccion.recalcularHechosConsensuados(filtroExcluyente);
    List<Hecho> result = coleccion.getHechosConsensuados();

    assertEquals(1, result.size());
    assertTrue(result.contains(h1));
  }

  @Test
  public void AlgoritmoMayoriaSimpleNoConsensuado() {
    Hecho h1 = crearHecho("H1");
    FuenteDeAgregacion agregador = crearAgregadorConFuentes(
        List.of(h1), // Aparece en 1 de 3 fuentes, no es mayoría
        List.of(),
        List.of()
    );

    Coleccion coleccion = new Coleccion(
        "MayoriaNoOk", agregador, "Desc", "Categoria");
    coleccion.setAlgoritmoDeConsenso(mayoriaSimple);
    coleccion.recalcularHechosConsensuados(filtroExcluyente);
    List<Hecho> result = coleccion.getHechosConsensuados();

    assertEquals(0, result.size());
  }

  @Test
  public void testMultiplesMencionesConsensuado() {
    Hecho h1 = crearHecho("H1");
    FuenteDeAgregacion agregador = crearAgregadorConFuentes(
        List.of(h1), // Aparece en 2 de 3 fuentes
        List.of(h1),
        List.of()
    );

    Coleccion coleccion = new Coleccion(
        "MultiplesOk", agregador, "Desc", "Categoria");
    coleccion.setAlgoritmoDeConsenso(multiplesMenciones);
    coleccion.recalcularHechosConsensuados(filtroExcluyente);
    List<Hecho> result = coleccion.getHechosConsensuados();

    assertEquals(1, result.size());
    assertTrue(result.contains(h1));
  }

  @Test
  public void testMultiplesMencionesNoConsensuado() {
    Hecho h1 = crearHecho("H1");
    FuenteDeAgregacion agregador = crearAgregadorConFuentes(
        List.of(h1), // Aparece solo en 1 de 2 fuentes
        List.of()
    );

    Coleccion coleccion = new Coleccion(
        "MultiplesNoOk", agregador, "Desc", "Categoria");
    coleccion.setAlgoritmoDeConsenso(multiplesMenciones);
    coleccion.recalcularHechosConsensuados(filtroExcluyente);
    List<Hecho> result = coleccion.getHechosConsensuados();

    assertEquals(0, result.size());
  }
}

