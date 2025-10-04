package db;

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

public class JDBCTest {
  private ColeccionRepository repo = new ColeccionRepository();

  @BeforeEach
  public void setUp() {
    final DetectorSpam detector = texto -> texto.contains("Troll");
    var fuente = new FuenteDinamica("Fuente para Estadísticas");

    var ubicacion = new PuntoGeografico(235,-5123);
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
        .conProvincia("PBA")
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
  public void puedoPersistirUnHechoEnFuenteDinamica() {
    FuentesRepository repo = new FuentesRepository();
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
    repo.save(fuente);
  }

  @Test
  @DisplayName("Creo un repositorio para colecciones y puedo guardar y acceder a los mismos")
  public void coleccionRepositoryTest() {
    var fuente = new FuenteDinamica("Fuente de prueba");
    var repoFuente = new FuentesRepository();
    repoFuente.save(fuente);
    var repo = new ColeccionRepository();
    Coleccion bonaerense = new Coleccion(
        "Robos",
        fuente,
        "Un día más siendo del conurbano",
        "Robos"
    );
    repo.save(bonaerense);
  }

  @Test
  @DisplayName("Genero estadisticas y las puedo visualizar en la db")
  public void estadisticaDBTest() {
    var calculadora = new CentralDeEstadisticas();
    calculadora.setFiltro(new Filtro(new CondicionTrue()));
    List<Coleccion> coleccionDB = repo.findAll();
//    System.out.println(coleccionDB.toString());
    var stat = calculadora.categoriaConMasHechos(coleccionDB);
    var repoStat = new EstadisticaRepository();
    repoStat.save(stat);
  }
}
