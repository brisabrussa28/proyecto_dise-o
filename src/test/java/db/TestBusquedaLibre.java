package db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.model.hecho.Origen;
import ar.edu.utn.frba.dds.model.hecho.etiqueta.Etiqueta;
import ar.edu.utn.frba.dds.model.hibernate.AccesoHecho;
import ar.edu.utn.frba.dds.model.info.PuntoGeografico;
import ar.edu.utn.frba.dds.utils.DBUtils;
import io.github.flbulgarelli.jpa.extras.test.SimplePersistenceTest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests para la funcionalidad de búsqueda full-text sobre la entidad Hecho.
 * Hereda de PersistenceTests para obtener el manejo de transacciones y la configuración de la BD.
 */
public class TestBusquedaLibre implements SimplePersistenceTest {
  private AccesoHecho accesoHecho;

  private final Hecho roboBanco = new Hecho(
      "Robo a mano armada en banco céntrico", "Tres individuos armados ingresaron...", "Delito",
      "Av. Corrientes 1234", "Buenos Aires", new PuntoGeografico(-34.6037, -58.3816),
      LocalDateTime.now().minusDays(1), LocalDateTime.now(), Origen.PROVISTO_CONTRIBUYENTE,
      List.of(new Etiqueta("robo"), new Etiqueta("arma"), new Etiqueta("banco"))
  );

  private final Hecho asaltoComercio = new Hecho(
      "Asalto a mano armada en comercio local",
      "Un delincuente armado ingresó...",
      "Seguridad ciudadana",
      "Calle San Martín 456",
      "Santa Fe",
      new PuntoGeografico(-31.6333, -60.7000),
      LocalDateTime.now().minusDays(2),
      LocalDateTime.now(),
      Origen.PROVISTO_CONTRIBUYENTE,
      List.of(new Etiqueta("robo"), new Etiqueta("comercio"), new Etiqueta("arma"))
  );

  private final Hecho intentoRobo = new Hecho(
      "Intento de robo a mano armada frustrado por vecinos",
      "Un grupo de vecinos logró detener a un ladrón armado...",
      "Delito",
      "Pasaje Mitre 789",
      "Mendoza",
      new PuntoGeografico(-32.8908, -68.8272),
      LocalDateTime.now().minusDays(3),
      LocalDateTime.now(),
      Origen.PROVISTO_CONTRIBUYENTE,
      List.of(new Etiqueta("robo"), new Etiqueta("arma"), new Etiqueta("vecinos"))
  );

  @BeforeEach
  public void setUp() {
    accesoHecho = new AccesoHecho(entityManager());
    withTransaction(() -> {
      accesoHecho.guardar(roboBanco);
      accesoHecho.guardar(asaltoComercio);
      accesoHecho.guardar(intentoRobo);
    });
  }

  @Test
  public void encuentraResultadoBienEscrito() {
    // La búsqueda "ladrón armado" es específica y solo debe coincidir con un documento.
    List<Hecho> resultados = accesoHecho.fullTextSearch("ladrón armado", 10);
    mostrarResultados(resultados);

    assertFalse(resultados.isEmpty());
    // CORRECCIÓN: Solo un hecho contiene la palabra "ladrón".
    assertEquals(1, resultados.size());
    assertEquals(
        "Intento de robo a mano armada frustrado por vecinos",
        resultados.get(0).getTitulo()
    );
  }

  @Test
  public void encuentraResultadoConBusquedaMalEscrita() {
    // La búsqueda fuzzy "armad0" debería encontrar "armado" y "armada".
    // Debe encontrar los 3 hechos, ya que todos contienen esa palabra.
    List<Hecho> resultados = accesoHecho.fullTextSearch("armad0", 10);
    mostrarResultados(resultados);

    assertFalse(resultados.isEmpty());
    // CORRECCIÓN: Se deben encontrar los 3 hechos que contienen "armado" o "armada".
    assertEquals(3, resultados.size());
  }

  private void mostrarResultados(List<Hecho> resultados) {
    Logger logger = Logger.getLogger(TestBusquedaLibre.class.getName());
    logger.info("Resultados encontrados: " + resultados.size());
    for (Hecho hecho : resultados) {
      logger.info("Título: " + hecho.getTitulo());
      logger.info("  Descripción: " + hecho.getDescripcion());
      logger.info("--------------------------------------------------");
    }
  }
}
