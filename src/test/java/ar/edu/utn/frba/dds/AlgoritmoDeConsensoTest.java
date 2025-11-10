package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.model.coleccion.Coleccion;
import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.Absoluta;
import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.AlgoritmoDeConsenso;
import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.MayoriaSimple;
import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.MultiplesMenciones;
import ar.edu.utn.frba.dds.model.filtro.Filtro;
import ar.edu.utn.frba.dds.model.filtro.condiciones.CondicionTrue;
import ar.edu.utn.frba.dds.model.fuentes.Fuente;
import ar.edu.utn.frba.dds.model.fuentes.FuenteDeAgregacion;
import ar.edu.utn.frba.dds.model.hecho.Estado;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.model.hecho.HechoBuilder;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
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
    // Usamos un filtro real que no filtra nada para los casos generales.
    filtroExcluyente = new Filtro(new CondicionTrue());
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
        "AbsolutaOk", agregador, "Desc", "Categoria", absoluta);
    coleccion.setAlgoritmoDeConsenso(absoluta);
    coleccion.recalcularHechosConsensuados(filtroExcluyente, agregador.getFuentesCargadas());
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
        "AbsolutaNoOk", agregador, "Desc", "Categoria", absoluta);
    coleccion.setAlgoritmoDeConsenso(absoluta);
    coleccion.recalcularHechosConsensuados(filtroExcluyente, agregador.getFuentesCargadas());
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
        "MayoriaOk", agregador, "Desc", "Categoria", mayoriaSimple);
    coleccion.setAlgoritmoDeConsenso(mayoriaSimple);
    coleccion.recalcularHechosConsensuados(filtroExcluyente, agregador.getFuentesCargadas());
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
        "MayoriaNoOk", agregador, "Desc", "Categoria", mayoriaSimple);
    coleccion.setAlgoritmoDeConsenso(mayoriaSimple);
    coleccion.recalcularHechosConsensuados(filtroExcluyente, agregador.getFuentesCargadas());
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
        "MultiplesOk", agregador, "Desc", "Categoria", multiplesMenciones);
    coleccion.setAlgoritmoDeConsenso(multiplesMenciones);
    coleccion.recalcularHechosConsensuados(filtroExcluyente, agregador.getFuentesCargadas());
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
        "MultiplesNoOk", agregador, "Desc", "Categoria", multiplesMenciones);
    coleccion.setAlgoritmoDeConsenso(multiplesMenciones);
    coleccion.recalcularHechosConsensuados(filtroExcluyente, agregador.getFuentesCargadas());
    List<Hecho> result = coleccion.getHechosConsensuados();

    assertEquals(0, result.size());
  }

  @Test
  public void hechoEliminadoNoEsConsensuado() {
    Hecho h1 = crearHecho("H1");
    h1.setEstado(Estado.ELIMINADO); // Marcamos el hecho como eliminado

    // Creamos un MOCK de Filtro solo para este test, ya que necesitamos un comportamiento específico
    Filtro filtroDeEliminados = mock(Filtro.class);
    when(filtroDeEliminados.filtrar(anyList())).thenAnswer(invocation -> {
      List<Hecho> aFiltrar = invocation.getArgument(0);
      return aFiltrar.stream()
                     .filter(h -> h.getEstado() != Estado.ELIMINADO)
                     .collect(Collectors.toList());
    });

    FuenteDeAgregacion agregador = crearAgregadorConFuentes(
        List.of(h1), // Aparece en todas las fuentes
        List.of(h1)
    );

    Coleccion coleccion = new Coleccion(
        "EliminadoNoConsensuado", agregador, "Desc", "Categoria", absoluta);
    coleccion.setAlgoritmoDeConsenso(absoluta);
    // Usamos el mock local en lugar del filtro global
    coleccion.recalcularHechosConsensuados(filtroDeEliminados, agregador.getFuentesCargadas());
    List<Hecho> result = coleccion.getHechosConsensuados();

    // El resultado debe ser 0 porque el filtro excluyente lo eliminó
    assertEquals(0, result.size());
  }
}

