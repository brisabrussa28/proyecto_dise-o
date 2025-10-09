package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ar.edu.utn.frba.dds.domain.exportador.json.ExportadorJson;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.HechoBuilder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ExportadorJsonTest {

  private ExportadorJson<Hecho> exportador;
  private ObjectMapper mapper;

  // carpeta temporal se elimina automáticamente.
  @TempDir
  Path tempDir;

  @BeforeEach
  void setUp() {
    exportador = new ExportadorJson<>();
    mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
  }

  @Test
  @DisplayName("Exporta una lista de hechos a un archivo JSON correctamente")
  void exportarListaDeHechosCorrectamente() throws IOException {
    LocalDateTime fechaSuceso = LocalDateTime.now().minusDays(1);
    List<Hecho> hechos = List.of(
        new HechoBuilder()
            .conTitulo("Hecho de prueba 1")
            .conFechaSuceso(fechaSuceso)
            .build(),
        new HechoBuilder()
            .conTitulo("Hecho de prueba 2")
            .conFechaSuceso(fechaSuceso.plusHours(1))
            .build()
    );
    Path outputPath = tempDir.resolve("hechos.json");

    System.out.println(outputPath);

    exportador.exportar(hechos, outputPath.toString());


    File outputFile = outputPath.toFile();
    assertTrue(outputFile.exists(), "El archivo de salida debería estar creado");

    List<Hecho> hechosLeidos = mapper.readValue(outputFile, new TypeReference<List<Hecho>>() {});
    assertEquals(2, hechosLeidos.size());
    assertEquals("Hecho de prueba 1", hechosLeidos.get(0).getTitulo());
    assertEquals(fechaSuceso, hechosLeidos.get(0).getFechasuceso());
  }

  @Test
  @DisplayName("Exporta una lista vacia a un archivo JSON")
  void exportarListaVacia() throws IOException {

    List<Hecho> listaVacia = Collections.emptyList();
    Path outputPath = tempDir.resolve("vacio.json");

    exportador.exportar(listaVacia, outputPath.toString());

    File outputFile = outputPath.toFile();
    assertTrue(outputFile.exists());

    List<Hecho> hechosLeidos = mapper.readValue(outputFile, new TypeReference<List<Hecho>>() {});
    assertTrue(hechosLeidos.isEmpty(), "El archivo JSON debería tener una lista vacía.");
  }

  @Test
  @DisplayName("No lanza excepcion si la ruta del archivo es inválida")
  void noLanzaExcepcionConRutaInvalida() {
    List<Hecho> hechos = List.of(
        new HechoBuilder().conTitulo("Test").conFechaSuceso(LocalDateTime.now().minusDays(1)).build()
    );
    String invalidPath = "/inválido/directorio/que/no/existe/test.json";

    exportador.exportar(hechos, invalidPath);
  }

}

