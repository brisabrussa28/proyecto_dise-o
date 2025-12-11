package db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.model.coleccion.Coleccion;
import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.Absoluta;
import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.MultiplesMenciones;
import ar.edu.utn.frba.dds.model.estadisticas.CentralDeEstadisticas;
import ar.edu.utn.frba.dds.model.estadisticas.Estadistica;
import ar.edu.utn.frba.dds.model.exportador.Exportador;
import ar.edu.utn.frba.dds.model.exportador.csv.ExportadorCSV;
import ar.edu.utn.frba.dds.model.exportador.csv.modoexportacion.ModoSobrescribir;
import ar.edu.utn.frba.dds.model.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.model.hecho.HechoBuilder;
import ar.edu.utn.frba.dds.model.hecho.Origen;
import ar.edu.utn.frba.dds.model.info.PuntoGeografico;
import ar.edu.utn.frba.dds.model.reportes.GestorDeSolicitudes;
import ar.edu.utn.frba.dds.model.reportes.detectorspam.DetectorSpam;
import ar.edu.utn.frba.dds.repositories.AlgoritmoRepository;
import ar.edu.utn.frba.dds.repositories.ColeccionRepository;
import ar.edu.utn.frba.dds.repositories.EstadisticaRepository;
import ar.edu.utn.frba.dds.repositories.FuenteRepository;
import ar.edu.utn.frba.dds.repositories.HechoRepository;
import ar.edu.utn.frba.dds.repositories.SolicitudesRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests que verifican la correcta persistencia de diferentes entidades del dominio.
 * Hereda de PersistenceTests para obtener el manejo de transacciones y la configuración de la BD.
 */
public class JDBCTest extends PersistenceTests { // Aseguramos que herede de PersistenceTests
  private ColeccionRepository repoColeccion = new ColeccionRepository();
  private HechoRepository hechoRepo = new HechoRepository();
  private FuenteRepository fuenteRepo = new FuenteRepository();
  private AlgoritmoRepository algoritmoRepository = new AlgoritmoRepository();
  private Coleccion coleccionDePrueba;
  private DetectorSpam detectorSpam;
  private SolicitudesRepository solicitudRepo = new SolicitudesRepository();

  LocalDateTime hora = LocalDateTime.now();
  PuntoGeografico ubicacion = new PuntoGeografico(-34.68415120338135, -58.58712709338028);

  Hecho hecho1 = new HechoBuilder()
      .conTitulo("Robo en Almagro")
      .conCategoria("Robos")
      .conFechaSuceso(hora.minusHours(1))
      .conOrigen(Origen.PROVISTO_CONTRIBUYENTE)
      .conUbicacion(ubicacion)
      .build();

  Hecho hecho2 = new HechoBuilder()
      .conTitulo("Robo en Caballito")
      .conCategoria("Robos")
      .conFechaSuceso(hora.minusHours(1))
      .conOrigen(Origen.PROVISTO_CONTRIBUYENTE)
      .conUbicacion(ubicacion)
      .build();

  Hecho hecho3 = new HechoBuilder()
      .conTitulo("Hurto en Avellaneda")
      .conCategoria("Hurtos")
      .conFechaSuceso(hora.minusHours(2))
      .conOrigen(Origen.PROVISTO_CONTRIBUYENTE)
      .conUbicacion(ubicacion)
      .build();

  @BeforeEach
  public void setUp() {
    var fuente = new FuenteDinamica("Fuente para Estadísticas");

    detectorSpam = mock(DetectorSpam.class);
    when(detectorSpam.esSpam(anyString())).thenReturn(false);


    fuente.agregarHecho(hecho1);
    fuente.agregarHecho(hecho2);
    fuente.agregarHecho(hecho3);
    FuenteRepository repoFuentes = new FuenteRepository();
    repoFuentes.save(fuente);

    var solicitudes = new SolicitudesRepository();
    var calculadora = new CentralDeEstadisticas();

    Exportador<Estadistica> exportadorCsv = new ExportadorCSV<>(new ModoSobrescribir());
    calculadora.setExportador(exportadorCsv);
    var algoritmo = new Absoluta();

    var coleccion = new Coleccion(
        "Coleccion de Hechos",
        fuente,
        "Descripcion de prueba",
        "General",
        algoritmo
    );

    coleccion.setAlgoritmoDeConsenso(algoritmo);
    // FIX: Eliminada la llamada explícita a algoritmoRepository.save(algoritmo)
    // para evitar 'PersistentObjectException: detached entity passed to persist'
    // Hibernate manejará la persistencia a través de la Coleccion.
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
    var fuenteRecuperada = fuenteRepo.findById(id);
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
        "Robos",
        new Absoluta()
    );
    var multiplesMenciones = new MultiplesMenciones();
    coleccionBonaerense.setAlgoritmoDeConsenso(multiplesMenciones);
    // Aquí sí lo guardamos porque no es parte del setUp general y es una instancia nueva
    algoritmoRepository.save(multiplesMenciones);
    repoColeccion.save(coleccionBonaerense);
    var id = coleccionBonaerense.getId();
    var coleccionRecuperada = repoColeccion.findById(id);
    assertNotNull(coleccionRecuperada);
    Assertions.assertEquals("Robos en BA", coleccionRecuperada.getTitulo());
  }

  @Test
  @DisplayName("Genero estadisticas y las puedo visualizar en la db")
  public void estadisticaDBTest() {
    CentralDeEstadisticas calculadora = new CentralDeEstadisticas();
    List<Coleccion> coleccionDB = ColeccionRepository.instance()
                                                     .findAll();
    //System.out.println(coleccionDB.toString());
    calculadora.setGestor(new GestorDeSolicitudes(solicitudRepo));
    var stat = calculadora.categoriaConMasHechos();
    EstadisticaRepository.instance().save(stat);
    Optional<Coleccion> coleccionOpt = Optional.ofNullable(repoColeccion.findById(1L));
    Assertions.assertTrue(
        coleccionOpt.isPresent(),
        "La colección de prueba no fue encontrada en la BD."
    );
    coleccionOpt.ifPresent(coleccion -> {
      var stat2 = calculadora.provinciaConMasHechos(coleccion);
      EstadisticaRepository.instance().save(stat2);
    });
    var stat3 = calculadora.horaConMasHechosDeCiertaCategoria("Robos");
    EstadisticaRepository.instance().save(stat3);
  }

  @Test
  public void creoUnaSolicitudYLaPuedoVisualizar() {
    GestorDeSolicitudes gestor = new GestorDeSolicitudes(solicitudRepo);
    var antes = gestor.getSolicitudesPendientes().size();
    gestor.crearSolicitud(hecho1, "mucho sexo gay".repeat(36), detectorSpam);
    assertEquals(antes + 1, gestor.getSolicitudesPendientes().size());
  }
}