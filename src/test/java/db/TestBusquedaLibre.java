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
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestBusquedaLibre {
  private AccesoHecho accesoHecho;
  private EntityManagerFactory emf;
  private EntityManager em;

  //Hechos usados para los test
  Hecho roboBanco = new Hecho(
      "Robo a mano armada en banco céntrico",
      "Tres individuos armados ingresaron al Banco Nación y se llevaron una suma millonaria",
      "Delito",
      "Av. Corrientes 1234",
      "Buenos Aires",
      new PuntoGeografico(-34.6037, -58.3816),
      LocalDateTime.of(2025, 9, 10, 14, 30),
      LocalDateTime.now(),
      Origen.PROVISTO_CONTRIBUYENTE,
      List.of(new Etiqueta("robo"), new Etiqueta("arma"), new Etiqueta("banco"))
  );

  Hecho asaltoComercio = new Hecho(
      "Asalto a mano armada en comercio local",
      "Un delincuente armado ingresó a una ferretería y amenazó al dueño, robo todo el dinero en la caja.",
      "Seguridad ciudadana",
      "Calle San Martín 456",
      "Santa Fe",
      new PuntoGeografico(-31.6333, -60.7000),
      LocalDateTime.of(2025, 9, 9, 18, 15),
      LocalDateTime.now(),
      Origen.PROVISTO_CONTRIBUYENTE,
      List.of(new Etiqueta("robo"), new Etiqueta("comercio"), new Etiqueta("arma"))
  );

  Hecho intentoRobo = new Hecho(
      "Intento de robo a mano armada frustrado por vecinos",
      "Un grupo de vecinos logró detener a un ladrón armado antes de que escapara luego de que robara la casa de una vecina.",
      "Delito",
      "Pasaje Mitre 789",
      "Mendoza",
      new PuntoGeografico(-32.8908, -68.8272),
      LocalDateTime.of(2025, 9, 8, 21, 0),
      LocalDateTime.now(),
      Origen.PROVISTO_CONTRIBUYENTE,
      List.of(new Etiqueta("robo"), new Etiqueta("arma"), new Etiqueta("vecinos"))
  );

  Hecho explosionBuenosAires = new Hecho(
      "Explosión en fábrica armamentística en zona industrial",
      "Una explosión afectó las instalaciones de una planta de producción de municiones",
      "Industria",
      "Camino del Buen Ayre km 32",
      "Buenos Aires",
      new PuntoGeografico(-34.5200, -58.7000),
      LocalDateTime.of(2025, 9, 7, 3, 45),
      LocalDateTime.now(),
      Origen.PROVISTO_CONTRIBUYENTE,
      List.of(new Etiqueta("explosión"), new Etiqueta("fábrica"), new Etiqueta("armamento"))
  );

  Hecho explosionCordoba = new Hecho(
      "Explosión en fábrica armamentística en Córdoba",
      "Una serie de explosiones ocurrieron en la Fábrica Militar «Río Tercero» causando estragos en los barrios " +
          "aledaños",
      "Explosiones",
      "Mendoza s/n, X5850",
      "Córdoba",
      new PuntoGeografico(-31.4000, -64.2000),
      LocalDateTime.of(1995, 11, 3, 9, 00),
      LocalDateTime.now(),
      Origen.PROVISTO_CONTRIBUYENTE,
      List.of(new Etiqueta("explosión"), new Etiqueta("fábrica"), new Etiqueta("armamento"), new Etiqueta("Córdoba"))
  );



  @BeforeEach //Se ejecuta antes de cada test
  public void setUp() {
    emf = Persistence.createEntityManagerFactory("simple-persistence-unit");
    em = emf.createEntityManager();
    accesoHecho = new AccesoHecho(em);
    accesoHecho.guardar(roboBanco);
    accesoHecho.guardar(intentoRobo);
    accesoHecho.guardar(asaltoComercio);
    accesoHecho.guardar(explosionBuenosAires);
    accesoHecho.guardar(explosionCordoba);
  }

  @AfterEach
  public void tearDown() {
    em.close();
    emf.close();
  }

  @Test
  public void encuentraResultadoBienEscrito() {
    List<Hecho> resultados = accesoHecho.fullTextSearch("ladrón armado", 10);
    mostrarResultados(resultados);
    assertFalse(resultados.isEmpty());
    assertEquals("Intento de robo a mano armada frustrado por vecinos", resultados.get(0).getTitulo());
  }

  @Test
  public void encuentraResultadoConBusquedaMalEscrita() {
    List<Hecho> resultados = accesoHecho.fullTextSearch("l4dro armad0", 10);
    mostrarResultados(resultados);
    assertFalse(resultados.isEmpty());
    assertEquals(3, resultados.size());
  }

  private void mostrarResultados(List<Hecho> resultados){
    Logger logger = Logger.getLogger(TestBusquedaLibre.class.getName());
    logger.info("Resultados encontrados:");
    for (Hecho hecho : resultados) {
      logger.info("Título: " + hecho.getTitulo());
      logger.info("  Descripción: " + hecho.getDescripcion());
      logger.info("--------------------------------------------------");
    }
  }
}
