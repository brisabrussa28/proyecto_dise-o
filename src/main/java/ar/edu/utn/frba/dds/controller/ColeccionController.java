package ar.edu.utn.frba.dds.controller;

import ar.edu.utn.frba.dds.model.coleccion.Coleccion;
import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.Absoluta;
import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.AlgoritmoDeConsenso;
import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.MayoriaSimple;
import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.MultiplesMenciones;
import ar.edu.utn.frba.dds.model.fuentes.Fuente;
import ar.edu.utn.frba.dds.repositories.ColeccionRepository;
import io.javalin.http.Context;
import java.util.List;

public class ColeccionController {
  FuenteController fuenteController = new FuenteController();

  public void crearColeccion(Context ctx) {
    try {
      String nombre = ctx.formParam("nombre");
      String descripcion = ctx.formParam("descripcion");
      String categoria = ctx.formParam("categoria");

      String algoTipo = ctx.formParam("algoritmo_tipo");
      AlgoritmoDeConsenso algoritmo = null;
      switch (algoTipo) {
        case "Absoluta":
          algoritmo = new Absoluta();
          break;
        case "May_simple":
          algoritmo = new MayoriaSimple();
          break;
        case "Mult_menciones":
          algoritmo = new MultiplesMenciones();
          break;
        default:
          algoritmo = new MayoriaSimple(); // Default seguro
      }

      String modoFuente = ctx.formParam("modo_fuente");
      Fuente fuente;

      if ("nueva".equals(modoFuente)) {
        fuente = fuenteController.crearFuente(ctx);
        fuenteController.save(fuente);
      } else {
        Long fuenteId = Long.parseLong(ctx.formParam("fuente_id"));
        fuente = fuenteController.findById(fuenteId);
      }
      Coleccion col = new Coleccion(nombre, fuente, descripcion, categoria, algoritmo);
      col.recalcularConsenso();
      ColeccionRepository.instance()
                         .save(col);
      ctx.redirect("/admin/colecciones");
    } catch (Exception e) {
      e.printStackTrace();
      ctx.status(400)
         .result("Error al crear colección: " + e.getMessage());
    }
  }

  public void persist(Coleccion colecccion) {
    ColeccionRepository.instance()
                       .save(colecccion);
  }

  public List<String> getCategorias() {
    return ColeccionRepository.instance()
                              .getCategorias();
  }

  public List<Coleccion> findAll() {
    return ColeccionRepository.instance()
                              .findAll();
  }

  public Coleccion findById(Long id) {
    return ColeccionRepository.instance()
                              .findById(id);
  }

  public Long countAll() {
    return ColeccionRepository.instance()
                              .countAll();
  }

  public record ColeccionDTO(Long id, String titulo) {}

  public List<ColeccionDTO> getColeccionesDTO() {
    return ColeccionRepository.instance()
                              .findAll().stream()
                              .map(c -> new ColeccionDTO(c.getId(), c.getTitulo()))
                              .toList();
  }
}
