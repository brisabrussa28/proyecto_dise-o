package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.domain.algoritmosconsenso.Absoluta;
import ar.edu.utn.frba.dds.domain.algoritmosconsenso.AlgoritmoDeConsenso;
import ar.edu.utn.frba.dds.domain.algoritmosconsenso.MayoriaSimple;
import ar.edu.utn.frba.dds.domain.algoritmosconsenso.MultiplesMenciones;
import ar.edu.utn.frba.dds.domain.coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.filtro.Filtro;
import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDeAgregacion;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.HechoBuilder;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import ar.edu.utn.frba.dds.domain.reportes.RepositorioDeSolicitudes;
import ar.edu.utn.frba.dds.domain.serializadores.Serializador;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class AlgoritmoDeConsensoTest {
  @Mock
  private RepositorioDeSolicitudes repo;
  @Mock
  private Filtro filtroExcluyente;
  @Mock
  private Serializador<Hecho> serializadorMock;

  private FuenteDeAgregacion agregador;
  private AlgoritmoDeConsenso absoluta;
  private AlgoritmoDeConsenso mayoriaSimple;
  private AlgoritmoDeConsenso multiplesMenciones;
  private static final LocalDateTime fecha = LocalDateTime.of(2023, 1, 1, 0, 0);


  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    when(filtroExcluyente.filtrar(anyList()))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(repo.filtroExcluyente()).thenReturn(filtroExcluyente);
    when(serializadorMock.importar("/tmp/fake.json")).thenReturn(new ArrayList<>());

    agregador = new FuenteDeAgregacion("Agregador", "/tmp/fake.json", serializadorMock);

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

  @SafeVarargs
  private void agregarFuentesConHechos(List<Hecho>... listasDeHechos) {
    // Reinicia el agregador para cada test con las fuentes necesarias
    agregador = new FuenteDeAgregacion("Agregador", "/tmp/fake.json", serializadorMock);
    for (List<Hecho> lista : listasDeHechos) {
      Fuente f = mock(Fuente.class);
      when(f.obtenerHechos()).thenReturn(lista);
      agregador.agregarFuente(f);
    }
  }

  @Test
  public void algoritmoAbsolutaHechosConsensuados() {
    Hecho h1 = crearHecho("H1");
    Hecho h2 = crearHecho("H2");
    agregarFuentesConHechos(
        List.of(h1, h2),
        List.of(h1, h2)
    );
    agregador.forzarActualizacionSincrona();

    Coleccion coleccion = new Coleccion("AbsolutaOk", agregador, "Desc", "Categoria", absoluta);

    List<Hecho> result = coleccion.getHechos(repo);
    assertEquals(2, result.size());
    assertTrue(result.containsAll(List.of(h1, h2)));
  }

  @Test
  public void algoritmoAbsolutaHechosNoConsensuados() {
    Hecho h1 = crearHecho("H1");
    Hecho h2 = crearHecho("H2");
    agregarFuentesConHechos(
        List.of(h1, h2),
        List.of(h1) // h2 falta
    );
    agregador.forzarActualizacionSincrona();

    Coleccion coleccion = new Coleccion("AbsolutaNoOk", agregador, "Desc", "Categoria", absoluta);

    List<Hecho> result = coleccion.getHechos(repo);
    assertEquals(1, result.size());
    assertTrue(result.contains(h1));
    assertFalse(result.contains(h2));
  }

  @Test
  public void algoritmoMayoriaSimpleConsensuado() {
    Hecho h1 = crearHecho("H1");
    agregarFuentesConHechos(
        List.of(h1),
        List.of(h1),
        List.of()
    );
    agregador.forzarActualizacionSincrona();

    Coleccion coleccion = new Coleccion("MayoriaOk", agregador, "Desc", "Categoria", mayoriaSimple);

    List<Hecho> result = coleccion.getHechos(repo);
    assertEquals(1, result.size());
    assertTrue(result.contains(h1));
  }

  @Test
  public void algoritmoMayoriaSimpleNoConsensuado() {
    Hecho h1 = crearHecho("H1");
    agregarFuentesConHechos(
        List.of(h1),
        List.of(),
        List.of()
    );
    agregador.forzarActualizacionSincrona();

    Coleccion coleccion = new Coleccion(
        "MayoriaNoOk",
        agregador,
        "Desc",
        "Categoria",
        mayoriaSimple
    );

    List<Hecho> result = coleccion.getHechos(repo);
    assertEquals(0, result.size());
  }

  @Test
  public void testMultiplesMencionesConsensuado() {
    Hecho h1 = crearHecho("H1");
    agregarFuentesConHechos(
        List.of(h1),
        List.of(h1),
        List.of()
    );
    agregador.forzarActualizacionSincrona();

    Coleccion coleccion = new Coleccion(
        "MultiplesOk",
        agregador,
        "Desc",
        "Categoria",
        multiplesMenciones
    );

    List<Hecho> result = coleccion.getHechos(repo);
    assertEquals(1, result.size());
    assertTrue(result.contains(h1));
  }

  @Test
  public void testMultiplesMencionesNoConsensuado() {
    Hecho hOriginal = crearHecho("H1");
    agregarFuentesConHechos(
        List.of(hOriginal),
        List.of() // Solo una mención
    );
    agregador.forzarActualizacionSincrona();

    Coleccion coleccion = new Coleccion(
        "MultiplesNoOk",
        agregador,
        "Desc",
        "Categoria",
        multiplesMenciones
    );

    List<Hecho> result = coleccion.getHechos(repo);
    assertEquals(0, result.size());
  }
}
