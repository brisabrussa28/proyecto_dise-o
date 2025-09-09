package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.*;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.HechoBuilder;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import ar.edu.utn.frba.dds.domain.serviciodebackup.ServicioDeBackup;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioDeBackupTest {

  private ServicioDeBackup servicio;
  private Path tempJsonFilePath;

  @BeforeEach
  void setUp() throws IOException {
    tempJsonFilePath = Files.createTempFile("test_copias_locales", ".json");
    servicio = new ServicioDeBackup(tempJsonFilePath.toString());
  }

  @AfterEach
  void tearDown() throws IOException {
    Files.deleteIfExists(tempJsonFilePath);
  }

  @Test
  void testGuardarYCargarCopiaHechos() throws IOException {
    List<Hecho> originalHechos = new ArrayList<>();
    originalHechos.add(new HechoBuilder()
        .conTitulo("Incendio en Fábrica")
        .conDescripcion("Incendio de grandes proporciones en una fábrica textil.")
        .conCategoria("Incendios")
        .conDireccion("Av. Siempreviva 742")
        .conProvincia("Provincia Test")
        .conUbicacion(new PuntoGeografico(-34.6037, -58.3816))
        .conFechaSuceso(LocalDateTime.of(2024, 6, 20, 10, 30, 0))
        .conFechaCarga(LocalDateTime.of(2024, 6, 20, 11, 0, 0))
        .conFuenteOrigen(Origen.DATASET)
        .conEtiquetas(List.of("fuego", "fabrica", "emergencia"))
        .build()
    );
    originalHechos.add(new HechoBuilder()
        .conTitulo("Accidente de Tráfico")
        .conDescripcion("Colisión múltiple en la autopista.")
        .conCategoria("Accidentes")
        .conDireccion("Autopista 25 de Mayo KM 5")
        .conProvincia("Provincia Test")
        .conUbicacion(new PuntoGeografico(-34.6000, -58.4000))
        .conFechaSuceso(LocalDateTime.of(2024, 6, 21, 15, 0, 0))
        .conFechaCarga(LocalDateTime.of(2024, 6, 21, 15, 15, 0))
        .conFuenteOrigen(Origen.DATASET)
        .conEtiquetas(List.of("trafico", "accidente", "autopista"))
        .build()
    );

    servicio.guardarCopiaLocalJson(originalHechos);

    List<Hecho> loadedHechos = servicio.cargarCopiaLocalJson(new TypeReference<List<Hecho>>() {});

    assertNotNull(loadedHechos);
    assertEquals(originalHechos.size(), loadedHechos.size());

    assertEquals(originalHechos.get(0).getTitulo(), loadedHechos.get(0).getTitulo());
    assertEquals(originalHechos.get(0).getDescripcion(), loadedHechos.get(0).getDescripcion());
    assertEquals(originalHechos.get(0).getFechaSuceso(), loadedHechos.get(0).getFechaSuceso());

    assertEquals(originalHechos.get(1).getTitulo(), loadedHechos.get(1).getTitulo());
    assertEquals(originalHechos.get(1).getDescripcion(), loadedHechos.get(1).getDescripcion());
    assertEquals(originalHechos.get(1).getFechaSuceso(), loadedHechos.get(1).getFechaSuceso());

    assertTrue(Files.exists(tempJsonFilePath));
    assertTrue(Files.size(tempJsonFilePath) > 0);
  }

  @Test
  void testCargarCopiaHechosArchivoNoExistente() {
    try {
      Files.deleteIfExists(tempJsonFilePath);
    } catch (IOException e) {
      e.printStackTrace();
    }

    List<Hecho> loadedHechos = servicio.cargarCopiaLocalJson(new TypeReference<List<Hecho>>() {});

    assertNotNull(loadedHechos);
    assertTrue(loadedHechos.isEmpty());
  }

  @Test
  void testCargarCopiaHechosArchivoVacio() throws IOException {
    Files.write(tempJsonFilePath, new byte[0]);

    List<Hecho> loadedHechos = servicio.cargarCopiaLocalJson(new TypeReference<List<Hecho>>() {});

    assertNotNull(loadedHechos);
    assertTrue(loadedHechos.isEmpty());
  }

  @Test
  void testGuardarCopiaHechosListaVacia() throws IOException {
    List<Hecho> emptyList = new ArrayList<>();

    servicio.guardarCopiaLocalJson(emptyList);

    List<Hecho> loadedHechos = servicio.cargarCopiaLocalJson(new TypeReference<List<Hecho>>() {});

    assertNotNull(loadedHechos);
    assertTrue(loadedHechos.isEmpty());

    assertTrue(Files.exists(tempJsonFilePath));
    assertTrue(Files.size(tempJsonFilePath) > 0);
  }

  @Test
  void testGuardarCopiaLocalJsonGenerico() {
    List<String> originalStrings = new ArrayList<>();
    originalStrings.add("Hello");
    originalStrings.add("World");

    servicio.guardarCopiaLocalJson(originalStrings);

    List<String> loadedStrings = servicio.cargarCopiaLocalJson(new TypeReference<List<String>>() {});

    assertNotNull(loadedStrings);
    assertEquals(originalStrings.size(), loadedStrings.size());
    assertEquals(originalStrings.get(0), loadedStrings.get(0));
    assertEquals(originalStrings.get(1), loadedStrings.get(1));
  }
}
