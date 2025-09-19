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
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.HechoBuilder;
import ar.edu.utn.frba.dds.domain.hecho.Origen;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.lector.Lector;
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
  CentralDeEstadisticas calculadora = new CentralDeEstadisticas();
  PuntoGeografico pg = new PuntoGeografico(33.0, 44.0);
  LocalDateTime hora = LocalDateTime.now();
  List<String> etiquetas = List.of("#robo");
  Hecho hecho;
  private RepositorioDeSolicitudes repo;
  private UUID solicitante;
  private final DetectorSpam detector = texto -> texto.contains("Troll");
  FuenteDinamica fuenteAuxD;
  LocalDateTime horaAux = LocalDateTime.now().minusDays(1);
  List<String> etiquetasAux = List.of(
      "#ancianita",
      "#robo_a_mano_armada",
      "#violencia"
  );
  Coleccion coleccion;
  List<Coleccion> colecciones = new ArrayList<>();
  Hecho hecho2;
  Lector<Hecho> lectorMock;
  Exportador<Hecho> exportadorMock;

  @TempDir
  Path tempDir;

  @BeforeEach
  public void setUp() throws IOException {
    lectorMock = mock(Lector.class);
    exportadorMock = mock(Exportador.class);
    when(lectorMock.getConfiguracionJson()).thenReturn("{}");

    hecho = new HechoBuilder()
        .conTitulo("titulo")
        .conDescripcion("desc")
        .conCategoria("Robos")
        .conDireccion("direccion")
        .conProvincia("Provincia")
        .conUbicacion(pg)
        .conFechaSuceso(hora)
        .conFechaCarga(hora)
        .conFuenteOrigen(Origen.PROVISTO_CONTRIBUYENTE)
        .conEtiquetas(etiquetas)
        .build();

    hecho2 = new HechoBuilder()
        .conTitulo("titulo")
        .conDescripcion("desc")
        .conCategoria("Robos")
        .conDireccion("direccion")
        .conProvincia("Provincia")
        .conUbicacion(null)
        .conFechaSuceso(horaAux)
        .conFechaCarga(LocalDateTime.now())
        .conFuenteOrigen(Origen.DATASET)
        .conEtiquetas(etiquetasAux)
        .build();


    repo = new RepositorioDeSolicitudes(detector);
    calculadora.setRepo(repo); // Asegurarse que la calculadora usa el repo
    solicitante = UUID.randomUUID();
    Path tempJsonFile = tempDir.resolve("test_fuente_dinamica.json");
    fuenteAuxD = new FuenteDinamica("Julio Cesar", tempJsonFile.toString(), lectorMock, exportadorMock);
    fuenteAuxD.agregarHecho(hecho2);

    coleccion = new Coleccion("Robos", fuenteAuxD, "Descripcion", "Robos");
    colecciones.add(coleccion);
  }

  @Test
  public void estadisticasSpam() {
    repo.agregarSolicitud(solicitante, hecho, "motivo1".repeat(100));
    repo.agregarSolicitud(solicitante, hecho, "motivo2".repeat(100));
    repo.agregarSolicitud(solicitante, hecho, "motivo3".repeat(100));
    repo.agregarSolicitud(solicitante, hecho, "motivo4".repeat(100));
    repo.agregarSolicitud(solicitante, hecho, "Troll".repeat(100));

    assertEquals(20, calculadora.porcentajeDeSolicitudesSpam(), 0.01);
  }

  @Test
  public void estadisticasDeProvinciaConMasHechos() {
    Estadistica resultado = calculadora.provinciaConMasHechos(coleccion);
    assertNotNull(resultado);
    assertEquals("Provincia", resultado.getDimension());
    assertEquals(1L, resultado.getValor());
  }

  @Test
  public void estadisticasCategoriaConMasHechos() {
    Estadistica resultado = calculadora.categoriaConMasHechos(colecciones);
    assertNotNull(resultado);
    assertEquals("Robos", resultado.getDimension());
    assertEquals(1L, resultado.getValor());
  }

  @Test
  public void estadisticasHechosDeCiertaCategoria() {
    Estadistica resultado = calculadora.provinciaConMasHechosDeCiertaCategoria(colecciones, "Robos");
    assertNotNull(resultado);
    assertEquals("Provincia", resultado.getDimension());
    assertEquals(1L, resultado.getValor());
  }

  @Test
  public void estadisticasHoraConMasHechosDeCiertaCategoria() {
    Estadistica resultado = calculadora.horaConMasHechosDeCiertaCategoria(colecciones, "Robos");
    String horaEsperada = String.format("%02d", horaAux.getHour());
    assertNotNull(resultado);
    assertEquals(horaEsperada, resultado.getDimension());
    assertEquals(1L, resultado.getValor());
  }

  @Test
  public void seExportaCorrectamente() throws IOException {
    // Arrange: Obtener los datos a exportar
    List<Estadistica> datos = calculadora.hechosPorCategoria(colecciones);
    Path outputPath = tempDir.resolve("export_test.csv");

    // Act: Exportar los datos
    calculadora.export(datos, outputPath.toString());

    // Assert: Verificar que el archivo fue creado y tiene el contenido correcto
    File[] files = tempDir.toFile().listFiles((dir, name) -> name.startsWith("export_test") && name.endsWith(".csv"));
    assertNotNull(files);
    assertEquals(1, files.length);
    File exportedFile = files[0];
    assertTrue(exportedFile.exists());

    List<String> lines = Files.readAllLines(exportedFile.toPath());
    assertEquals(2, lines.size()); // Cabecera + 1 fila de datos
    assertEquals("\"DIMENSION\",\"VALOR\"", lines.get(0));
    assertEquals("\"Robos\",\"1\"", lines.get(1));
  }
}

