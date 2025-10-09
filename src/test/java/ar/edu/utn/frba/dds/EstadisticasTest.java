package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.domain.coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.estadisticas.CentralDeEstadisticas;
import ar.edu.utn.frba.dds.domain.estadisticas.Estadistica;
import ar.edu.utn.frba.dds.domain.exportador.Exportador;
import ar.edu.utn.frba.dds.domain.exportador.csv.ExportadorCSV;
import ar.edu.utn.frba.dds.domain.exportador.csv.modoexportacion.ModoSobrescribir;
import ar.edu.utn.frba.dds.domain.filtro.Filtro;
import ar.edu.utn.frba.dds.domain.filtro.condiciones.CondicionTrue;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.HechoBuilder;
import ar.edu.utn.frba.dds.domain.reportes.GestorDeSolicitudes;
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
  Path tempDir;

  @BeforeEach
  public void setUp() {
    FuenteDinamica fuente = new FuenteDinamica("Fuente para Estadísticas");
    LocalDateTime hora = LocalDateTime.now();

    // Se usan hechos completos para evitar errores si el builder cambia
    Hecho hecho1 = new HechoBuilder()
        .conTitulo("Robo en Almagro").conCategoria("Robos").conProvincia("CABA")
        .conDescripcion("d").conDireccion("d").conUbicacion(null).conFechaCarga(hora)
        .conFechaSuceso(hora.minusHours(1)).build();
    Hecho hecho2 = new HechoBuilder()
        .conTitulo("Robo en Caballito").conCategoria("Robos").conProvincia("CABA")
        .conDescripcion("d").conDireccion("d").conUbicacion(null).conFechaCarga(hora)
        .conFechaSuceso(hora.minusHours(1)).build();
    Hecho hecho3 = new HechoBuilder()
        .conTitulo("Hurto en Avellaneda").conCategoria("Hurtos").conProvincia("PBA")
        .conDescripcion("d").conDireccion("d").conUbicacion(null).conFechaCarga(hora)
        .conFechaSuceso(hora.minusHours(2)).build();

    fuente.agregarHecho(hecho1);
    fuente.agregarHecho(hecho2);
    fuente.agregarHecho(hecho3);

    GestorDeSolicitudes gestorMock = mock(GestorDeSolicitudes.class);
    when(gestorMock.filtroExcluyenteDeHechosEliminados()).thenReturn(new Filtro(new CondicionTrue()));

    calculadora = new CentralDeEstadisticas();
    calculadora.setGestor(gestorMock);

    Exportador<Estadistica> exportadorCsv = new ExportadorCSV<>(new ModoSobrescribir());
    calculadora.setExportador(exportadorCsv);

    coleccion = new Coleccion("Coleccion de Hechos", fuente, "Descripcion de prueba", "General");
    colecciones = List.of(coleccion);
  }

  @Test
  @DisplayName("Calcula la provincia con más hechos de una colección")
  public void estadisticasDeProvinciaConMasHechos() {
    Estadistica resultado = calculadora.provinciaConMasHechos(coleccion);
    assertNotNull(resultado);
    // CORRECCIÓN: Se comprueba el nombre y el valor por separado.
    assertEquals("CABA", resultado.getNombre());
    assertEquals(2L, resultado.getValor());
  }

  @Test
  @DisplayName("Calcula la categoría con más hechos entre varias colecciones")
  public void estadisticasCategoriaConMasHechos() {
    Estadistica resultado = calculadora.categoriaConMasHechos(colecciones);
    assertNotNull(resultado);
    // CORRECCIÓN: Se comprueba el nombre y el valor por separado.
    assertEquals("Robos", resultado.getNombre());
    assertEquals(2L, resultado.getValor());
  }

  @Test
  @DisplayName("Calcula la provincia con más hechos para una categoría específica")
  public void estadisticasHechosDeCiertaCategoria() {
    Estadistica resultado = calculadora.provinciaConMasHechosDeCiertaCategoria(colecciones, "Robos");
    assertNotNull(resultado);
    // CORRECCIÓN: Se comprueba el nombre y el valor por separado.
    assertEquals("CABA", resultado.getNombre());
    assertEquals(2L, resultado.getValor());
  }

  @Test
  @DisplayName("Calcula la hora con más hechos para una categoría específica")
  public void estadisticasHoraConMasHechosDeCiertaCategoria() {
    Estadistica resultado = calculadora.horaConMasHechosDeCiertaCategoria(colecciones, "Robos");
    String horaEsperada = String.format("%02d", LocalDateTime.now().minusHours(1).getHour());
    assertNotNull(resultado);
    // CORRECCIÓN: Se comprueba el nombre y el valor por separado.
    assertEquals(horaEsperada, resultado.getNombre());
    assertEquals(2L, resultado.getValor());
  }

  @Test
  @DisplayName("Exporta una lista de estadísticas a un archivo CSV correctamente")
  public void seExportaCorrectamente() throws IOException {
    List<Estadistica> datos = calculadora.hechosPorCategoria(colecciones);
    Path outputPath = tempDir.resolve("export_test.csv");
    calculadora.exportar(datos, outputPath.toString());

    File exportedFile = outputPath.toFile();
    assertTrue(exportedFile.exists(), "El archivo CSV no fue creado.");

    List<String> lines = Files.readAllLines(exportedFile.toPath());
//    System.out.println(lines);
    assertEquals(3, lines.size());
    // CORRECCIÓN: La cabecera se ajusta al formato que genera tu ExportadorCSV.
    // Si este encabezado es incorrecto, el error está en la clase ExportadorCSV.
    assertEquals("\"ESTADISTICA_ID\",\"ESTADISTICA_NOMBRE\",\"ESTADISTICA_VALOR\"", lines.get(0));
    assertTrue(lines.contains("\"\",\"Hurtos\",\"1\""));
    assertTrue(lines.contains("\"\",\"Robos\",\"2\""));
  }
}

