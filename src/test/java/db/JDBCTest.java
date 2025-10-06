package db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ar.edu.utn.frba.dds.domain.coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.estadisicas.CentralDeEstadisticas;
import ar.edu.utn.frba.dds.domain.estadisicas.Estadistica;
import ar.edu.utn.frba.dds.domain.exportador.Exportador;
import ar.edu.utn.frba.dds.domain.exportador.csv.ExportadorCSV;
import ar.edu.utn.frba.dds.domain.exportador.csv.modoexportacion.ModoSobrescribir;
import ar.edu.utn.frba.dds.domain.filtro.Filtro;
import ar.edu.utn.frba.dds.domain.filtro.condiciones.CondicionTrue;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.HechoBuilder;
import ar.edu.utn.frba.dds.domain.hecho.Origen;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.reportes.RepositorioDeSolicitudes;
import ar.edu.utn.frba.dds.domain.reportes.detectorspam.DetectorSpam;
import ar.edu.utn.frba.dds.domain.repos.ColeccionRepository;
import ar.edu.utn.frba.dds.domain.repos.EstadisticaRepository;
import ar.edu.utn.frba.dds.domain.repos.FuentesRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests que verifican la correcta persistencia de diferentes entidades del dominio.
 * Hereda de PersistenceTests para obtener el manejo de transacciones y la configuración de la BD.
 */
public class JDBCTest extends PersistenceTests {

  private ColeccionRepository repo = new ColeccionRepository();

  @BeforeEach
  public void setUp() {
    final DetectorSpam detector = texto -> texto.contains("Troll");
    var fuente = new FuenteDinamica("Fuente para Estadísticas");

    var ubicacion = new PuntoGeografico(50, -80);
    LocalDateTime hora = LocalDateTime.now();

    var hecho1 = new HechoBuilder()
        .conTitulo("Robo en Almagro")
        .conCategoria("Robos")
        .conProvincia("CABA")
        .conFechaSuceso(hora.minusHours(1))
        .conOrigen(Origen.PROVISTO_CONTRIBUYENTE)
        .conUbicacion(ubicacion)
        .build();

    var hecho2 = new HechoBuilder()
        .conTitulo("Robo en Caballito")
        .conCategoria("Robos")
        .conProvincia("CABA")
        .conFechaSuceso(hora.minusHours(1))
        .conOrigen(Origen.PROVISTO_CONTRIBUYENTE)
        .conUbicacion(ubicacion)
        .build();

    var hecho3 = new HechoBuilder()
        .conTitulo("Hurto en Avellaneda")
        .conCategoria("Hurtos")
        .conProvincia("Buenos Aires")
        .conFechaSuceso(hora.minusHours(2))
        .conOrigen(Origen.PROVISTO_CONTRIBUYENTE)
        .conUbicacion(ubicacion)
        .build();

    fuente.agregarHecho(hecho1);
    fuente.agregarHecho(hecho2);
    fuente.agregarHecho(hecho3);

    FuentesRepository repoFuentes = new FuentesRepository();
    repoFuentes.save(fuente);

    var solicitudes = new RepositorioDeSolicitudes(detector);
    var calculadora = new CentralDeEstadisticas();
    calculadora.setRepo(solicitudes);

    Exportador<Estadistica> exportadorCsv = new ExportadorCSV<>(new ModoSobrescribir());
    calculadora.setExportador(exportadorCsv);

    var coleccion = new Coleccion(
        "Coleccion de Hechos",
        fuente,
        "Descripcion de prueba",
        "General"
    );
    repo.save(coleccion);
  }

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
    FuenteDinamica fuenteRecuperada = find(FuenteDinamica.class, 2L); // Asumiendo que el ID es 1
    assertNotNull(fuenteRecuperada);
    assertFalse(fuenteRecuperada.obtenerHechos()
                                .isEmpty());
    assertEquals(
        "Robo",
        fuenteRecuperada.obtenerHechos()
                        .get(0)
                        .getTitulo()
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
    assertEquals("Robos en BA", coleccionRecuperada.getTitulo());
  }

  @Test
  @DisplayName("Genero estadisticas y las puedo visualizar en la db")
  public void estadisticaDBTest() {
    var calculadora = new CentralDeEstadisticas();
    calculadora.setFiltro(new Filtro(new CondicionTrue()));
    List<Coleccion> coleccionDB = repo.findAll();
    //System.out.println(coleccionDB.toString());
    var stat = calculadora.categoriaConMasHechos(coleccionDB);
    var repoStat = new EstadisticaRepository();
    repoStat.save(stat);
    Coleccion coleccion = repo.findById(1L);
    var stat2 = calculadora.provinciaConMasHechos(coleccion);
    repoStat.save(stat2);
  }
}
