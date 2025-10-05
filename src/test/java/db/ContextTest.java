package db;


import io.github.flbulgarelli.jpa.extras.test.SimplePersistenceTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ContextTest implements SimplePersistenceTest {

  @Test
  void contextUp() {
    assertNotNull(entityManager());
  }

  @Test
  void contextUpWithTransaction() throws Exception {
    withTransaction(() -> {
    });
  }
  @Test
  void imprimirTimeZoneDeLaJVM() {
    System.out.println("================== PRUEBA DE DIAGNÓSTICO ==================");
    String timeZone = System.getProperty("user.timezone");
    System.out.println("La propiedad 'user.timezone' de la JVM es: " + timeZone);
    System.out.println("=========================================================");
    // Este test no necesita aserciones, solo queremos ver la salida.
  }

}
