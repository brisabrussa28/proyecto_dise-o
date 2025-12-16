package ar.edu.utn.frba.dds.model.lector.csv.filaconverter.server;

import ar.edu.utn.frba.dds.model.coleccion.Coleccion;
import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.Absoluta;
import ar.edu.utn.frba.dds.model.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.model.hecho.HechoBuilder;
import ar.edu.utn.frba.dds.model.hecho.Origen;
import ar.edu.utn.frba.dds.model.info.PuntoGeografico;
import ar.edu.utn.frba.dds.repositories.ColeccionRepository;
import ar.edu.utn.frba.dds.repositories.FuenteRepository;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;
import java.time.LocalDateTime;

public class Bootstrap implements WithSimplePersistenceUnit {

  public void init() {
    withTransaction(() -> {
      System.out.println("=== INICIALIZANDO SISTEMA ===");

      System.out.println("Creando datos de prueba...");
      // Crear hechos
      Hecho h1 = new HechoBuilder()
          .conTitulo("Vandalismo en Rosario")
          .conCategoria("Vandalismo")
          .conProvincia("Santa Fe")
          .conFechaSuceso(LocalDateTime.now().minusHours(4))
          .conOrigen(Origen.PROVISTO_CONTRIBUYENTE)
          .conUbicacion(new PuntoGeografico(-32.95, -60.66))
          .build();

      Hecho h2 = new HechoBuilder()
          .conTitulo("Accidente de tránsito en Córdoba")
          .conCategoria("Accidente")
          .conProvincia("Córdoba")
          .conFechaSuceso(LocalDateTime.now().minusDays(1))
          .conOrigen(Origen.PROVISTO_CONTRIBUYENTE)
          .conUbicacion(new PuntoGeografico(-31.42, -64.18))
          .build();

      Hecho h3 = new HechoBuilder()
          .conTitulo("Incendio en Mendoza")
          .conCategoria("Incendio")
          .conProvincia("Mendoza")
          .conFechaSuceso(LocalDateTime.now().minusHours(12))
          .conOrigen(Origen.PROVISTO_CONTRIBUYENTE)
          .conUbicacion(new PuntoGeografico(-32.89, -68.85))
          .build();

      Hecho h4 = new HechoBuilder()
          .conTitulo("Robo en San Miguel de Tucumán")
          .conCategoria("Robo")
          .conProvincia("Tucumán")
          .conFechaSuceso(LocalDateTime.now().minusDays(2))
          .conOrigen(Origen.PROVISTO_CONTRIBUYENTE)
          .conUbicacion(new PuntoGeografico(-26.82, -65.22))
          .build();

      Hecho h5 = new HechoBuilder()
          .conTitulo("Accidente en Villa María")
          .conCategoria("Accidente")
          .conProvincia("Córdoba")
          .conFechaSuceso(LocalDateTime.now().minusHours(8))
          .conOrigen(Origen.PROVISTO_CONTRIBUYENTE)
          .conUbicacion(new PuntoGeografico(-32.41, -63.25))
          .build();

      Hecho h6 = new HechoBuilder()
          .conTitulo("Incendio forestal en San Rafael")
          .conCategoria("Incendio")
          .conProvincia("Mendoza")
          .conFechaSuceso(LocalDateTime.now().minusDays(3))
          .conOrigen(Origen.PROVISTO_CONTRIBUYENTE)
          .conUbicacion(new PuntoGeografico(-34.61, -68.33))
          .build();

      Hecho h7 = new HechoBuilder()
          .conTitulo("Vandalismo en Santa Fe Capital")
          .conCategoria("Vandalismo")
          .conProvincia("Santa Fe")
          .conFechaSuceso(LocalDateTime.now().minusHours(6))
          .conOrigen(Origen.PROVISTO_CONTRIBUYENTE)
          .conUbicacion(new PuntoGeografico(-31.63, -60.70))
          .build();

      Hecho h8 = new HechoBuilder()
          .conTitulo("Robo en Yerba Buena")
          .conCategoria("Robo")
          .conProvincia("Tucumán")
          .conFechaSuceso(LocalDateTime.now().minusHours(10))
          .conOrigen(Origen.PROVISTO_CONTRIBUYENTE)
          .conUbicacion(new PuntoGeografico(-26.82, -65.33))
          .build();

      Hecho h9 = new HechoBuilder()
          .conTitulo("Accidente en Río Cuarto")
          .conCategoria("Accidente")
          .conProvincia("Córdoba")
          .conFechaSuceso(LocalDateTime.now().minusDays(1))
          .conOrigen(Origen.PROVISTO_CONTRIBUYENTE)
          .conUbicacion(new PuntoGeografico(-33.13, -64.35))
          .build();

      Hecho h10 = new HechoBuilder()
          .conTitulo("Incendio en Godoy Cruz")
          .conCategoria("Incendio")
          .conProvincia("Mendoza")
          .conFechaSuceso(LocalDateTime.now().minusHours(5))
          .conOrigen(Origen.PROVISTO_CONTRIBUYENTE)
          .conUbicacion(new PuntoGeografico(-32.92, -68.83))
          .build();

      Hecho h11 = new HechoBuilder()
          .conTitulo("Vandalismo en Rafaela")
          .conCategoria("Vandalismo")
          .conProvincia("Santa Fe")
          .conFechaSuceso(LocalDateTime.now().minusDays(2))
          .conOrigen(Origen.PROVISTO_CONTRIBUYENTE)
          .conUbicacion(new PuntoGeografico(-31.25, -61.49))
          .build();

      Hecho h12 = new HechoBuilder()
          .conTitulo("Robo en Concepción")
          .conCategoria("Robo")
          .conProvincia("Tucumán")
          .conFechaSuceso(LocalDateTime.now().minusHours(7))
          .conOrigen(Origen.PROVISTO_CONTRIBUYENTE)
          .conUbicacion(new PuntoGeografico(-27.34, -65.59))
          .build();

      Hecho h13 = new HechoBuilder()
          .conTitulo("Accidente en Buenos Aires")
          .conCategoria("Accidente")
          .conProvincia("Buenos Aires")
          .conFechaSuceso(LocalDateTime.now().minusHours(9))
          .conOrigen(Origen.PROVISTO_CONTRIBUYENTE)
          .conUbicacion(new PuntoGeografico(-34.61, -58.38))
          .build();

      Hecho h14 = new HechoBuilder()
          .conTitulo("Incendio en La Plata")
          .conCategoria("Incendio")
          .conProvincia("Buenos Aires")
          .conFechaSuceso(LocalDateTime.now().minusDays(1))
          .conOrigen(Origen.PROVISTO_CONTRIBUYENTE)
          .conUbicacion(new PuntoGeografico(-34.92, -57.95))
          .build();

      Hecho h15 = new HechoBuilder()
          .conTitulo("Robo en Mar del Plata")
          .conCategoria("Robo")
          .conProvincia("Buenos Aires")
          .conFechaSuceso(LocalDateTime.now().minusHours(11))
          .conOrigen(Origen.PROVISTO_CONTRIBUYENTE)
          .conUbicacion(new PuntoGeografico(-38.00, -57.55))
          .build();

      Hecho h16 = new HechoBuilder()
          .conTitulo("Accidente en Posadas")
          .conCategoria("Accidente")
          .conProvincia("Misiones")
          .conFechaSuceso(LocalDateTime.now().minusDays(2))
          .conOrigen(Origen.PROVISTO_CONTRIBUYENTE)
          .conUbicacion(new PuntoGeografico(-27.36, -55.89))
          .build();

      Hecho h17 = new HechoBuilder()
          .conTitulo("Incendio en San Luis")
          .conCategoria("Incendio")
          .conProvincia("San Luis")
          .conFechaSuceso(LocalDateTime.now().minusHours(14))
          .conOrigen(Origen.PROVISTO_CONTRIBUYENTE)
          .conUbicacion(new PuntoGeografico(-33.30, -66.34))
          .build();

      Hecho h18 = new HechoBuilder()
          .conTitulo("Robo en Corrientes")
          .conCategoria("Robo")
          .conProvincia("Corrientes")
          .conFechaSuceso(LocalDateTime.now().minusHours(3))
          .conOrigen(Origen.PROVISTO_CONTRIBUYENTE)
          .conUbicacion(new PuntoGeografico(-27.48, -58.83))
          .build();

      Hecho h19 = new HechoBuilder()
          .conTitulo("Accidente en Neuquén")
          .conCategoria("Accidente")
          .conProvincia("Neuquén")
          .conFechaSuceso(LocalDateTime.now().minusDays(1))
          .conOrigen(Origen.PROVISTO_CONTRIBUYENTE)
          .conUbicacion(new PuntoGeografico(-38.95, -68.06))
          .build();

      Hecho h20 = new HechoBuilder()
          .conTitulo("Incendio en Bariloche")
          .conCategoria("Incendio")
          .conProvincia("Río Negro")
          .conFechaSuceso(LocalDateTime.now().minusHours(15))
          .conOrigen(Origen.PROVISTO_CONTRIBUYENTE)
          .conUbicacion(new PuntoGeografico(-41.13, -71.31))
          .build();

      // Crear fuente y colección
      var fuente = new FuenteDinamica("Fuente para Estadísticas");
      fuente.agregarHecho(h1);
      fuente.agregarHecho(h2);
      fuente.agregarHecho(h3);
      fuente.agregarHecho(h4);
      fuente.agregarHecho(h5);
      fuente.agregarHecho(h6);
      fuente.agregarHecho(h7);
      fuente.agregarHecho(h8);
      fuente.agregarHecho(h9);
      fuente.agregarHecho(h10);
      fuente.agregarHecho(h11);
      fuente.agregarHecho(h12);
      fuente.agregarHecho(h13);
      fuente.agregarHecho(h14);
      fuente.agregarHecho(h15);
      fuente.agregarHecho(h16);
      fuente.agregarHecho(h17);
      fuente.agregarHecho(h18);
      fuente.agregarHecho(h19);
      fuente.agregarHecho(h20);

      new FuenteRepository().save(fuente);
      System.out.println("Fuente creada: " + fuente.getNombre());

      // Colección de Accidentes
      var coleccionAccidentes = new Coleccion(
          "Colección de Accidentes",
          fuente,
          "Hechos relacionados con accidentes de tránsito y urbanos",
          "Accidentes",
          new Absoluta()
      );

    // Colección de Incendios
      var coleccionIncendios = new Coleccion(
          "Colección de Incendios",
          fuente,
          "Hechos relacionados con incendios forestales y urbanos",
          "Incendios",
          new Absoluta()
      );

    // Colección de Robos y Vandalismo
      var coleccionRobosVandalismo = new Coleccion(
          "Colección de Robos y Vandalismo",
          fuente,
          "Hechos relacionados con delitos contra la propiedad",
          "Delitos",
          new Absoluta()
      );

      new ColeccionRepository().save(coleccionAccidentes);
      new ColeccionRepository().save(coleccionIncendios);
      new ColeccionRepository().save(coleccionRobosVandalismo);
      System.out.println("Colecciones creadas");

      System.out.println("=== INICIALIZACIÓN COMPLETADA ===");
    });
  }
}