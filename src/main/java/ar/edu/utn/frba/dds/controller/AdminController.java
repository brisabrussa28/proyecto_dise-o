package ar.edu.utn.frba.dds.controller;

import ar.edu.utn.frba.dds.model.coleccion.Coleccion;
import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.AlgoritmoDeConsenso;
import ar.edu.utn.frba.dds.model.fuentes.Fuente;
import ar.edu.utn.frba.dds.model.fuentes.FuenteDeAgregacion;
import ar.edu.utn.frba.dds.model.fuentes.FuenteEstatica;
import ar.edu.utn.frba.dds.model.fuentes.FuenteExternaAPI;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.repositories.AlgoritmoRepository;
import ar.edu.utn.frba.dds.repositories.ColeccionRepository;
import ar.edu.utn.frba.dds.repositories.FuenteRepository;
import ar.edu.utn.frba.dds.repositories.HechoRepository;
import ar.edu.utn.frba.dds.repositories.SolicitudesRepository;
import io.javalin.http.Context;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminController {
  public void mostrarDashboard(Context ctx, Map<String, Object> model) {

    Long totalHechos = HechoRepository.instance()
                                      .countAll();
    Long totalColecciones = ColeccionRepository.instance()
                                               .countAll();
    Long totalReportes = SolicitudesRepository.instance()
                                              .cantidadTotal();

    model.put("cantHechos", totalHechos);
    model.put("cantColecciones", totalColecciones);
    model.put("cantReportes", totalReportes);
    ctx.render("admin/dashboard.hbs", model);
  }

  public void listarColecciones(Context ctx, Map<String, Object> model) {
    List<Coleccion> colecciones = ColeccionRepository.instance()
                                                     .findAll();
    model.put("colecciones", colecciones);
    ctx.render("/admin/colecciones-lista.hbs", model);
  }

  public void editarColeccion(Context ctx, Map<String, Object> model) {
    Long id = Long.parseLong(ctx.pathParam("id"));
    Coleccion coleccion = ColeccionRepository.instance()
                                             .findById(id);

    List<Hecho> hechos = HechoRepository.instance()
                                        .findAll();
    model.put("coleccion", coleccion);
    model.put("hechosDisponibles", hechos);

    ctx.render("/admin/coleccion-detalle.hbs", model);
  }

  public void agregarHechoAColeccion(Context ctx) {
    Long idColeccion = Long.parseLong(ctx.pathParam("id"));
    Long idHecho = Long.parseLong(ctx.pathParam("hecho_id"));

    Coleccion col = ColeccionRepository.instance()
                                       .findById(idColeccion);
    Hecho hecho = HechoRepository.instance()
                                 .findById(idHecho);

    if (col != null && hecho != null) {
      col.getHechosConsensuados()
         .add(hecho);
      ColeccionRepository.instance()
                         .save(col);
    }

    ctx.redirect("/admin/colecciones/" + idColeccion);
  }

  public void removerHechoDeColeccion(Context ctx) {
    Long idColeccion = Long.parseLong(ctx.pathParam("id"));
    Long idHecho = Long.parseLong(ctx.pathParam("hecho_id"));

    Coleccion col = ColeccionRepository.instance()
                                       .findById(idColeccion);
    col.getHechosConsensuados()
       .removeIf(hecho -> hecho.getId()
                               .equals(idHecho));

    ColeccionRepository.instance()
                       .save(col);
    ctx.redirect("/admin/colecciones" + idColeccion);
  }

  public void crearColeccion(Context ctx) {
    String nombre = ctx.formParam("nombre");
    String descripcion = ctx.formParam("descripcion");
    String categoria = ctx.formParam("categoria");
    AlgoritmoDeConsenso algoritmoDeConsenso = AlgoritmoRepository.instance()
                                                                 .findById(Long.parseLong(ctx.formParam("algoritmo")));
    Fuente fuente = FuenteRepository.instance()
                                    .findById(Long.parseLong(ctx.formParam("fuente")));
    Coleccion col = new Coleccion(nombre, fuente, descripcion, categoria, algoritmoDeConsenso);
    ColeccionRepository.instance()
                       .save(col);
    ctx.redirect("/admin/colecciones");
  }

  public void listarFuentes(Context ctx, Map<String, Object> model) {
    List<Fuente> fuentes = FuenteRepository.instance()
                                           .findAll();
    model.put("fuentes", fuentes);
    ctx.render("/admin/fuentes-lista.hbs", model);
  }

  public void crearFuente(Context ctx) {
    String nombre = ctx.formParam("nombre");
    String tipo = ctx.formParam("tipo"); // "ESTATICA", "API", "AGREGACION"

    Fuente nuevaFuente = null;

    switch (tipo) {
      case "ESTATICA":
        nuevaFuente = new FuenteEstatica();
        // La estática nace vacía, luego le agregas hechos
        break;

      case "API":
        FuenteExternaAPI api = new FuenteExternaAPI();
        api.setUrlBase(ctx.formParam("url"));
        nuevaFuente = api;
        break;

      case "AGREGACION":
        // Podrías recibir IDs de fuentes hijas aquí, o crearla vacía
        nuevaFuente = new FuenteDeAgregacion();
        break;
    }

    if (nuevaFuente != null) {
      nuevaFuente.setNombre(nombre);
      FuenteRepository.instance()
                      .save(nuevaFuente);
    }

    ctx.redirect("/admin/fuentes");
  }
}
