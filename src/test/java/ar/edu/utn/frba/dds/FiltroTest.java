package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.mock; // Importar mock

import ar.edu.utn.frba.dds.domain.detectorspam.DetectorSpam;
import ar.edu.utn.frba.dds.domain.filtro.*;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.HechoBuilder;
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
  LocalDateTime horaAux = LocalDateTime.now().minusDays(1);
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
    Hecho hecho = new HechoBuilder()
        .conTitulo("titulo")
        .conDescripcion("Un día más siendo del conurbano")
        .conCategoria("Robos")
        .conDireccion("dire")
        .conProvincia("Buenos Aires")
        .conUbicacion(pgAux)
        .conFechaSuceso(horaAux)
        .conFechaCarga(LocalDateTime.now()) // fechaCarga
        .conFuenteOrigen(Origen.PROVISTO_CONTRIBUYENTE) // origen
        .conEtiquetas(etiquetasAux)
        .build();
    fuenteAuxD.agregarHecho(hecho);
    return fuenteAuxD.obtenerHechos();
  }

  @Test
  public void filtraPorCategoriaCorrectamente() {
    List<Hecho> hechos = getHechosParaTest();
    FiltroPredicado filtroCategoria = new FiltroPredicado(h -> h.getCategoria().equalsIgnoreCase("Robos"));
    assertNotEquals(
        0,
        filtroCategoria.filtrar(hechos)
            .size()
    );
  }

  @Test
  public void filtraPorDireccionCorrectamente() {
    List<Hecho> hechos = getHechosParaTest();
    FiltroPredicado filtroDireccion = new FiltroPredicado(h -> h.getDireccion().equalsIgnoreCase("Dire"));
    assertNotEquals(
        0,
        filtroDireccion.filtrar(hechos)
            .size()
    );
  }


  @Test
  public void filtraPorFechaCargaCorrectamente() {
    List<Hecho> hechos = getHechosParaTest();
    FiltroPredicado filtroFecha = new FiltroPredicado(h -> h.getFechaCarga().toLocalDate().equals(hechos.get(0).getFechaCarga().toLocalDate()));

    assertNotEquals(
        0,
        filtroFecha.filtrar(hechos)
            .size()
    );
  }


  @Test
  public void aplicaVariosFiltrosCorrectamente() {
    List<Hecho> hechos = getHechosParaTest();
    FiltroPredicado filtroCategoria = new FiltroPredicado(h -> h.getCategoria().equalsIgnoreCase("Robos"));
    FiltroPredicado filtroDireccion = new FiltroPredicado(h -> h.getDireccion().equalsIgnoreCase("Dire"));
    FiltroPredicado filtroEtiqueta = new FiltroPredicado(h -> h.getEtiquetas().contains(etiquetasAux.get(0)));
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
