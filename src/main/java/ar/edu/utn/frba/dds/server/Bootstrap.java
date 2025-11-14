package ar.edu.utn.frba.dds.server;

import ar.edu.utn.frba.dds.model.coleccion.Coleccion;
import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.Absoluta;
import ar.edu.utn.frba.dds.model.estadisticas.CentralDeEstadisticas;
import ar.edu.utn.frba.dds.model.estadisticas.Estadistica;
import ar.edu.utn.frba.dds.model.exportador.Exportador;
import ar.edu.utn.frba.dds.model.exportador.csv.ExportadorCSV;
import ar.edu.utn.frba.dds.model.exportador.csv.modoexportacion.ModoSobrescribir;
import ar.edu.utn.frba.dds.model.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.model.fuentes.FuenteEstatica;
import ar.edu.utn.frba.dds.model.hecho.CampoHecho;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.model.hecho.HechoBuilder;
import ar.edu.utn.frba.dds.model.hecho.Origen;
import ar.edu.utn.frba.dds.model.info.PuntoGeografico;
import ar.edu.utn.frba.dds.model.lector.configuracion.ConfiguracionLectorCsv;
import ar.edu.utn.frba.dds.model.usuario.Usuario;
import ar.edu.utn.frba.dds.repositories.AlgoritmoRepository;
import ar.edu.utn.frba.dds.repositories.ColeccionRepository;
import ar.edu.utn.frba.dds.repositories.EstadisticaRepository;
import ar.edu.utn.frba.dds.repositories.FuenteRepository;
import ar.edu.utn.frba.dds.repositories.SolicitudesRepository;
import ar.edu.utn.frba.dds.repositories.UserRepository;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Bootstrap implements WithSimplePersistenceUnit {
  private Map<String, List<String>> convertirMapeoAString(Map<CampoHecho, List<String>> mapeoEnum) {
    return mapeoEnum.entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                        entry -> entry.getKey()
                                      .name(), Map.Entry::getValue
                    ));
  }

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
      Map<String, List<String>> mapeoColumnas = convertirMapeoAString(Map.of(
          CampoHecho.TITULO, List.of("titulo"),
          CampoHecho.DESCRIPCION, List.of("descripcion"),
          CampoHecho.LATITUD, List.of("latitud"),
          CampoHecho.LONGITUD, List.of("longitud"),
          CampoHecho.FECHA_SUCESO, List.of("fechaSuceso"),
          CampoHecho.CATEGORIA, List.of("categoria"),
          CampoHecho.DIRECCION, List.of("direccion"),
          CampoHecho.PROVINCIA, List.of("provincia")
      ));
      // HechoFilaConverter converter = new HechoFilaConverter("dd/MM/yyyy", mapeoColumnas);
      var fuenteCsv = new FuenteEstatica(
          "Una fuente estatica",
          "src/main/resources/csvs/ejemplo.csv",
          new ConfiguracionLectorCsv(',', "dd/MM/yyyy", mapeoColumnas)
      );
      fuente.agregarHecho(hecho1);
      fuente.agregarHecho(hecho2);
      fuente.agregarHecho(hecho3);
      FuenteRepository repoFuentes = new FuenteRepository();
      repoFuentes.save(fuente);
      repoFuentes.save(fuenteCsv);
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
      AlgoritmoRepository algoritmoRepository = new AlgoritmoRepository();
      algoritmoRepository.save(algoritmo);
      ColeccionRepository repoColeccion = new ColeccionRepository();
      repoColeccion.save(coleccion);
      //UserRepository.instance().guardar(new Usuario("admin1@mock.com", "admin123", "Administrador"));
      EstadisticaRepository repoEstadisticas = EstadisticaRepository.instance();
      repoEstadisticas.save(new Estadistica("Robos", 64L, "CATEGORIA CON MAS HECHOS", "Robos"));
      repoEstadisticas.save(new Estadistica("Obras", 28L, "CATEGORIA CON MAS HECHOS", "Obras"));
      repoEstadisticas.save(new Estadistica("Buenos Aires", 43L, "PROVINCIA CON MAS HECHOS", null));
      repoEstadisticas.save(new Estadistica("15", 18L, "HORA CON MAS HECHOS DE UNA CATEGORIA", "Robos"));
      repoEstadisticas.save(new Estadistica("Spam", 37L, "CANTIDAD DE SOLICITUDES SPAM", null));
    });

  }
}
