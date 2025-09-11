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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AlgoritmoDeConsensoTest {
  private RepositorioDeSolicitudes repo;
  private Filtro filtroExcluyente;
  private FuenteDeAgregacion agregador;
  private AlgoritmoDeConsenso absoluta;
  private AlgoritmoDeConsenso mayoriaSimple;
  private AlgoritmoDeConsenso multiplesMenciones;
  private static final LocalDateTime fecha = LocalDateTime.of(2023, 1, 1, 0, 0);


  @BeforeEach
  public void setUp() throws IOException {
    repo = mock(RepositorioDeSolicitudes.class);
    filtroExcluyente = mock(Filtro.class);
    when(filtroExcluyente.filtrar(anyList()))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(repo.filtroExcluyente()).thenReturn(filtroExcluyente);

    Path tempFile = Files.createTempFile("fake", ".json");
    agregador = new FuenteDeAgregacion("Agregador", tempFile.toString());

    absoluta = new Absoluta();
    mayoriaSimple = new MayoriaSimple();
    multiplesMenciones = new MultiplesMenciones();
  }

  private Hecho crearHecho(String titulo) {
    return new HechoBuilder()
        .conTitulo(titulo)
        .conDescripcion("desc " + titulo)
        .conCategoria("cat " + titulo)
        .conDireccion("dir " + titulo)
        .conProvincia("Provincia " + titulo)
        .conUbicacion(new PuntoGeografico(1, 1))
        .conFechaSuceso(fecha)
        .conFechaCarga(fecha)
        .conFuenteOrigen(Origen.DATASET)
        .conEtiquetas(List.of("etiqueta"))
        .build();
  }

  private void agregarFuentesConHechos(List<Hecho>... listasDeHechos) throws IOException {
    Path tempFile = Files.createTempFile("fake", ".json");
    agregador = new FuenteDeAgregacion("Agregador", tempFile.toString());// reinicia cada vez
    for (List<Hecho> lista : listasDeHechos) {
      Fuente f = mock(Fuente.class);
      when(f.obtenerHechos()).thenReturn(lista);
      agregador.agregarFuente(f);
    }
  }

  @Test
  public void AlgoritmoAbsolutaHechosConsensuados() throws IOException {
    Hecho h1 = crearHecho("H1");
    Hecho h2 = crearHecho("H2");
    agregarFuentesConHechos(
        List.of(h1, h2),
        List.of(h1, h2)
    );
    agregador.forzarActualizacionSincrona();

    Coleccion coleccion = new Coleccion("AbsolutaOk", agregador, "Desc", "Catgoria", absoluta);

    List<Hecho> result = coleccion.getHechos(repo);
    assertEquals(2, result.size());
    assertTrue(result.contains(h1));
    assertTrue(result.contains(h2));
  }

  @Test
  public void AlgoritmoAbsolutaHechosNoConsensuados() throws IOException {
    Hecho h1 = crearHecho("H1");
    Hecho h2 = crearHecho("H2");
    agregarFuentesConHechos(
        List.of(h1, h2),
        List.of(h1) // h2 falta
    );
    agregador.forzarActualizacionSincrona();

    Coleccion coleccion = new Coleccion("AbsolutaNoOk", agregador, "Desc", "Catgoria", absoluta);
    coleccion.recalcularHechosConsensuados(repo);
    List<Hecho> result = coleccion.getHechosConsensuados();

    assertEquals(1, result.size());
    assertTrue(result.contains(h1));
    assertFalse(result.contains(h2));
  }

  @Test
  public void AlgoritmoMayoriaSimpleConsensuado() throws IOException {
    Hecho h1 = crearHecho("H1");
    agregarFuentesConHechos(
        List.of(h1),
        List.of(h1),
        List.of()
    );
    agregador.forzarActualizacionSincrona();

    Coleccion coleccion = new Coleccion("MayoriaOk", agregador, "Desc", "Catgoria", mayoriaSimple);

    List<Hecho> result = coleccion.getHechos(repo);
    assertEquals(1, result.size());
    assertTrue(result.contains(h1));
  }

  @Test
  public void AlgoritmoMayoriaSimpleNoConsensuado() throws IOException {
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
        "Catgoria",
        mayoriaSimple
    );
    coleccion.recalcularHechosConsensuados(repo);
    List<Hecho> result = coleccion.getHechosConsensuados();

    assertEquals(0, result.size());
  }

  @Test
  public void testMultiplesMencionesConsensuado() throws IOException {
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
        "Catgoria",
        multiplesMenciones
    );

    List<Hecho> result = coleccion.getHechos(repo);
    assertEquals(1, result.size());
    assertTrue(result.contains(h1));
  }

  @Test
  public void testMultiplesMencionesNoConsensuado() throws IOException {
    // Hecho aparece solo en una fuente
    Hecho h1 = crearHecho("H1");

    // Primera fuente lo contiene, segunda fuente está vacía
    agregarFuentesConHechos(
        List.of(h1),
        List.of()
    );
    agregador.forzarActualizacionSincrona();

    Coleccion coleccion = new Coleccion(
        "MultiplesNoOk",
        agregador,
        "Desc",
        "Categoria",
        multiplesMenciones
    );

    coleccion.recalcularHechosConsensuados(repo);
    List<Hecho> result = coleccion.getHechosConsensuados();

    // El hecho está en solo una fuente → no hay consenso
    assertEquals(0, result.size());
  }
}
