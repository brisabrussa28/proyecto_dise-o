package ar.edu.utn.frba.dds.controller;

import ar.edu.utn.frba.dds.model.coleccion.Coleccion;
import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.Absoluta;
import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.AlgoritmoDeConsenso;
import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.MayoriaSimple;
import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.MultiplesMenciones;
import ar.edu.utn.frba.dds.model.fuentes.Fuente;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.repositories.ColeccionRepository;
import ar.edu.utn.frba.dds.repositories.HechoRepository;
import io.javalin.http.Context;
import java.util.List;

public class ColeccionController {
  FuenteController fuenteController = new FuenteController();

  public void crearColeccion(Context ctx) {
    try {
      String nombre = ctx.formParam("nombre");
      String descripcion = ctx.formParam("descripcion");
      String categoria = ctx.formParam("categoria");

      // Algoritmo de consenso
      String algoritmoStr = ctx.formParam("algoritmo"); // Cambiado de algoritmo_tipo a algoritmo
      AlgoritmoDeConsenso algoritmo = determinarAlgoritmo(algoritmoStr);

      // Fuente de datos
      String modo_fuente = ctx.formParam("modo_fuente");
      Fuente fuente = null;

      System.out.println("Modo fuente recibido: " + modo_fuente);

      if ("nueva".equals(modo_fuente)) {
        System.out.println("Creando NUEVA fuente...");
        fuente = fuenteController.crearFuente(ctx);
        if (fuente == null) {
          throw new RuntimeException("No se pudo crear la fuente");
        }
        // NO llamar a fuenteController.save() aquí porque crearFuente() ya lo hace
        System.out.println("Fuente creada con ID: " + fuente.getId());
      } else {
        System.out.println("Usando fuente EXISTENTE...");
        String fuenteIdStr = ctx.formParam("fuente_id");
        if (fuenteIdStr != null && !fuenteIdStr.isEmpty()) {
          Long fuenteId = Long.parseLong(fuenteIdStr);
          fuente = fuenteController.findById(fuenteId);
          System.out.println("Fuente recuperada con ID: " + fuenteId);
        } else {
          throw new RuntimeException("No se proporcionó ID de fuente existente");
        }
      }

      if (fuente == null) {
        throw new RuntimeException("No se pudo obtener o crear la fuente para la colección");
      }

      // Crear colección
      Coleccion col = new Coleccion(nombre, fuente, descripcion, categoria, algoritmo);
      col.recalcularConsenso();
      ColeccionRepository.instance().save(col);

      System.out.println("Colección creada exitosamente con ID: " + col.getId());
      ctx.redirect("/admin/colecciones");
    } catch (Exception e) {
      e.printStackTrace();
      ctx.status(400).result("Error al crear colección: " + e.getMessage());
    }
  }

  private AlgoritmoDeConsenso determinarAlgoritmo(String algoritmoStr) {
    if (algoritmoStr == null) {
      return new MayoriaSimple();
    }

    return switch (algoritmoStr) {
      case "MAYORIA_SIMPLE" -> new MayoriaSimple();
      case "ABSOLUTA" -> new Absoluta();
      case "MULTIPLES_MENCIONES" -> new MultiplesMenciones();
      default -> new MayoriaSimple();
    };
  }

  // --- NUEVO MÉTODO PARA CALCULO MANUAL ---
  public void calcularConsenso(Context ctx) {
    try {
      Long id = Long.parseLong(ctx.pathParam("id"));
      Coleccion coleccion = this.findById(id);

      if (coleccion != null) {
        coleccion.recalcularConsenso();
        this.persist(coleccion);
        ctx.redirect("/admin/colecciones/" + id);
      } else {
        ctx.status(404).result("Colección no encontrada");
      }
    } catch (Exception e) {
      e.printStackTrace();
      ctx.status(500).result("Error al calcular consenso: " + e.getMessage());
    }
  }

  public void persist(Coleccion colecccion) {
    ColeccionRepository.instance().save(colecccion);
  }

  public List<String> getCategorias() {
    return ColeccionRepository.instance().getCategorias();
  }

  public List<Coleccion> findAll() {
    return ColeccionRepository.instance().findAll();
  }

  public Coleccion findById(Long id) {
    return ColeccionRepository.instance().findById(id);
  }

  public List<Coleccion> buscarRapido(String titulo, String categoria) {
    return ColeccionRepository.instance().buscarRapido(titulo, categoria);
  }

  public Long countAll() {
    return ColeccionRepository.instance().countAll();
  }

  public record ColeccionDTO(Long id, String titulo) {}

  public List<ColeccionDTO> getColeccionesDTO() {
    return ColeccionRepository.instance()
                              .findAll().stream()
                              .map(c -> new ColeccionDTO(c.getId(), c.getTitulo()))
                              .toList();
  }
}