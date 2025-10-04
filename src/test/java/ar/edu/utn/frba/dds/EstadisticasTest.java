package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.domain.coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.estadisicas.CentralDeEstadisticas;
import ar.edu.utn.frba.dds.domain.estadisicas.Estadistica;
import ar.edu.utn.frba.dds.domain.exportador.Exportador;
import ar.edu.utn.frba.dds.domain.exportador.csv.ExportadorCSV;
import ar.edu.utn.frba.dds.domain.exportador.csv.modoexportacion.ModoSobrescribir;
import ar.edu.utn.frba.dds.domain.filtro.Filtro;
import ar.edu.utn.frba.dds.domain.filtro.condiciones.CondicionTrue;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.HechoBuilder;
import ar.edu.utn.frba.dds.domain.reportes.RepositorioDeSolicitudes;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class EstadisticasTest {
  private CentralDeEstadisticas calculadora;
  private Coleccion coleccion;
  private List<Coleccion> colecciones;

  @TempDir
  Path tempDir; // Directorio temporal para el test de exportación

  @BeforeEach
  public void setUp() {
    // 1. Crear la fuente y los hechos de prueba
    FuenteDinamica fuente = new FuenteDinamica("Fuente para Estadísticas");
    LocalDateTime hora = LocalDateTime.now();

    Hecho hecho1 = new HechoBuilder()
        .conTitulo("Robo en Almagro").conCategoria("Robos").conProvincia("CABA")
        .conFechaSuceso(hora.minusHours(1)).build();

    Hecho hecho2 = new HechoBuilder()
        .conTitulo("Robo en Caballito").conCategoria("Robos").conProvincia("CABA")
        .conFechaSuceso(hora.minusHours(1)).build();

    Hecho hecho3 = new HechoBuilder()
        .conTitulo("Hurto en Avellaneda").conCategoria("Hurtos").conProvincia("PBA")
        .conFechaSuceso(hora.minusHours(2)).build();

    fuente.agregarHecho(hecho1);
    fuente.agregarHecho(hecho2);
    fuente.agregarHecho(hecho3);

    // 2. Mockear el repositorio para que devuelva un filtro que no haga nada
    RepositorioDeSolicitudes repoMock = mock(RepositorioDeSolicitudes.class);
    // FIX: Se instancia un Filtro con CondicionTrue para simular un filtro nulo.
    when(repoMock.filtroExcluyente()).thenReturn(new Filtro(new CondicionTrue()));

    // 3. Configurar la CentralDeEstadisticas con sus dependencias
    calculadora = new CentralDeEstadisticas();
    calculadora.setRepo(repoMock);

    Exportador<Estadistica> exportadorCsv = new ExportadorCSV<>(new ModoSobrescribir());
    calculadora.setExportador(exportadorCsv);

    // 4. Crear la colección y la lista de colecciones para los tests
    coleccion = new Coleccion("Coleccion de Hechos", fuente, "Descripcion de prueba", "General");
    colecciones = List.of(coleccion);
  }

  @Test
  @DisplayName("La central puede contar correctamente las solicitudes de spam del repositorio")
  public void estadisticasSpam() {
    // Arrange
    RepositorioDeSolicitudes repoMock = mock(RepositorioDeSolicitudes.class);
    when(repoMock.cantidadDeSpamDetectado()).thenReturn(5); // Simulamos que el repo detectó 5 spams
    calculadora.setRepo(repoMock);

    // Act & Assert
    assertEquals(5, calculadora.cantidadDeSolicitudesSpam());
  }

  @Test
  @DisplayName("Calcula correctamente la provincia con más hechos de una colección")
  public void estadisticasDeProvinciaConMasHechos() {
    Estadistica resultado = calculadora.provinciaConMasHechos(coleccion);
    assertNotNull(resultado);
    assertEquals("CABA", resultado.getDimension());
    assertEquals(2, resultado.getValor());
  }

  @Test
  @DisplayName("Calcula correctamente la categoría con más hechos entre varias colecciones")
  public void estadisticasCategoriaConMasHechos() {
    Estadistica resultado = calculadora.categoriaConMasHechos(colecciones);
    assertNotNull(resultado);
    assertEquals("Robos", resultado.getDimension());
    assertEquals(2, resultado.getValor());
  }

  @Test
  @DisplayName("Calcula la provincia con más hechos para una categoría específica")
  public void estadisticasHechosDeCiertaCategoria() {
    Estadistica resultado = calculadora.provinciaConMasHechosDeCiertaCategoria(colecciones, "Robos");
    assertNotNull(resultado);
    assertEquals("CABA", resultado.getDimension());
    assertEquals(2, resultado.getValor());
  }

  @Test
  @DisplayName("Calcula la hora con más hechos para una categoría específica")
  public void estadisticasHoraConMasHechosDeCiertaCategoria() {
    Estadistica resultado = calculadora.horaConMasHechosDeCiertaCategoria(colecciones, "Robos");
    String horaEsperada = String.format("%02d", LocalDateTime.now().minusHours(1).getHour());
    assertNotNull(resultado);
    assertEquals(horaEsperada, resultado.getDimension());
    assertEquals(2, resultado.getValor());
  }

  @Test
  @DisplayName("Exporta una lista de estadísticas a un archivo CSV correctamente")
  public void seExportaCorrectamente() throws IOException {
    // Arrange
    List<Estadistica> datos = calculadora.hechosPorCategoria(colecciones);
    Path outputPath = tempDir.resolve("export_test.csv");

    // Act
    calculadora.exportar(datos, outputPath.toString());

    // Assert
    File exportedFile = outputPath.toFile();
    assertTrue(exportedFile.exists(), "El archivo CSV no fue creado.");
    assertTrue(exportedFile.length() > 0, "El archivo CSV está vacío.");

    List<String> lines = Files.readAllLines(exportedFile.toPath());
    assertEquals(3, lines.size(), "El archivo CSV no tiene el número esperado de filas (cabecera + 2 datos).");
    assertEquals("\"DIMENSION\",\"VALOR\"", lines.get(0));
    assertTrue(lines.contains("\"Hurtos\",\"1\""));
    assertTrue(lines.contains("\"Robos\",\"2\""));
  }
}

