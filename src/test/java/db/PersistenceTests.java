package db;

import io.github.flbulgarelli.jpa.extras.test.SimplePersistenceTest;
import org.junit.jupiter.api.BeforeAll;
import java.util.TimeZone;

/**
 * Clase base abstracta para todos los tests que requieran interacción con la base de datos.
 * Configura la zona horaria correcta de forma centralizada antes de que se ejecuten los tests.
 */
public class PersistenceTests implements SimplePersistenceTest {
  /**
   * Este método se ejecuta una sola vez antes de todos los tests en las clases que hereden de esta.
   * Establece la zona horaria por defecto para la JVM para evitar problemas de compatibilidad
   * entre el sistema operativo y la base de datos.
   */
  @BeforeAll
  public static void setUpAll() {
    TimeZone.setDefault(TimeZone.getTimeZone("America/Argentina/Buenos_Aires"));
  }
}
