package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ar.edu.utn.frba.dds.domain.coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.estadisicas.CentralDeEstadisticas;
import ar.edu.utn.frba.dds.domain.estadisicas.Estadistica;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.HechoBuilder;
import ar.edu.utn.frba.dds.domain.reportes.RepositorioDeSolicitudes;
import ar.edu.utn.frba.dds.domain.reportes.detectorspam.DetectorSpam;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class EstadisticasTest {
  private CentralDeEstadisticas calculadora;
  private RepositorioDeSolicitudes repo;
  private FuenteDinamica fuente;
  private Coleccion coleccion;
  private List<Coleccion> colecciones;
  private Hecho hecho1, hecho2, hecho3;
  private final DetectorSpam detector = texto -> texto.contains("Troll");

  @TempDir
  Path tempDir; // Para el test de exportación

  @BeforeEach
  public void setUp() {
    // CORRECCIÓN: FuenteDinamica se instancia con su nuevo constructor simple.
    // Ya no necesita mocks de Lector, Exportador ni un path a un archivo.
    fuente = new FuenteDinamica("Fuente para Estadísticas");

    LocalDateTime hora = LocalDateTime.now();
    hecho1 = new HechoBuilder()
        .conCategoria("Robos")
        .conProvincia("CABA")
        .conFechaSuceso(hora.minusHours(1))
        .build();

    hecho2 = new HechoBuilder()
        .conCategoria("Robos")
        .conProvincia("CABA")
        .conFechaSuceso(hora.minusHours(1))
        .build();

    hecho3 = new HechoBuilder()
        .conCategoria("Hurtos")
        .conProvincia("PBA")
        .conFechaSuceso(hora.minusHours(2))
        .build();

    fuente.agregarHecho(hecho1);
    fuente.agregarHecho(hecho2);
    fuente.agregarHecho(hecho3);

    repo = new RepositorioDeSolicitudes(detector);
    calculadora = new CentralDeEstadisticas();
    calculadora.setRepo(repo);

    coleccion = new Coleccion("Coleccion de Hechos", fuente, "Descripcion de prueba", "General");
    colecciones = new ArrayList<>();
    colecciones.add(coleccion);
  }

  @Test
  public void estadisticasSpam() {
    UUID solicitante = UUID.randomUUID();
    repo.agregarSolicitud(solicitante, hecho1, "motivo bueno");
    repo.agregarSolicitud(solicitante, hecho1, "otro motivo bueno");
    repo.agregarSolicitud(solicitante, hecho1, "Este es un comentario Troll");

    // 1 de 3 solicitudes es spam (33.33%)
    assertEquals(33.33, calculadora.porcentajeDeSolicitudesSpam(), 0.01);
  }

  @Test
  public void estadisticasDeProvinciaConMasHechos() {
    Estadistica resultado = calculadora.provinciaConMasHechos(coleccion);
    assertNotNull(resultado);
    assertEquals("CABA", resultado.getDimension());
    assertEquals(2, resultado.getValor());
  }

  @Test
  public void estadisticasCategoriaConMasHechos() {
    Estadistica resultado = calculadora.categoriaConMasHechos(colecciones);
    assertNotNull(resultado);
    assertEquals("Robos", resultado.getDimension());
    assertEquals(2, resultado.getValor());
  }

  @Test
  public void estadisticasHechosDeCiertaCategoria() {
    Estadistica resultado = calculadora.provinciaConMasHechosDeCiertaCategoria(colecciones, "Robos");
    assertNotNull(resultado);
    assertEquals("CABA", resultado.getDimension());
    assertEquals(2, resultado.getValor());
  }

  @Test
  public void estadisticasHoraConMasHechosDeCiertaCategoria() {
    Estadistica resultado = calculadora.horaConMasHechosDeCiertaCategoria(colecciones, "Robos");
    String horaEsperada = String.format("%02d", LocalDateTime.now().minusHours(1).getHour());
    assertNotNull(resultado);
    assertEquals(horaEsperada, resultado.getDimension());
    assertEquals(2, resultado.getValor());
  }

  @Test
  public void seExportaCorrectamente() throws IOException {
    List<Estadistica> datos = calculadora.hechosPorCategoria(colecciones);
    Path outputPath = tempDir.resolve("export_test.csv");

    calculadora.export(datos, outputPath.toString());

    File exportedFile = outputPath.toFile();
    assertTrue(exportedFile.exists());
    assertTrue(exportedFile.length() > 0);

    List<String> lines = Files.readAllLines(exportedFile.toPath());
    assertEquals(3, lines.size()); // Cabecera + 2 filas de datos
    assertEquals("\"DIMENSION\",\"VALOR\"", lines.get(0));
    assertTrue(lines.contains("\"Hurtos\",\"1\""));
    assertTrue(lines.contains("\"Robos\",\"2\""));
  }
}