package ar.edu.utn.frba.dds.model.lector.csv.filaconverter.controller;

import ar.edu.utn.frba.dds.controller.FuenteController;
import ar.edu.utn.frba.dds.model.coleccion.Coleccion;
import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.Absoluta;
import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.AlgoritmoDeConsenso;
import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.MayoriaSimple;
import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.MultiplesMenciones;
import ar.edu.utn.frba.dds.model.filtro.CondicionBuilder;
import ar.edu.utn.frba.dds.model.filtro.condiciones.CondicionTrue;
import ar.edu.utn.frba.dds.model.filtro.condiciones.Operador;
import ar.edu.utn.frba.dds.model.fuentes.Fuente;
import ar.edu.utn.frba.dds.repositories.ColeccionRepository;
import io.javalin.http.Context;
import java.util.List;

public class ColeccionController {
  ar.edu.utn.frba.dds.controller.FuenteController fuenteController = new FuenteController();

  public void crearColeccion(Context ctx) {
    try {
      String nombre = ctx.formParam("nombre");
      String descripcion = ctx.formParam("descripcion");
      String categoria = ctx.formParam("categoria");

      // Algoritmo de consenso
      String algoritmoStr = ctx.formParam("algoritmo");
      AlgoritmoDeConsenso algoritmo = determinarAlgoritmo(algoritmoStr);

      // Fuente de datos
      String modo_fuente = ctx.formParam("modo_fuente");
      Fuente fuente = null;

      if ("nueva".equals(modo_fuente)) {
        fuente = fuenteController.crearFuente(ctx);
        if (fuente == null) {
          throw new RuntimeException("No se pudo crear la fuente");
        }
      } else {
        String fuenteIdStr = ctx.formParam("fuente_id");
        if (fuenteIdStr != null && !fuenteIdStr.isEmpty()) {
          Long fuenteId = Long.parseLong(fuenteIdStr);
          fuente = fuenteController.findById(fuenteId);
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

  // --- MÉTODO ACTUALIZADO PARA AGREGAR CONDICIONES (CON SOPORTE BETWEEN) ---
  public void agregarCondicion(Context ctx) {
    try {
      Long id = Long.parseLong(ctx.pathParam("id"));
      Coleccion coleccion = this.findById(id);

      if (coleccion != null) {
        String logica = ctx.formParam("cond_logica"); // AND / OR
        String campo = ctx.formParam("cond_campo");
        String operadorStr = ctx.formParam("cond_operador");
        String valor = ctx.formParam("cond_valor");
        String valor2 = ctx.formParam("cond_valor_2"); // Segundo valor para rangos

        if (campo == null || campo.isBlank() || valor == null || valor.isBlank()) {
          ctx.redirect("/admin/colecciones/" + id);
          return;
        }

        CondicionBuilder builder = new CondicionBuilder(coleccion.getCondicion());

        // Manejo especial para BETWEEN (Rango)
        if ("BETWEEN".equals(operadorStr)) {
          if (valor2 == null || valor2.isBlank()) {
            throw new IllegalArgumentException("Se requiere un segundo valor para el rango.");
          }

          // Descomponemos BETWEEN en dos condiciones.
          if ("OR".equalsIgnoreCase(logica)) {
            builder.or(campo, Operador.MAYOR_QUE, valor);
          } else {
            builder.and(campo, Operador.MAYOR_QUE, valor);
          }
          // El cierre del rango siempre es restrictivo (AND) respecto al inicio
          builder.and(campo, Operador.MENOR_QUE, valor2);

        } else {
          // Mapeo manual de strings del front a Enum Operador disponible
          Operador operador;
          if (operadorStr == null) operadorStr = "IGUAL";

          switch (operadorStr) {
            case "MAYOR":
            case "MAYOR_QUE":
            case "MAYOR_IGUAL": // Fallback a MAYOR_QUE por limitación del Enum
              operador = Operador.MAYOR_QUE;
              break;
            case "MENOR":
            case "MENOR_QUE":
            case "MENOR_IGUAL": // Fallback a MENOR_QUE por limitación del Enum
              operador = Operador.MENOR_QUE;
              break;
            case "DISTINTO":
              operador = Operador.DISTINTO;
              break;
            case "CONTIENE":
              // Fallback a IGUAL porque CONTIENE no existe en el Enum Operador provisto
              operador = Operador.IGUAL;
              break;
            default:
              operador = Operador.IGUAL;
          }

          if ("OR".equalsIgnoreCase(logica)) {
            builder.or(campo, operador, valor);
          } else {
            builder.and(campo, operador, valor);
          }
        }

        // Actualizamos y guardamos
        coleccion.setCondicion(builder.build());
        this.persist(coleccion);

        ctx.redirect("/admin/colecciones/" + id);
      } else {
        ctx.status(404).result("Colección no encontrada");
      }
    } catch (Exception e) {
      e.printStackTrace();
      ctx.status(500).result("Error al agregar condición: " + e.getMessage());
    }
  }

  // --- MÉTODO PARA ELIMINAR CONDICIONES ---
  public void eliminarCondicion(Context ctx) {
    try {
      Long id = Long.parseLong(ctx.pathParam("id"));
      Coleccion coleccion = this.findById(id);

      if (coleccion != null) {
        // Reseteamos a una condición True (filtro vacío/nulo)
        coleccion.setCondicion(new CondicionTrue());

        // Recalculamos consenso para reflejar el cambio (opcional, pero recomendado)
        coleccion.recalcularConsenso();

        this.persist(coleccion);
        ctx.redirect("/admin/colecciones/" + id);
      } else {
        ctx.status(404).result("Colección no encontrada");
      }
    } catch (Exception e) {
      e.printStackTrace();
      ctx.status(500).result("Error al eliminar condiciones: " + e.getMessage());
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