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
import ar.edu.utn.frba.dds.model.reportes.Solicitud;
import ar.edu.utn.frba.dds.model.usuario.Rol;
import ar.edu.utn.frba.dds.model.usuario.Usuario;
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
                        entry -> entry.getKey().name(),
                        Map.Entry::getValue
                    ));
  }

  public void init() {
    withTransaction(() -> {

      LocalDateTime ahora = LocalDateTime.now();
      PuntoGeografico ubicacion = new PuntoGeografico(-34.60, -58.38);

      Hecho h1 = new HechoBuilder()
          .conTitulo("Robo en Almagro")
          .conCategoria("Robo")
          .conProvincia("Buenos Aires")
          .conFechaSuceso(ahora.minusHours(1))
          .conOrigen(Origen.PROVISTO_CONTRIBUYENTE)
          .conUbicacion(ubicacion)
          .build();

      Hecho h2 = new HechoBuilder()
          .conTitulo("Hurto en Avellaneda")
          .conCategoria("Hurto")
          .conProvincia("Buenos Aires")
          .conFechaSuceso(ahora.minusHours(2))
          .conOrigen(Origen.PROVISTO_CONTRIBUYENTE)
          .conUbicacion(ubicacion)
          .build();

      Hecho h3 = new HechoBuilder()
          .conTitulo("Accidente en Córdoba")
          .conCategoria("Accidente")
          .conProvincia("Córdoba")
          .conFechaSuceso(ahora.minusHours(3))
          .conOrigen(Origen.PROVISTO_CONTRIBUYENTE)
          .conUbicacion(ubicacion)
          .build();

      Hecho h4 = new HechoBuilder()
          .conTitulo("Vandalismo en Rosario")
          .conCategoria("Vandalismo")
          .conProvincia("Santa Fe")
          .conFechaSuceso(ahora.minusHours(4))
          .conOrigen(Origen.PROVISTO_CONTRIBUYENTE)
          .conUbicacion(ubicacion)
          .build();

      var fuente = new FuenteDinamica("Fuente para Estadísticas");
      fuente.agregarHecho(h1);
      fuente.agregarHecho(h2);
      fuente.agregarHecho(h3);
      fuente.agregarHecho(h4);

      new FuenteRepository().save(fuente);

      var coleccion = new Coleccion(
          "Colección de Hechos",
          fuente,
          "Colección de prueba",
          "General",
          new Absoluta()
      );

      new ColeccionRepository().save(coleccion);

      UserRepository.instance().guardar(
          new Usuario("admin1@mock.com", "administrador", "admin123", Rol.ADMINISTRADOR)
      );

      EstadisticaRepository repoEst = EstadisticaRepository.instance();

      repoEst.save(new Estadistica("Buenos Aires", 5L, "Robo", null, "HECHOS POR PROVINCIA Y CATEGORIA"));
      repoEst.save(new Estadistica("Buenos Aires", 3L, "Hurto", null, "HECHOS POR PROVINCIA Y CATEGORIA"));
      repoEst.save(new Estadistica("Córdoba", 4L, "Accidente", null, "HECHOS POR PROVINCIA Y CATEGORIA"));
      repoEst.save(new Estadistica("Santa Fe", 2L, "Vandalismo", null, "HECHOS POR PROVINCIA Y CATEGORIA"));

      repoEst.save(new Estadistica("08", 3L, "Robo", null, "HECHOS POR HORA Y CATEGORIA"));
      repoEst.save(new Estadistica("09", 2L, "Hurto", null, "HECHOS POR HORA Y CATEGORIA"));
      repoEst.save(new Estadistica("10", 4L, "Accidente", null, "HECHOS POR HORA Y CATEGORIA"));
      repoEst.save(new Estadistica("11", 1L, "Vandalismo", null, "HECHOS POR HORA Y CATEGORIA"));

      repoEst.save(new Estadistica(null, 5L, "Robo", null, "HECHOS REPORTADOS POR CATEGORIA"));
      repoEst.save(new Estadistica(null, 3L, "Hurto", null, "HECHOS REPORTADOS POR CATEGORIA"));
      repoEst.save(new Estadistica(null, 4L, "Accidente", null, "HECHOS REPORTADOS POR CATEGORIA"));
      repoEst.save(new Estadistica(null, 2L, "Vandalismo", null, "HECHOS REPORTADOS POR CATEGORIA"));

      repoEst.save(new Estadistica("Buenos Aires", 4L, null, coleccion, "HECHOS REPORTADOS POR PROVINCIA Y COLECCION"));
      repoEst.save(new Estadistica("Córdoba", 2L, null, coleccion, "HECHOS REPORTADOS POR PROVINCIA Y COLECCION"));
      repoEst.save(new Estadistica("Santa Fe", 1L, null, coleccion, "HECHOS REPORTADOS POR PROVINCIA Y COLECCION"));

      repoEst.save(new Estadistica(null, 110L, null, null, "CANTIDAD DE HECHOS"));

      repoEst.save(new Estadistica(null, 60L, null, null, "CANTIDAD DE SOLICITUDES PENDIENTES"));

      repoEst.save(new Estadistica(null, 30L, null, null, "CANTIDAD DE SPAM"));

      String motivo = "x".repeat(600);
      SolicitudesRepository.instance().guardar(new Solicitud(h1, motivo));
      SolicitudesRepository.instance().guardar(new Solicitud(h2, motivo));
    });
  }
}