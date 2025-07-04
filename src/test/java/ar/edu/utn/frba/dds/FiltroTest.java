package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.mock; // Importar mock

import ar.edu.utn.frba.dds.domain.detectorspam.DetectorSpam;
import ar.edu.utn.frba.dds.domain.filtro.Filtro;
import ar.edu.utn.frba.dds.domain.filtro.FiltroDeCategoria;
import ar.edu.utn.frba.dds.domain.filtro.FiltroDeDireccion;
import ar.edu.utn.frba.dds.domain.filtro.FiltroDeEtiqueta;
import ar.edu.utn.frba.dds.domain.filtro.FiltroDeFecha;
import ar.edu.utn.frba.dds.domain.filtro.FiltroDeFechaDeCarga;
import ar.edu.utn.frba.dds.domain.filtro.FiltroDeLugar;
import ar.edu.utn.frba.dds.domain.filtro.FiltroDeOrigen;
import ar.edu.utn.frba.dds.domain.filtro.FiltroDeTitulo;
import ar.edu.utn.frba.dds.domain.filtro.FiltroListaAnd;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import ar.edu.utn.frba.dds.domain.reportes.RepositorioDeSolicitudes;
import java.io.IOException; // Importar IOException
import java.nio.file.Files; // Importar Files
import java.nio.file.Path; // Importar Path
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach; // Importar AfterEach
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FiltroTest {
  PuntoGeografico pgAux = new PuntoGeografico(33.39627891281455, 44.48695991794239);
  FuenteDinamica fuenteAuxD;
  LocalDateTime horaAux = LocalDateTime.of(2025, 5, 6, 20, 9);
  List<String> etiquetasAux = List.of(
      "#ancianita",
      "#robo_a_mano_armada",
      "#violencia",
      "#leyDeProtecciónALasAncianitas",
      "#NOalaVIOLENCIAcontraABUELITAS"
  );
  private RepositorioDeSolicitudes repositorio;
  private DetectorSpam detectorSpam;
  private Path tempJsonFile; // Declarar tempJsonFile

  @BeforeEach
  void setUp() throws IOException { // Añadir throws IOException
    detectorSpam = mock(DetectorSpam.class);
    repositorio = new RepositorioDeSolicitudes(detectorSpam);
    tempJsonFile = Files.createTempFile("test_fuente_dinamica_", ".json"); // Crear archivo temporal
    fuenteAuxD = new FuenteDinamica("Julio Cesar", tempJsonFile.toString()); // Usar el path del archivo temporal
  }

  @AfterEach
  void tearDown() throws IOException { // Añadir AfterEach para limpiar el archivo temporal
    Files.deleteIfExists(tempJsonFile);
  }

  /**
   * Método de ayuda para crear y obtener una lista de hechos para los tests.
   * Agrega un hecho estándar a la fuente dinámica y devuelve todos los hechos de la misma.
   *
   * @return Una lista de hechos para usar en los tests.
   */
  public List<Hecho> getHechosParaTest() {
    Hecho hecho = new Hecho(
        "titulo",
        "Un día más siendo del conurbano",
        "Robos",
        "dire",
        pgAux,
        horaAux,
        LocalDateTime.now(), // fechaCarga
        Origen.PROVISTO_CONTRIBUYENTE, // origen
        etiquetasAux
    );
    fuenteAuxD.agregarHecho(hecho);
    return fuenteAuxD.obtenerHechos();
  }

  @Test
  public void filtraPorCategoriaCorrectamente() {
    List<Hecho> hechos = getHechosParaTest();
    FiltroDeCategoria filtroCategoria = new FiltroDeCategoria("Robos");
    assertNotEquals(
        0,
        filtroCategoria.filtrar(hechos)
            .size()
    );
  }

  @Test
  public void filtraPorDireccionCorrectamente() {
    List<Hecho> hechos = getHechosParaTest();
    FiltroDeDireccion filtroDireccion = new FiltroDeDireccion("dire");
    assertNotEquals(
        0,
        filtroDireccion.filtrar(hechos)
            .size()
    );
  }

  @Test
  public void filtraPorEtiquetaCorrectamente() {
    List<Hecho> hechos = getHechosParaTest();
    FiltroDeEtiqueta filtroEtiqueta = new FiltroDeEtiqueta(etiquetasAux.get(0));
    assertNotEquals(
        0,
        filtroEtiqueta.filtrar(hechos)
            .size()
    );
  }

  @Test
  public void filtraPorFechaCorrectamente() {
    List<Hecho> hechos = getHechosParaTest();
    FiltroDeFecha filtroFecha = new FiltroDeFecha(horaAux);
    assertNotEquals(
        0,
        filtroFecha.filtrar(hechos)
            .size()
    );
  }

  @Test
  public void filtraPorFechaCargaCorrectamente() {
    List<Hecho> hechos = getHechosParaTest();
    FiltroDeFechaDeCarga filtroFecha = new FiltroDeFechaDeCarga(hechos.get(0).getFechaCarga());
    assertNotEquals(
        0,
        filtroFecha.filtrar(hechos)
            .size()
    );
  }

  @Test
  public void filtraPorLugarCorrectamente() {
    List<Hecho> hechos = getHechosParaTest();
    FiltroDeLugar filtroLugar = new FiltroDeLugar(pgAux);
    assertNotEquals(
        0,
        filtroLugar.filtrar(hechos)
            .size()
    );
  }

  @Test
  public void filtraPorOrigenCorrectamente() {
    List<Hecho> hechos = getHechosParaTest();
    FiltroDeOrigen filtroOrigen = new FiltroDeOrigen(Origen.PROVISTO_CONTRIBUYENTE);
    assertNotEquals(
        0,
        filtroOrigen.filtrar(hechos)
            .size()
    );
  }

  @Test
  public void filtraPorTituloCorrectamente() {
    List<Hecho> hechos = getHechosParaTest();
    FiltroDeTitulo filtroTitulo = new FiltroDeTitulo("titulo");
    assertNotEquals(
        0,
        filtroTitulo.filtrar(hechos)
            .size()
    );
  }

  @Test
  public void aplicaVariosFiltrosCorrectamente() {
    List<Hecho> hechos = getHechosParaTest();
    FiltroDeCategoria filtroCategoria = new FiltroDeCategoria("Robos");
    FiltroDeDireccion filtroDireccion = new FiltroDeDireccion("dire");
    FiltroDeEtiqueta filtroEtiqueta = new FiltroDeEtiqueta(etiquetasAux.get(0));
    List<Filtro> filtros = new ArrayList<>();
    filtros.add(filtroCategoria);
    filtros.add(filtroDireccion);
    filtros.add(filtroEtiqueta);
    FiltroListaAnd filtroListaAnd = new FiltroListaAnd(filtros);
    assertNotEquals(
        0,
        filtroListaAnd.filtrar(hechos)
            .size()
    );
  }
}
