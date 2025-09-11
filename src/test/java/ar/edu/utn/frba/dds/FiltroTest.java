package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.domain.filtro.FiltroPersistente;
import ar.edu.utn.frba.dds.domain.filtro.condiciones.condicion.Condicion;
import ar.edu.utn.frba.dds.domain.filtro.condiciones.condicion.CondicionAnd;
import ar.edu.utn.frba.dds.domain.filtro.condiciones.condicion.CondicionGenerica;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.HechoBuilder;
import ar.edu.utn.frba.dds.domain.hecho.Origen;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.serializadores.Serializador;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class FiltroTest {
  PuntoGeografico pgAux = new PuntoGeografico(33.39627891281455, 44.48695991794239);
  FuenteDinamica fuenteAuxD;
  LocalDateTime horaAux = LocalDateTime.now().minusDays(1);

  @Mock
  private Serializador<Hecho> serializadorMock;
  private Path tempJsonFile;
  private Hecho hechoDePrueba;

  @BeforeEach
  void setUp() throws IOException {
    MockitoAnnotations.openMocks(this);
    tempJsonFile = Files.createTempFile("test_fuente_dinamica_", ".json");
    when(serializadorMock.importar(anyString())).thenReturn(new ArrayList<>());
    fuenteAuxD = new FuenteDinamica("Julio Cesar", tempJsonFile.toString(), serializadorMock);

    hechoDePrueba = new HechoBuilder()
        .conTitulo("titulo")
        .conDescripcion("Un día más siendo del conurbano")
        .conCategoria("Robos")
        .conDireccion("dire")
        .conProvincia("Buenos Aires")
        .conUbicacion(pgAux)
        .conFechaSuceso(horaAux)
        .conFechaCarga(LocalDateTime.now())
        .conFuenteOrigen(Origen.PROVISTO_CONTRIBUYENTE)
        .build();
  }

  @AfterEach
  void tearDown() throws IOException {
    Files.deleteIfExists(tempJsonFile);
  }

  @Test
  public void filtraPorCategoriaCorrectamente() {
    List<Hecho> hechos = List.of(hechoDePrueba);
    Condicion condicionCategoria = new CondicionGenerica("categoria", "IGUAL", "Robos");
    FiltroPersistente filtro = new FiltroPersistente(condicionCategoria);

    List<Hecho> resultado = filtro.filtrar(hechos);
    assertEquals(1, resultado.size());
    assertEquals("Robos", resultado.get(0).getCategoria());
  }

  @Test
  public void filtraPorDireccionCorrectamente() {
    List<Hecho> hechos = List.of(hechoDePrueba);
    Condicion condicionDireccion = new CondicionGenerica("direccion", "IGUAL", "dire");
    FiltroPersistente filtro = new FiltroPersistente(condicionDireccion);

    List<Hecho> resultado = filtro.filtrar(hechos);
    assertEquals(1, resultado.size());
    assertEquals("dire", resultado.get(0).getDireccion());
  }

  @Test
  public void filtraPorFechaSucesoCorrectamente() {
    List<Hecho> hechos = List.of(hechoDePrueba);
    Condicion condicionFecha = new CondicionGenerica("fechaSuceso", "IGUAL", horaAux);
    FiltroPersistente filtro = new FiltroPersistente(condicionFecha);
    assertEquals(1, filtro.filtrar(hechos).size());
  }


  @Test
  public void aplicaVariosFiltrosCorrectamente() {
    List<Hecho> hechos = List.of(hechoDePrueba);

    CondicionAnd condicionAnd = new CondicionAnd();
    condicionAnd.agregarCondicion(new CondicionGenerica("categoria", "IGUAL", "Robos"));
    condicionAnd.agregarCondicion(new CondicionGenerica("direccion", "IGUAL", "dire"));

    FiltroPersistente filtro = new FiltroPersistente(condicionAnd);
    assertEquals(1, filtro.filtrar(hechos).size());
  }

  @Test
  public void aplicaVariosFiltrosYFalla() {
    List<Hecho> hechos = List.of(hechoDePrueba);

    CondicionAnd condicionAnd = new CondicionAnd();
    condicionAnd.agregarCondicion(new CondicionGenerica("categoria", "IGUAL", "Robos"));
    condicionAnd.agregarCondicion(new CondicionGenerica("direccion", "IGUAL", "direccion_incorrecta"));

    FiltroPersistente filtro = new FiltroPersistente(condicionAnd);
    assertTrue(filtro.filtrar(hechos).isEmpty());
  }
}

