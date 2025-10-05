package db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.Origen;
import ar.edu.utn.frba.dds.domain.hecho.etiqueta.Etiqueta;
import ar.edu.utn.frba.dds.domain.hibernate.AccesoHecho;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests para la funcionalidad de búsqueda full-text sobre la entidad Hecho.
 * Hereda de PersistenceTests para obtener el manejo de transacciones y la configuración de la BD.
 */
public class TestBusquedaLibre extends PersistenceTests {
  private AccesoHecho accesoHecho;

  @BeforeEach
  public void setUp() {
    // La transacción se inicia automáticamente por la clase base PersistenceTests.
    // Solo inicializamos el objeto de acceso a datos.
    accesoHecho = new AccesoHecho(entityManager());
  }

  /**
   * Método helper para crear y persistir un conjunto de datos de prueba limpios.
   * Se llama desde cada test para garantizar el aislamiento.
   */
  private void persistirHechosDePrueba() {
    Hecho roboBanco = new Hecho(
        "Robo a mano armada en banco céntrico", "Tres individuos armados ingresaron...", "Delito",
        "Av. Corrientes 1234", "Buenos Aires", new PuntoGeografico(-34.6037, -58.3816),
        LocalDateTime.of(2025, 9, 10, 14, 30), LocalDateTime.now(), Origen.PROVISTO_CONTRIBUYENTE,
        List.of(new Etiqueta("robo"), new Etiqueta("arma"), new Etiqueta("banco"))
    );

    Hecho asaltoComercio = new Hecho(
        "Asalto a mano armada en comercio local", "Un delincuente armado ingresó...", "Seguridad ciudadana",
        "Calle San Martín 456", "Santa Fe", new PuntoGeografico(-31.6333, -60.7000),
        LocalDateTime.of(2025, 9, 9, 18, 15), LocalDateTime.now(), Origen.PROVISTO_CONTRIBUYENTE,
        List.of(new Etiqueta("robo"), new Etiqueta("comercio"), new Etiqueta("arma"))
    );

    Hecho intentoRobo = new Hecho(
        "Intento de robo a mano armada frustrado por vecinos", "Un grupo de vecinos logró detener a un ladrón armado...", "Delito",
        "Pasaje Mitre 789", "Mendoza", new PuntoGeografico(-32.8908, -68.8272),
        LocalDateTime.of(2025, 9, 8, 21, 0), LocalDateTime.now(), Origen.PROVISTO_CONTRIBUYENTE,
        List.of(new Etiqueta("robo"), new Etiqueta("arma"), new Etiqueta("vecinos"))
    );

    // Persistimos los datos. Se limpiarán con el rollback automático del framework de tests.
    persist(roboBanco);
    persist(asaltoComercio);
    persist(intentoRobo);

    // Forzamos la sincronización con la BD y la indexación para que la búsqueda los encuentre.
    entityManager().flush();
  }

  @Test
  public void encuentraResultadoBienEscrito() {
    // 1. Crear un entorno limpio para este test
    persistirHechosDePrueba();

    // 2. Ejecutar la lógica del test
    // La búsqueda "ladrón armado" es específica y solo debe coincidir con un documento.
    List<Hecho> resultados = accesoHecho.fullTextSearch("ladrón armado", 10);
    mostrarResultados(resultados);

    // 3. Verificar los resultados
    assertFalse(resultados.isEmpty());
    assertEquals(1, resultados.size());
    assertEquals("Intento de robo a mano armada frustrado por vecinos", resultados.get(0).getTitulo());
  }

  @Test
  public void encuentraResultadoConBusquedaMalEscrita() {
    // 1. Crear un entorno limpio para este test
    persistirHechosDePrueba();

    // 2. Ejecutar la lógica del test
    // La búsqueda fuzzy "armad0" debería encontrar "armado" y "armada".
    // Debe encontrar los 3 hechos, ya que todos contienen esa palabra.
    List<Hecho> resultados = accesoHecho.fullTextSearch("armad0", 10);
    mostrarResultados(resultados);

    // 3. Verificar los resultados
    assertFalse(resultados.isEmpty());
    assertEquals(1, resultados.size());
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

