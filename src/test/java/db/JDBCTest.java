package db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ar.edu.utn.frba.dds.domain.coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.estadisticas.CentralDeEstadisticas;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.HechoBuilder;
import ar.edu.utn.frba.dds.domain.hecho.Origen;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.reportes.GestorDeSolicitudes;
import ar.edu.utn.frba.dds.domain.reportes.RepositorioDeSolicitudes;
import ar.edu.utn.frba.dds.domain.repos.ColeccionRepository;
import ar.edu.utn.frba.dds.domain.repos.EstadisticaRepository;
import ar.edu.utn.frba.dds.domain.repos.HechoRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests que verifican la correcta persistencia de diferentes entidades del dominio.
 * Hereda de PersistenceTests para obtener el manejo de transacciones y la configuración de la BD.
 */
public class JDBCTest extends PersistenceTests {
  private ColeccionRepository repo = new ColeccionRepository();
  private HechoRepository hechoRepo = new HechoRepository();

  @Test
  @DisplayName("Puedo persistir un Hecho en una FuenteDinamica")
  public void puedoPersistirUnHechoEnFuenteDinamica() {
    withTransaction(() -> {
      FuenteDinamica fuente = new FuenteDinamica("Fuente de prueba para persistencia");
      PuntoGeografico ubicacion = new PuntoGeografico(33.0, 44.0);

      Hecho hecho = new HechoBuilder()
          .conTitulo("Robo")
          .conDescripcion("Robo a mano armada")
          .conUbicacion(ubicacion)
          .conFechaSuceso(LocalDateTime.now()
                                       .minusDays(5))
          .conFuenteOrigen(Origen.PROVISTO_CONTRIBUYENTE)
          .build();
      fuente.agregarHecho(hecho);
      persist(fuente); // Usamos el método 'persist' heredado de la librería
    });

    // Verificación (opcional, fuera de la transacción)
    FuenteDinamica fuenteRecuperada = find(FuenteDinamica.class, 1L); // Asumiendo que el ID es 1
    assertNotNull(fuenteRecuperada);
    assertFalse(fuenteRecuperada.obtenerHechos()
                                .isEmpty());
    assertEquals(
        "Robo",
        fuenteRecuperada.obtenerHechos()
                        .get(0)
                        .getHecho_titulo()
    );
  }

  @Test
  @DisplayName("Puedo persistir y recuperar una Coleccion")
  public void coleccionRepositoryTest() {
    withTransaction(() -> {
      FuenteDinamica fuente = new FuenteDinamica("Fuente para colección");
      persist(fuente);

      Coleccion coleccionBonaerense = new Coleccion(
          "Robos en BA",
          fuente,
          "Hechos delictivos en Buenos Aires",
          "Robos"
      );
      persist(coleccionBonaerense);
    });

    Coleccion coleccionRecuperada = find(Coleccion.class, 1L); // Asumiendo que el ID es 1
    assertNotNull(coleccionRecuperada);
    assertEquals("Robos en BA", coleccionRecuperada.getColeccion_titulo());
  }

  @Test
  @DisplayName("Genero estadisticas y las puedo visualizar en la db")
  public void estadisticaDBTest() {
    var calculadora = new CentralDeEstadisticas();
    List<Coleccion> coleccionDB = repo.findAll();
    //System.out.println(coleccionDB.toString());
    calculadora.setGestor(new GestorDeSolicitudes(new RepositorioDeSolicitudes()));
    var stat = calculadora.categoriaConMasHechos(coleccionDB);
    var repoStat = new EstadisticaRepository();
    repoStat.save(stat);
    Coleccion coleccion = repo.findById(1L);
    var stat2 = calculadora.provinciaConMasHechos(coleccion);
    repoStat.save(stat2);
  }
}
