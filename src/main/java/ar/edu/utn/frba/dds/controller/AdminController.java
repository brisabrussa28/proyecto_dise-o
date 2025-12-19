package ar.edu.utn.frba.dds.controller;

import ar.edu.utn.frba.dds.model.coleccion.Coleccion;
import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.Absoluta;
import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.AlgoritmoDeConsenso;
import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.MayoriaSimple;
import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.MultiplesMenciones;
import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.Verdadero;
import ar.edu.utn.frba.dds.model.fuentes.Fuente;
import ar.edu.utn.frba.dds.model.fuentes.FuenteDeAgregacion;
import ar.edu.utn.frba.dds.model.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.model.fuentes.FuenteEstatica;
import ar.edu.utn.frba.dds.model.fuentes.FuenteExternaAPI;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.repositories.ColeccionRepository;
import ar.edu.utn.frba.dds.repositories.FuenteRepository;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;
import io.javalin.http.Context;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminController implements WithSimplePersistenceUnit {
  ColeccionController coleccionController = new ColeccionController();
  FuenteController fuenteController = new FuenteController();
  HechoController hechoController = new HechoController();
  SolicitudController solicitudController = new SolicitudController(); // Para el dashboard

  public void mostrarDashboard(Context ctx, Map<String, Object> model) {
    Long totalHechos = hechoController.countAll();
    Long totalColecciones = coleccionController.countAll();
    Long totalReportes = solicitudController.countAll();
    Long totalFuentes = fuenteController.countAll();

    model.put("cantHechos", totalHechos);
    model.put("cantColecciones", totalColecciones);
    model.put("cantReportes", totalReportes);
    model.put("cantFuentes", totalFuentes);
    ctx.render("admin/dashboard.hbs", model);
  }

  // --- COLECCIONES ---
  public void listarColecciones(Context ctx, Map<String, Object> model) {
    List<Coleccion> colecciones = coleccionController.findAll();
    List<Fuente> fuentes = fuenteController.findAll();
    model.put("colecciones", colecciones);
    model.put("fuentes", fuentes);
    ctx.render("admin/colecciones-lista.hbs", model);
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
    ctx.render("admin/coleccion-detalle.hbs", model);
  }

  public void configurarColeccion(Context ctx) {
    withTransaction(() -> {
      Long id = Long.parseLong(ctx.pathParam("id"));
      Coleccion col = entityManager().find(Coleccion.class, id);
      col.setTitulo(ctx.formParam("titulo"));
      col.setDescripcion(ctx.formParam("descripcion"));
      col.setCategoria(ctx.formParam("categoria"));

      String fuenteIdStr = ctx.formParam("fuente_id");
      if (fuenteIdStr != null) {
        Long fuenteId = Long.parseLong(fuenteIdStr);
        Fuente nuevaFuente = entityManager().find(Fuente.class, fuenteId);
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
          case "Sin definir":
            nuevoAlgo = new Verdadero();
            break;
        }
        col.setAlgoritmoDeConsenso(nuevoAlgo);
      }
      col.recalcularConsenso();
      entityManager().merge(col);
    });
    ctx.redirect("/admin/colecciones/" + ctx.pathParam("id"));
  }

  public void agregarHechoAColeccion(Context ctx) {
    Long idColeccion = Long.parseLong(ctx.pathParam("id"));
    Long idHecho = Long.parseLong(ctx.pathParam("hecho_id"));
    Coleccion col = coleccionController.findById(idColeccion);
    Hecho hecho = hechoController.findById(idHecho);
    if (col != null && hecho != null) {
      col.getHechosConsensuados()
         .add(hecho);
      col.recalcularConsenso();
      coleccionController.persist(col);
    }
    ctx.redirect("/admin/colecciones/" + idColeccion);
  }

  public void removerHechoDeColeccion(Context ctx) {
    Long idColeccion = Long.parseLong(ctx.pathParam("id"));
    Long idHecho = Long.parseLong(ctx.pathParam("hecho_id"));
    Coleccion col = coleccionController.findById(idColeccion);
    col.getHechosConsensuados()
       .removeIf(hecho -> hecho.getId()
                               .equals(idHecho));
    coleccionController.persist(col);
    ctx.redirect("/admin/colecciones" + idColeccion);
  }

  // --- FUENTES ---
  public void listarFuentes(Context ctx, Map<String, Object> model) {
    List<Fuente> fuentes = fuenteController.findAll();
    model.put("fuentes", fuentes);
    ctx.render("admin/fuentes-lista.hbs", model);
  }

  public void crearFuente(Context ctx) {
    try {
      fuenteController.crearFuente(ctx);
      ctx.redirect("/admin/fuentes");
    } catch (RuntimeException e) {
      Map<String, Object> model = new HashMap<>();
      model.put("fuentes", fuenteController.findAll());
      model.put("error", e.getMessage());
      ctx.render("admin/fuentes-lista.hbs", model);
    }
  }

  public void editarFuente(Context ctx, Map<String, Object> model) {
    Long id = Long.parseLong(ctx.pathParam("id"));

    withTransaction(() -> {
      Fuente fuente = entityManager().find(Fuente.class, id);

      if (fuente == null) {
        ctx.status(404)
           .result("Fuente no encontrada");
        return;
      }
      String tipo = fuenteController.determinarTipo(fuente);
      model.put("tipoFuente", tipo);
      model.put("esDinamica", fuente instanceof FuenteDinamica);
      model.put("esApi", fuente instanceof FuenteExternaAPI);
      model.put("esEstatica", fuente instanceof FuenteEstatica);
      model.put("esAgregacion", fuente instanceof FuenteDeAgregacion);
      if (fuente instanceof FuenteDeAgregacion) {
        List<Fuente> fuentesDisponibles = fuenteController.findAll(true);
        FuenteDeAgregacion fda = (FuenteDeAgregacion) fuente;
        List<Long> idsYaAgregados = fda.getFuentesCargadas()
                                       .stream()
                                       .map(Fuente::getId)
                                       .collect(Collectors.toList());
        idsYaAgregados.add(id);
        List<Fuente> filtradas = fuentesDisponibles.stream()
                                                   .filter(f -> !idsYaAgregados.contains(f.getId()))
                                                   .collect(Collectors.toList());
        model.put("fuentesDisponiblesAgregacion", filtradas);
      }
      model.put(
          "coleccionesDirectas",
          ColeccionRepository.instance()
                             .findByFuenteId(id)
      );
      model.put(
          "agregacionesPadre",
          FuenteRepository.instance()
                          .findAgregacionesByHija(id)
      );
      model.put(
          "coleccionesIndirectas",
          FuenteRepository.instance()
                          .findColeccionesIndirectas(id)
      );
      model.put("fuente", fuente);
      List<Hecho> todosLosHechos = hechoController.findAll();
      todosLosHechos.removeAll(fuente.getHechos());
      model.put("hechosDisponibles", todosLosHechos);
    });

    ctx.render("admin/fuente-detalle.hbs", model);
  }

  public void configurarFuente(Context ctx) {
    Long id = Long.parseLong(ctx.pathParam("id"));
    Fuente fte = fuenteController.findById(id);
    if (fte == null) {
      ctx.status(404)
         .result("Fuente no encontrada");
      return;
    }
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
        FuenteRepository.instance()
                        .delete(fuente);
        ctx.redirect("/admin/fuentes");
      } catch (Exception e) {
        ctx.status(500)
           .result("Error al eliminar: " + e.getMessage());
      }
    } else {
      ctx.status(404)
         .result("Fuente no encontrada");
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
      }
    } catch (Exception e) {
      ctx.status(500)
         .result("Error: " + e.getMessage());
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
      }
    } catch (Exception e) {
      ctx.status(500)
         .result("Error: " + e.getMessage());
    }
  }

  public void borrarHechoDeFuente(Context ctx) {
    Long idFuente = Long.parseLong(ctx.pathParam("id"));
    Long idHecho = Long.parseLong(ctx.pathParam("idHecho"));
    Fuente fuente = fuenteController.findById(idFuente);
    fuenteController.borrarHechoDinamico(fuente, idHecho);
    ctx.redirect("/admin/fuentes/" + idFuente);
  }

  public void agregarHechoDeFuente(Context ctx) {
    Long idFuente = Long.parseLong(ctx.pathParam("id"));
    Long idHecho = Long.parseLong(ctx.pathParam("idHecho"));
    Fuente fuente = fuenteController.findById(idFuente);
    fuenteController.agregarHechoDinamico(fuente, hechoController.findById(idHecho));
    ctx.redirect("/admin/fuentes/" + idFuente);
  }
}