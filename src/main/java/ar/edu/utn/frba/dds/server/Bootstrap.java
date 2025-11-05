package ar.edu.utn.frba.dds.server;

import ar.edu.utn.frba.dds.model.coleccion.Coleccion;
import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.Absoluta;
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
import ar.edu.utn.frba.dds.repositories.AlgoritmoRepository;
import ar.edu.utn.frba.dds.repositories.ColeccionRepository;
import ar.edu.utn.frba.dds.repositories.FuenteRepository;
import ar.edu.utn.frba.dds.repositories.SolicitudesRepository;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;
import java.time.LocalDateTime;

public class Bootstrap implements WithSimplePersistenceUnit {
  public void init() {
    //    HechoRepository hechoRepo = new HechoRepository();
    //    FuenteRepository fuenteRepo = new FuenteRepository();
    //    SolicitudesRepository solicitudRepo = new SolicitudesRepository();

    withTransaction(() -> {
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

      var fuente = new FuenteDinamica("Fuente para Estadísticas");
      fuente.agregarHecho(hecho1);
      fuente.agregarHecho(hecho2);
      fuente.agregarHecho(hecho3);
      FuenteRepository repoFuentes = new FuenteRepository();
      repoFuentes.save(fuente);
      var solicitudes = new SolicitudesRepository();
      var calculadora = new CentralDeEstadisticas();
      Exportador<Estadistica> exportadorCsv = new ExportadorCSV<>(new ModoSobrescribir());
      calculadora.setExportador(exportadorCsv);

      var coleccion = new Coleccion(
          "Coleccion de Hechos",
          fuente,
          "Descripcion de prueba",
          "General"
      );
      var algoritmo = new Absoluta();
      coleccion.setAlgoritmoDeConsenso(algoritmo);
      AlgoritmoRepository algoritmoRepository = new AlgoritmoRepository();
      algoritmoRepository.save(algoritmo);
      ColeccionRepository repoColeccion = new ColeccionRepository();
      repoColeccion.save(coleccion);
    });

  }
}
