package db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.HechoBuilder;
import ar.edu.utn.frba.dds.domain.hecho.Origen;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.repo.FuentesRepository;
import java.time.LocalDateTime;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FuenteDinamicaJDBCTest {

  private EntityManagerFactory emf;
  private EntityManager em;
  private EntityTransaction tx;

  @Before
  public void setUp() {
    emf = Persistence.createEntityManagerFactory("simple-persistence-unit");
    em = emf.createEntityManager();
    tx = em.getTransaction();
  }

  @After
  public void tearDown() {
    if (em != null) {
      em.close();
    }
    if (emf != null) {
      emf.close();
    }
  }

  @Test
  public void puedoPersistirUnHechoEnFuenteDinamica() {
    // Create DataSource with settings matching persistence.xml
    BasicDataSource dataSource = new BasicDataSource();
    dataSource.setDriverClassName("org.postgresql.Driver");
    dataSource.setUrl("jdbc:postgresql://localhost:5432/ddstpa");
    dataSource.setUsername("postgres");
    dataSource.setPassword("1234abcd");

    // Initialize repository with datasource
    FuentesRepository repo = new FuentesRepository(dataSource);
    FuenteDinamica fuente = new FuenteDinamica("Fuente de prueba");
    PuntoGeografico ubicacion = new PuntoGeografico(33.0, 44.0);
    LocalDateTime fechaSuceso = LocalDateTime.now()
                                             .minusDays(5);
    LocalDateTime fechaCarga = LocalDateTime.now();

    Hecho hecho = new HechoBuilder()
        .conTitulo("Robo")
        .conDescripcion("Robo a mano armada")
        .conCategoria("DELITO")
        .conDireccion("Calle falsa 123")
        .conUbicacion(ubicacion)
        .conFechaSuceso(fechaSuceso)
        .conFechaCarga(fechaCarga)
        .conFuenteOrigen(Origen.PROVISTO_CONTRIBUYENTE)
        .build();
    hecho.setTitulo("Hecho de prueba");
    fuente.agregarHecho(hecho);
    repo.agregarFuente(fuente, em);
    // First save using JPA to set ID
//    tx.begin();
//    em.persist(fuente);
//    tx.commit();
//    em.clear();

    // Now you can test repository functionality if needed
    // For this to work, update agregarFuente in FuentesRepository to be public

    // Test retrieval via JPA
    FuenteDinamica fuenteRecuperada = em.find(FuenteDinamica.class, fuente.getId());

    assertNotNull("La fuente debe estar persistida", fuenteRecuperada);
    assertEquals("El nombre debe ser el mismo", "Fuente de prueba", fuenteRecuperada.getNombre());
    assertEquals(
        "Debe tener un hecho",
        1,
        fuenteRecuperada.obtenerHechos()
                        .size()
    );
    assertEquals(
        "El título del hecho debe ser el mismo", "Hecho de prueba",
        fuenteRecuperada.obtenerHechos()
                        .get(0)
                        .getTitulo()
    );
  }

}
