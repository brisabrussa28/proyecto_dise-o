package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ar.edu.utn.frba.dds.model.coleccion.Coleccion;
import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.Absoluta;
import ar.edu.utn.frba.dds.model.estadisticas.CentralDeEstadisticas;
import ar.edu.utn.frba.dds.model.estadisticas.Estadistica;
import ar.edu.utn.frba.dds.model.exportador.Exportador;
import ar.edu.utn.frba.dds.model.exportador.csv.ExportadorCSV;
import ar.edu.utn.frba.dds.model.exportador.csv.modoexportacion.ModoSobrescribir;
import ar.edu.utn.frba.dds.model.filtro.Filtro;
import ar.edu.utn.frba.dds.model.filtro.condiciones.CondicionTrue;
import ar.edu.utn.frba.dds.model.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.model.hecho.HechoBuilder;
import ar.edu.utn.frba.dds.model.reportes.GestorDeSolicitudes;
import ar.edu.utn.frba.dds.model.reportes.Solicitud;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class EstadisticasTest {
  private CentralDeEstadisticas calculadora;
  private Coleccion coleccion;

  private final LocalDateTime FECHA_BASE = LocalDateTime.of(2023, 1, 1, 21, 0, 0);

  @TempDir
  Path tempDir;

  @BeforeEach
  public void setUp() {
    FuenteDinamica fuente = new FuenteDinamica("Fuente para Estadísticas");

    Hecho hecho1 = new HechoBuilder()
        .conTitulo("Robo en Almagro").conCategoria("Robos").conProvincia("CABA")
        .conDescripcion("d").conDireccion("d").conUbicacion(null).conFechaCarga(FECHA_BASE)
        .conFechaSuceso(FECHA_BASE.minusHours(1)).build(); // 20:00
    Hecho hecho2 = new HechoBuilder()
        .conTitulo("Robo en Caballito").conCategoria("Robos").conProvincia("CABA")
        .conDescripcion("d").conDireccion("d").conUbicacion(null).conFechaCarga(FECHA_BASE)
        .conFechaSuceso(FECHA_BASE.minusHours(1)).build(); // 20:00
    Hecho hecho3 = new HechoBuilder()
        .conTitulo("Hurto en Avellaneda").conCategoria("Hurtos").conProvincia("Buenos Aires")
        .conDescripcion("d").conDireccion("d").conUbicacion(null).conFechaCarga(FECHA_BASE)
        .conFechaSuceso(FECHA_BASE.minusHours(2)).build(); // 19:00

    fuente.agregarHecho(hecho1);
    fuente.agregarHecho(hecho2);
    fuente.agregarHecho(hecho3);

    // Mock del gestor
    GestorDeSolicitudes gestorMock = mock(GestorDeSolicitudes.class);
    when(gestorMock.filtroExcluyenteDeHechosEliminados()).thenReturn(new Filtro(new CondicionTrue()));
    when(gestorMock.obtenerHechosReportados()).thenReturn(List.of(hecho1, hecho2, hecho3));
    when(gestorMock.obtenerHechosEliminados()).thenReturn(List.of());

    // Mock de solicitudes y spam
    Solicitud solicitud1 = mock(Solicitud.class);
    Solicitud solicitud2 = mock(Solicitud.class);
    Solicitud spam1 = mock(Solicitud.class);

    when(gestorMock.getSolicitudesPendientes()).thenReturn(List.of(solicitud1, solicitud2));
    when(gestorMock.getSpam()).thenReturn(List.of(spam1));

    calculadora = new CentralDeEstadisticas();
    calculadora.setGestor(gestorMock);

    Exportador<Estadistica> exportadorCsv = new ExportadorCSV<>(new ModoSobrescribir());
    calculadora.setExportador(exportadorCsv);

    var algoritmo = new Absoluta();
    coleccion = new Coleccion("Coleccion de Hechos", fuente, "Descripcion de prueba", "General", algoritmo);
  }

  @Test
  @DisplayName("Calcula la provincia con más hechos de una colección")
  public void estadisticasDeProvinciaConMasHechos() {
    List<Estadistica> lista = calculadora.hechosPorProvinciaDeUnaColeccion(coleccion);
    Estadistica resultado = lista.stream()
                                 .max(Comparator.comparingLong(Estadistica::getCantidad))
                                 .orElseThrow();

    assertNotNull(resultado);
    assertEquals("CABA", resultado.getGrupo());
    assertEquals(2L, resultado.getCantidad());
  }

  @Test
  @DisplayName("Calcula la categoría con más hechos reportados")
  public void estadisticasCategoriaConMasHechos() {
    List<Estadistica> lista = calculadora.hechosPorCategoria();
    Estadistica resultado = lista.stream()
                                 .max(Comparator.comparingLong(Estadistica::getCantidad))
                                 .orElseThrow();

    assertNotNull(resultado);
    assertEquals("Robos", resultado.getCategoria());
    assertEquals(2L, resultado.getCantidad());
  }

  @Test
  @DisplayName("Calcula la provincia con más hechos para una categoría específica")
  public void estadisticasHechosDeCiertaCategoria() {
    List<Estadistica> lista = calculadora.hechosPorProvinciaSegunCategoria("Robos");
    Estadistica resultado = lista.stream()
                                 .max(Comparator.comparingLong(Estadistica::getCantidad))
                                 .orElseThrow();

    assertNotNull(resultado);
    assertEquals("CABA", resultado.getGrupo());
    assertEquals(2L, resultado.getCantidad());
  }

  @Test
  @DisplayName("Calcula la hora con más hechos para una categoría específica")
  public void estadisticasHoraConMasHechosDeCiertaCategoria() {
    List<Estadistica> lista = calculadora.hechosPorHora("Robos");
    Estadistica resultado = lista.stream()
                                 .max(Comparator.comparingLong(Estadistica::getCantidad))
                                 .orElseThrow();

    assertNotNull(resultado);
    assertEquals("20", resultado.getGrupo()); // 20:00 hs
    assertEquals(2L, resultado.getCantidad());
  }

  @Test
  @DisplayName("Calcula cantidad de hechos excluyendo eliminados")
  public void estadisticasCantidadHechos() {
    Estadistica resultado = calculadora.calcularStatsCantHechos();
    assertEquals(3L, resultado.getCantidad());
  }

  @Test
  @DisplayName("Calcula cantidad de solicitudes pendientes")
  public void estadisticasCantidadSolicitudesPendientes() {
    Estadistica resultado = calculadora.calcularStatsCantSolicitudes();
    assertEquals(2L, resultado.getCantidad());
  }

  @Test
  @DisplayName("Calcula cantidad de solicitudes spam")
  public void estadisticasCantidadSpam() {
    Estadistica resultado = calculadora.calcularStatsCantSpam();
    assertEquals(1L, resultado.getCantidad());
  }

  @Test
  @DisplayName("Exporta una lista de estadísticas a un archivo CSV correctamente")
  public void seExportaCorrectamente() throws IOException {
    List<Estadistica> datos = calculadora.hechosPorCategoria();
    Path outputPath = tempDir.resolve("export_test.csv");
    calculadora.exportar(datos, outputPath.toString());

    File exportedFile = outputPath.toFile();
    assertTrue(exportedFile.exists(), "El archivo CSV no fue creado.");

    List<String> lines = Files.readAllLines(exportedFile.toPath());

    assertTrue(lines.size() > 1);
    String header = lines.get(0);
    assertTrue(header.contains("ESTADISTICA_CATEGORIA"));
    assertTrue(lines.stream().anyMatch(line -> line.contains("Hurtos")));
    assertTrue(lines.stream().anyMatch(line -> line.contains("Robos")));
  }
}