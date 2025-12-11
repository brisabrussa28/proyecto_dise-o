package ar.edu.utn.frba.dds.controller;

import ar.edu.utn.frba.dds.model.coleccion.Coleccion;
import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.Absoluta;
import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.AlgoritmoDeConsenso;
import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.MayoriaSimple;
import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.MultiplesMenciones;
import ar.edu.utn.frba.dds.model.fuentes.Fuente;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import io.javalin.http.Context;
import java.util.List;
import java.util.Map;

public class AdminController {
  ColeccionController coleccionController = new ColeccionController();
  FuenteController fuenteController = new FuenteController();
  HechoController hechoController = new HechoController();
  SolicitudController solicitudController = new SolicitudController();

  public void mostrarDashboard(Context ctx, Map<String, Object> model) {

    Long totalHechos = hechoController.countAll();
    Long totalColecciones = coleccionController.countAll();
    Long totalReportes = solicitudController.countAll();

    model.put("cantHechos", totalHechos);
    model.put("cantColecciones", totalColecciones);
    model.put("cantReportes", totalReportes);
    ctx.render("admin/dashboard.hbs", model);
  }

  public void listarColecciones(Context ctx, Map<String, Object> model) {
    List<Coleccion> colecciones = coleccionController.findAll();
    List<Fuente> fuentes = fuenteController.findAll();
    model.put("colecciones", colecciones);
    model.put("fuentes", fuentes);
    ctx.render("/admin/colecciones-lista.hbs", model);
  }

  public void editarColeccion(Context ctx, Map<String, Object> model) {
    Long id = Long.parseLong(ctx.pathParam("id"));
    Coleccion coleccion = coleccionController.findById(id);
    List<Fuente> fuentes = fuenteController.findAll();
    model.put("coleccion", coleccion);
    model.put("fuentesDisponibles", fuentes);


    ctx.render("/admin/coleccion-detalle.hbs", model);
  }

  public void agregarHechoAColeccion(Context ctx) {
    Long idColeccion = Long.parseLong(ctx.pathParam("id"));
    Long idHecho = Long.parseLong(ctx.pathParam("hecho_id"));

    Coleccion col = coleccionController
        .findById(idColeccion);
    Hecho hecho = hechoController
        .findById(idHecho);

    if (col != null && hecho != null) {
      col.getHechosConsensuados()
         .add(hecho);
      coleccionController
          .persist(col);
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

    coleccionController
        .persist(col);
    ctx.redirect("/admin/colecciones" + idColeccion);
  }

  public void crearColeccion(Context ctx) {
    coleccionController.crearColeccion(ctx);
  }

  public void listarFuentes(Context ctx, Map<String, Object> model) {
    List<Fuente> fuentes = fuenteController.findAll();
    model.put("fuentes", fuentes);
    ctx.render("/admin/fuentes-lista.hbs", model);
  }

  public void crearFuente(Context ctx) {
    fuenteController.crearFuente(ctx);
    ctx.redirect("/admin/fuentes");
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

    // Volvemos a la misma pantalla con mensaje de éxito (opcional) o redirect simple
    ctx.redirect("/admin/colecciones/" + id);
  }
}
