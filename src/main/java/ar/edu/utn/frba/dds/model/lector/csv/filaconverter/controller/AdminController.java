package ar.edu.utn.frba.dds.model.lector.csv.filaconverter.controller;

import ar.edu.utn.frba.dds.controller.ColeccionController;
import ar.edu.utn.frba.dds.controller.FuenteController;
import ar.edu.utn.frba.dds.controller.HechoController;
import ar.edu.utn.frba.dds.controller.SolicitudController;
import ar.edu.utn.frba.dds.model.coleccion.Coleccion;
import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.Absoluta;
import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.AlgoritmoDeConsenso;
import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.MayoriaSimple;
import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.MultiplesMenciones;
import ar.edu.utn.frba.dds.model.fuentes.Fuente;
import ar.edu.utn.frba.dds.model.fuentes.FuenteDeAgregacion;
import ar.edu.utn.frba.dds.model.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.model.fuentes.FuenteEstatica;
import ar.edu.utn.frba.dds.model.fuentes.FuenteExternaAPI;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.model.hecho.Origen;
import ar.edu.utn.frba.dds.model.info.PuntoGeografico;
import ar.edu.utn.frba.dds.repositories.ColeccionRepository;
import ar.edu.utn.frba.dds.repositories.FuenteRepository;
import io.javalin.http.Context;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminController {
  ar.edu.utn.frba.dds.controller.ColeccionController coleccionController = new ColeccionController();
  ar.edu.utn.frba.dds.controller.FuenteController fuenteController = new FuenteController();
  ar.edu.utn.frba.dds.controller.HechoController hechoController = new HechoController();
  ar.edu.utn.frba.dds.controller.SolicitudController solicitudController = new SolicitudController();

  public void mostrarDashboard(Context ctx, Map<String, Object> model) {
    Long totalHechos = hechoController.countAll();
    Long totalColecciones = coleccionController.countAll();
    Long totalReportes = solicitudController.countAll();

    model.put("cantHechos", totalHechos);
    model.put("cantColecciones", totalColecciones);
    model.put("cantReportes", totalReportes);
    ctx.render("admin/dashboard.hbs", model);
  }

  // --- COLECCIONES ---

  public void listarColecciones(Context ctx, Map<String, Object> model) {
    List<Coleccion> colecciones = coleccionController.findAll();
    List<Fuente> fuentes = fuenteController.findAll();
    model.put("colecciones", colecciones);
    model.put("fuentes", fuentes);
    ctx.render("/admin/colecciones-lista.hbs", model);
  }

  public void crearColeccion(Context ctx) {
    coleccionController.crearColeccion(ctx);
  }

  public void editarColeccion(Context ctx, Map<String, Object> model) {
    Long id = Long.parseLong(ctx.pathParam("id"));
    Coleccion coleccion = coleccionController.findById(id);
    List<Fuente> fuentes = fuenteController.findAll();
    model.put("coleccion", coleccion);
    model.put("fuentesDisponibles", fuentes);
    ctx.render("/admin/coleccion-detalle.hbs", model);
  }

  public void configurarColeccion(Context ctx) {
    Long id = Long.parseLong(ctx.pathParam("id"));
    Coleccion col = coleccionController.findById(id);

    col.setTitulo(ctx.formParam("titulo"));
    col.setDescripcion(ctx.formParam("descripcion"));
    col.setCategoria(ctx.formParam("categoria"));

    String fuenteIdStr = ctx.formParam("fuente_id");
    if (fuenteIdStr != null) {
      Long fuenteId = Long.parseLong(fuenteIdStr);
      Fuente nuevaFuente = fuenteController.findById(fuenteId);
      col.setFuente(nuevaFuente);
    }

    String algoTipo = ctx.formParam("algoritmo_tipo");
    if (algoTipo != null) {
      AlgoritmoDeConsenso nuevoAlgo = null;
      switch (algoTipo) {
        case "Absoluta":
          nuevoAlgo = new Absoluta();
          break;
        case "May_simple":
          nuevoAlgo = new MayoriaSimple();
          break;
        case "Mult_menciones":
          nuevoAlgo = new MultiplesMenciones();
          break;
      }
      col.setAlgoritmoDeConsenso(nuevoAlgo);
    }
    col.recalcularConsenso();

    coleccionController.persist(col);
    ctx.redirect("/admin/colecciones/" + id);
  }

  public void agregarHechoAColeccion(Context ctx) {
    Long idColeccion = Long.parseLong(ctx.pathParam("id"));
    Long idHecho = Long.parseLong(ctx.pathParam("hecho_id"));
    Coleccion col = coleccionController.findById(idColeccion);
    Hecho hecho = hechoController.findById(idHecho);
    if (col != null && hecho != null) {
      col.getHechosConsensuados().add(hecho);
      coleccionController.persist(col);
    }
    ctx.redirect("/admin/colecciones/" + idColeccion);
  }

  public void removerHechoDeColeccion(Context ctx) {
    Long idColeccion = Long.parseLong(ctx.pathParam("id"));
    Long idHecho = Long.parseLong(ctx.pathParam("hecho_id"));
    Coleccion col = coleccionController.findById(idColeccion);
    col.getHechosConsensuados().removeIf(hecho -> hecho.getId().equals(idHecho));
    coleccionController.persist(col);
    ctx.redirect("/admin/colecciones" + idColeccion);
  }

  // --- FUENTES ---

  public void listarFuentes(Context ctx, Map<String, Object> model) {
    List<Fuente> fuentes = fuenteController.findAll();
    model.put("fuentes", fuentes);
    ctx.render("/admin/fuentes-lista.hbs", model);
  }

  public void crearFuente(Context ctx) {
    fuenteController.crearFuente(ctx);
    ctx.redirect("/admin/fuentes");
  }

  public void editarFuente(Context ctx, Map<String, Object> model) {
    Long id = Long.parseLong(ctx.pathParam("id"));
    Fuente fuente = fuenteController.findById(id);

    if (fuente == null) {
      ctx.status(404).result("Fuente no encontrada");
      return;
    }

    // Identificar tipo
    String tipo = fuenteController.determinarTipo(fuente);
    model.put("tipoFuente", tipo);
    model.put("esDinamica", fuente instanceof FuenteDinamica);
    model.put("esApi", fuente instanceof FuenteExternaAPI);
    model.put("esEstatica", fuente instanceof FuenteEstatica);
    model.put("esAgregacion", fuente instanceof FuenteDeAgregacion);

    // Configuración para Agregación: Listar fuentes disponibles para agregar
    if (fuente instanceof FuenteDeAgregacion) {
      // Solo permitimos agregar fuentes SIMPLES (no agregación)
      List<Fuente> fuentesDisponibles = fuenteController.findAll(true);

      // Excluir las que ya están agregadas Y la misma fuente
      FuenteDeAgregacion fda = (FuenteDeAgregacion) fuente;
      List<Long> idsYaAgregados = fda.getFuentesCargadas().stream()
                                     .map(Fuente::getId)
                                     .collect(Collectors.toList());
      idsYaAgregados.add(id); // Excluir la misma fuente

      List<Fuente> filtradas = fuentesDisponibles.stream()
                                                 .filter(f -> !idsYaAgregados.contains(f.getId()))
                                                 .collect(Collectors.toList());

      model.put("fuentesDisponiblesAgregacion", filtradas);
    }

    // Buscar Dependencias Directas
    List<Coleccion> coleccionesDirectas = ColeccionRepository.instance().findByFuenteId(id);
    List<FuenteDeAgregacion> agregacionesPadre = FuenteRepository.instance().findAgregacionesByHija(id);

    model.put("coleccionesDirectas", coleccionesDirectas);
    model.put("agregacionesPadre", agregacionesPadre);

    // Colecciones Indirectas (a través de agregaciones padre)
    List<Coleccion> coleccionesIndirectas = FuenteRepository.instance().findColeccionesIndirectas(id);
    model.put("coleccionesIndirectas", coleccionesIndirectas);

    model.put("fuente", fuente);
    ctx.render("/admin/fuente-detalle.hbs", model);
  }

  public void configurarFuente(Context ctx) {
    Long id = Long.parseLong(ctx.pathParam("id"));
    Fuente fte = fuenteController.findById(id);

    if (fte == null) {
      ctx.status(404).result("Fuente no encontrada");
      return;
    }

    // Solo permitimos editar el nombre
    String nuevoNombre = ctx.formParam("nombre");
    if (nuevoNombre != null && !nuevoNombre.isBlank()) {
      fte.setNombre(nuevoNombre);
      fuenteController.save(fte);
    }

    ctx.redirect("/admin/fuentes/" + id);
  }

  public void borrarFuente(Context ctx) {
    Long id = Long.parseLong(ctx.pathParam("id"));
    Fuente fuente = fuenteController.findById(id);

    if (fuente != null) {
      try {
        // El repositorio maneja la lógica recursiva completa
        FuenteRepository.instance().delete(fuente);
        ctx.redirect("/admin/fuentes");
      } catch (Exception e) {
        e.printStackTrace();
        ctx.status(500).result("Error al eliminar fuente: " + e.getMessage());
      }
    } else {
      ctx.status(404).result("Fuente no encontrada");
    }
  }

  // --- GESTIÓN DE FUENTES DE AGREGACIÓN ---

  public void agregarFuenteAAgregacion(Context ctx) {
    Long idPadre = Long.parseLong(ctx.pathParam("id"));
    String idHijaStr = ctx.formParam("fuente_hija_id");

    if (idHijaStr == null || idHijaStr.isBlank()) {
      ctx.redirect("/admin/fuentes/" + idPadre);
      return;
    }

    try {
      Long idHija = Long.parseLong(idHijaStr);
      Fuente padre = fuenteController.findById(idPadre);

      if (padre instanceof FuenteDeAgregacion) {
        fuenteController.agregarFuenteAAgregacion(padre, idHija);
        ctx.redirect("/admin/fuentes/" + idPadre);
      } else {
        ctx.status(400).result("La fuente no es de tipo agregación");
      }
    } catch (Exception e) {
      e.printStackTrace();
      ctx.status(500).result("Error: " + e.getMessage());
    }
  }

  public void quitarFuenteDeAgregacion(Context ctx) {
    Long idPadre = Long.parseLong(ctx.pathParam("id"));
    Long idHija = Long.parseLong(ctx.pathParam("idHija"));

    try {
      Fuente padre = fuenteController.findById(idPadre);

      if (padre instanceof FuenteDeAgregacion) {
        fuenteController.removerFuenteDeAgregacion(padre, idHija);
        ctx.redirect("/admin/fuentes/" + idPadre);
      } else {
        ctx.status(400).result("La fuente no es de tipo agregación");
      }
    } catch (Exception e) {
      e.printStackTrace();
      ctx.status(500).result("Error: " + e.getMessage());
    }
  }

  // --- GESTIÓN DE HECHOS (DINÁMICAS) ---

  // MÉTODO ELIMINADO - ahora se usa el formulario completo en /hechos/nuevo

  public void borrarHechoDeFuente(Context ctx) {
    Long idFuente = Long.parseLong(ctx.pathParam("id"));
    Long idHecho = Long.parseLong(ctx.pathParam("idHecho"));
    Fuente fuente = fuenteController.findById(idFuente);

    fuenteController.borrarHechoDinamico(fuente, idHecho);
    ctx.redirect("/admin/fuentes/" + idFuente);
  }
}