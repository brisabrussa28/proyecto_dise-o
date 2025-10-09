package db;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import ar.edu.utn.frba.dds.domain.coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.estadisticas.CentralDeEstadisticas;
import ar.edu.utn.frba.dds.domain.estadisticas.Estadistica;
import ar.edu.utn.frba.dds.domain.exportador.Exportador;
import ar.edu.utn.frba.dds.domain.exportador.csv.ExportadorCSV;
import ar.edu.utn.frba.dds.domain.exportador.csv.modoexportacion.ModoSobrescribir;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.HechoBuilder;
import ar.edu.utn.frba.dds.domain.hecho.Origen;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.reportes.GestorDeSolicitudes;
import ar.edu.utn.frba.dds.domain.reportes.RepositorioDeSolicitudes;
import ar.edu.utn.frba.dds.domain.reportes.detectorspam.DetectorSpam;
import ar.edu.utn.frba.dds.domain.repos.ColeccionRepository;
import ar.edu.utn.frba.dds.domain.repos.EstadisticaRepository;
import ar.edu.utn.frba.dds.domain.repos.FuenteRepository;
import ar.edu.utn.frba.dds.domain.repos.HechoRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests que verifican la correcta persistencia de diferentes entidades del dominio.
 * Hereda de PersistenceTests para obtener el manejo de transacciones y la configuración de la BD.
 */
public class JDBCTest extends PersistenceTests {
  private ColeccionRepository repoColeccion = new ColeccionRepository();
  private HechoRepository hechoRepo = new HechoRepository();
  private FuenteRepository fuenteRepo = new FuenteRepository();

  @BeforeEach
  public void setUp() {
    final DetectorSpam detector = texto -> texto.contains("Troll");
    var fuente = new FuenteDinamica("Fuente para Estadísticas");

    var ubicacion = new PuntoGeografico(-34.68415120338135, -58.58712709338028);
    LocalDateTime hora = LocalDateTime.now();

    var hecho1 = new HechoBuilder()
        .conTitulo("Robo en Almagro")
        .conCategoria("Robos")
        .conFechaSuceso(hora.minusHours(1))
        .conOrigen(Origen.PROVISTO_CONTRIBUYENTE)
        .conUbicacion(ubicacion)
        .build();

    var hecho2 = new HechoBuilder()
        .conTitulo("Robo en Caballito")
        .conCategoria("Robos")
        .conFechaSuceso(hora.minusHours(1))
        .conOrigen(Origen.PROVISTO_CONTRIBUYENTE)
        .conUbicacion(ubicacion)
        .build();

    var hecho3 = new HechoBuilder()
        .conTitulo("Hurto en Avellaneda")
        .conCategoria("Hurtos")
        .conFechaSuceso(hora.minusHours(2))
        .conOrigen(Origen.PROVISTO_CONTRIBUYENTE)
        .conUbicacion(ubicacion)
        .build();
    fuente.agregarHecho(hecho1);
    fuente.agregarHecho(hecho2);
    fuente.agregarHecho(hecho3);
    FuenteRepository repoFuentes = new FuenteRepository();
    repoFuentes.save(fuente);

    var solicitudes = new RepositorioDeSolicitudes();
    var calculadora = new CentralDeEstadisticas();

    Exportador<Estadistica> exportadorCsv = new ExportadorCSV<>(new ModoSobrescribir());
    calculadora.setExportador(exportadorCsv);

    var coleccion = new Coleccion(
        "Coleccion de Hechos",
        fuente,
        "Descripcion de prueba",
        "General"
    );
    repoColeccion.save(coleccion);
  }

  @Test
  @DisplayName("Puedo persistir un Hecho en una FuenteDinamica")
  public void puedoPersistirUnHechoEnFuenteDinamica() {
    FuenteDinamica fuente = new FuenteDinamica("Fuente de prueba para persistencia");
    PuntoGeografico ubicacion = new PuntoGeografico(-34.68415120338135, -58.58712709338028);

    Hecho hecho = new HechoBuilder()
        .conTitulo("Robo")
        .conDescripcion("Robo a mano armada")
        .conUbicacion(ubicacion)
        .conFechaSuceso(LocalDateTime.now()
                                     .minusDays(5))
        .conFuenteOrigen(Origen.PROVISTO_CONTRIBUYENTE)
        .build();
    fuente.agregarHecho(hecho);
    fuenteRepo.save(fuente); // Usamos el método 'persist' heredado de la librería
    var id = fuente.getId();
    // Verificación (opcional, fuera de la transacción)
    FuenteDinamica fuenteRecuperada = find(FuenteDinamica.class, id); // Asumiendo que el ID es 1
    assertNotNull(fuenteRecuperada);
    Assertions.assertFalse(fuenteRecuperada.getHechos()
                                           .isEmpty());
    Assertions.assertEquals(
        "Robo", fuenteRecuperada.getHechos()
                                .get(0)
                                .getTitulo()
    );
  }

  @Test
  @DisplayName("Puedo persistir y recuperar una Coleccion")
  public void coleccionRepositoryTest() {
    FuenteDinamica fuente = new FuenteDinamica("Fuente para colección");
    fuenteRepo.save(fuente);
    Coleccion coleccionBonaerense = new Coleccion(
        "Robos en BA",
        fuente,
        "Hechos delictivos en Buenos Aires",
        "Robos"
    );
    repoColeccion.save(coleccionBonaerense);
    var id = coleccionBonaerense.getId();
    Coleccion coleccionRecuperada = find(Coleccion.class, id);
    assertNotNull(coleccionRecuperada);
    Assertions.assertEquals("Robos en BA", coleccionRecuperada.getTitulo());
  }

  @Test
  @DisplayName("Genero estadisticas y las puedo visualizar en la db")
  public void estadisticaDBTest() {
    CentralDeEstadisticas calculadora = new CentralDeEstadisticas();
    List<Coleccion> coleccionDB = repoColeccion.findAll();
    //System.out.println(coleccionDB.toString());
    calculadora.setGestor(new GestorDeSolicitudes(new RepositorioDeSolicitudes()));
    var stat = calculadora.categoriaConMasHechos(coleccionDB);
    var repoStat = new EstadisticaRepository();
    repoStat.save(stat);
    var coleccion = repoColeccion.findById(1L);
    var stat2 = calculadora.provinciaConMasHechos(coleccion);
    repoStat.save(stat2);
  }
}
