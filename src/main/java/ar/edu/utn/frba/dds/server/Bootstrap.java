package ar.edu.utn.frba.dds.server;

import ar.edu.utn.frba.dds.model.coleccion.Coleccion;
import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.Absoluta;
import ar.edu.utn.frba.dds.model.estadisticas.Estadistica;
import ar.edu.utn.frba.dds.model.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.model.hecho.CampoHecho;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.model.hecho.HechoBuilder;
import ar.edu.utn.frba.dds.model.hecho.Origen;
import ar.edu.utn.frba.dds.model.info.PuntoGeografico;
import ar.edu.utn.frba.dds.model.reportes.Solicitud;
import ar.edu.utn.frba.dds.model.usuario.Rol;
import ar.edu.utn.frba.dds.model.usuario.Usuario;
import ar.edu.utn.frba.dds.repositories.ColeccionRepository;
import ar.edu.utn.frba.dds.repositories.EstadisticaRepository;
import ar.edu.utn.frba.dds.repositories.FuenteRepository;
import ar.edu.utn.frba.dds.repositories.SolicitudesRepository;
import ar.edu.utn.frba.dds.repositories.TFIDFRepository;
import ar.edu.utn.frba.dds.repositories.UserRepository;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;
import java.time.LocalDateTime;
import java.util.HashMap;
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
      System.out.println("=== INICIALIZANDO SISTEMA ===");

      // Verificar si ya hay datos inicializados
      if (yaInicializado()) {
        System.out.println("El sistema ya fue inicializado anteriormente.");
        return;
      }

      LocalDateTime ahora = LocalDateTime.now();
      PuntoGeografico ubicacion = new PuntoGeografico(-34.60, -58.38);

      System.out.println("Creando datos de prueba...");

      // Crear hechos
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

      // Crear fuente y colección
      var fuente = new FuenteDinamica("Fuente para Estadísticas");
      fuente.agregarHecho(h1);
      fuente.agregarHecho(h2);
      fuente.agregarHecho(h3);
      fuente.agregarHecho(h4);

      new FuenteRepository().save(fuente);
      System.out.println("Fuente creada: " + fuente.getNombre());

      var coleccion = new Coleccion(
          "Colección de Hechos",
          fuente,
          "Colección de prueba",
          "General",
          new Absoluta()
      );

      new ColeccionRepository().save(coleccion);
      System.out.println("Colección creada: " + coleccion.getTitulo());

      // Crear usuario admin
      crearUsuarioAdmin();

      // Crear estadísticas de prueba
      crearEstadisticasDePrueba(coleccion);

      // Crear solicitudes de prueba
      crearSolicitudesDePrueba(h1, h2);

      // ===== INICIALIZACIÓN TF-IDF =====
      inicializarEjemplosTFIDF();

      System.out.println("=== INICIALIZACIÓN COMPLETADA ===");
    });
  }

  /**
   * Verifica si el sistema ya fue inicializado
   */
  private boolean yaInicializado() {
    try {
      // Verificar si ya hay vectores TF-IDF
      TFIDFRepository tfidfRepo = TFIDFRepository.instance();
      long conteoVectores = tfidfRepo.contarVectoresSpam();

      System.out.println("Verificando estado de inicialización:");
      System.out.println("  - Vectores TF-IDF: " + conteoVectores);

      // Si ya hay vectores, asumimos que ya fue inicializado
      return conteoVectores > 0;

    } catch (Exception e) {
      System.err.println("Error al verificar inicialización: " + e.getMessage());
      return false; // Si hay error, proceder con inicialización
    }
  }

  /**
   * Crea el usuario administrador
   */
  private void crearUsuarioAdmin() {
    try {
      UserRepository userRepo = UserRepository.instance();
      if (userRepo.buscarPorEmail("admin1@mock.com") == null) {
        Usuario admin = new Usuario(
            "admin1@mock.com",
            "administrador",
            null,
            "admin123",
            Rol.ADMINISTRADOR
        );
        userRepo.guardar(admin);
        System.out.println("Usuario admin creado: admin1@mock.com");
      } else {
        System.out.println("Usuario admin ya existe: admin1@mock.com");
      }
    } catch (Exception e) {
      System.err.println("Error al crear usuario admin: " + e.getMessage());
    }
  }

  /**
   * Crea estadísticas de prueba
   */
  private void crearEstadisticasDePrueba(Coleccion coleccion) {
    try {
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

      System.out.println("Estadísticas de prueba creadas");
    } catch (Exception e) {
      System.err.println("Error al crear estadísticas de prueba: " + e.getMessage());
    }
  }

  /**
   * Crea solicitudes de prueba
   */
  private void crearSolicitudesDePrueba(Hecho h1, Hecho h2) {
    try {
      String motivo = "x".repeat(600);
      SolicitudesRepository.instance().guardar(new Solicitud(h1, motivo));
      SolicitudesRepository.instance().guardar(new Solicitud(h2, motivo));
      System.out.println("Solicitudes de prueba creadas");
    } catch (Exception e) {
      System.err.println("Error al crear solicitudes de prueba: " + e.getMessage());
    }
  }

  /**
   * Inicializa la base de datos con ejemplos básicos de spam para TF-IDF
   */
  private void inicializarEjemplosTFIDF() {
    System.out.println("Inicializando ejemplos TF-IDF...");

    TFIDFRepository tfidfRepo = TFIDFRepository.instance();

    try {
      // Verificar conexión
      Long totalSpam = tfidfRepo.contarVectoresSpam();
      System.out.println("Vectores actuales en DB: " + totalSpam);

      if (totalSpam > 0) {
        System.out.println("TF-IDF ya tiene ejemplos almacenados");
        return;
      }

      // Ejemplos básicos de spam
      String[] ejemplosSpam = {
          "COMPRA YA!!! OFERTA EXCLUSIVA 50% DESCUENTO",
          "GANE DINERO RÁPIDO trabajando desde casa",
          "CLIC AQUÍ para premio sorpresa",
          "URGENTE: necesita ayuda financiera inmediata",
          "Producto milagroso que cura todo",
          "Oportunidad única de inversión garantizada",
          "Gane millones sin esfuerzo ni experiencia",
          "Descuento exclusivo solo por tiempo limitado",
          "Ingrese a este link para reclamar su premio",
          "Dinero fácil y rápido desde su hogar"
      };

      int creados = 0;
      for (String ejemplo : ejemplosSpam) {
        try {
          // Verificar si ya existe
          if (!tfidfRepo.existeTextoSimilar(ejemplo)) {
            // Crear vector simple
            Map<String, Double> vectorSimple = crearVectorSimple(ejemplo);

            // Usar el repositorio para guardar
            tfidfRepo.guardarVector(ejemplo, vectorSimple, true);
            creados++;

            System.out.println("  ✓ Vector creado para: " +
                                   ejemplo.substring(0, Math.min(40, ejemplo.length())) + "...");
          } else {
            System.out.println("  ⚠ Ya existe: " +
                                   ejemplo.substring(0, Math.min(40, ejemplo.length())) + "...");
          }
        } catch (Exception e) {
          System.err.println("  ✗ Error al crear vector para: " +
                                 ejemplo.substring(0, Math.min(40, ejemplo.length())) + "...");
          System.err.println("    Error: " + e.getMessage());
        }
      }

      System.out.println("Inicializados " + creados + " ejemplos de spam para TF-IDF");

      // Verificar que se guardaron
      Long totalDespues = tfidfRepo.contarVectoresSpam();
      System.out.println("Total vectores después de inicialización: " + totalDespues);

    } catch (Exception e) {
      System.err.println("ERROR CRÍTICO al inicializar TF-IDF: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * Crea un vector simple para un texto
   */
  private Map<String, Double> crearVectorSimple(String texto) {
    Map<String, Double> vector = new HashMap<>();

    if (texto == null || texto.trim().isEmpty()) {
      vector.put("vacio", 1.0);
      return vector;
    }

    String textoLower = texto.toLowerCase();

    // Palabras clave comunes en spam
    String[] palabrasClave = {
        "compra", "oferta", "exclusiva", "descuento", "dinero",
        "rápido", "trabajar", "casa", "clic", "aquí", "urgente",
        "ayuda", "financiera", "producto", "milagroso", "cura",
        "todo", "oportunidad", "única", "inversión", "garantizada",
        "gane", "millones", "esfuerzo", "experiencia", "exclusivo",
        "tiempo", "limitado", "ingrese", "link", "reclamar",
        "premio", "fácil", "hogar"
    };

    for (String palabra : palabrasClave) {
      if (textoLower.contains(palabra)) {
        vector.put(palabra, 1.0);
      }
    }

    // Si no hay palabras clave, poner frecuencia básica
    if (vector.isEmpty()) {
      // Dividir en palabras y contar frecuencias básicas
      String[] palabras = textoLower.split("\\s+");
      for (String palabra : palabras) {
        if (palabra.length() > 2) {
          vector.put(palabra, vector.getOrDefault(palabra, 0.0) + 1.0);
        }
      }
    }

    return vector;
  }
}