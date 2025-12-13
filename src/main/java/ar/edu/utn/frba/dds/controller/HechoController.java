package ar.edu.utn.frba.dds.controller;

import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.model.hecho.etiqueta.Etiqueta;
import ar.edu.utn.frba.dds.model.hecho.multimedia.Multimedia;
import ar.edu.utn.frba.dds.model.info.PuntoGeografico;
import ar.edu.utn.frba.dds.repositories.HechoRepository;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.NotFoundResponse;
import io.javalin.http.UploadedFile;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class HechoController {
  public Hecho subirHecho(Hecho hecho) {
    this.validarHecho(hecho);
    HechoRepository.instance()
                   .save(hecho);
    return hecho;
  }

  public Optional<Hecho> findAny() {
    return HechoRepository.instance()
                          .findAny();
  }

  public Hecho findById(Long id) {
    return HechoRepository.instance()
                          .findById(id);
  }

  public List<Hecho> findAll() {
    return HechoRepository.instance()
                          .findAll();
  }

  public Long countAll() {
    return HechoRepository.instance()
                          .countAll();
  }

  public Hecho modificarHecho(
      Hecho hechoOriginal,
      Hecho hechoModificado
  ) {
    if (hechoModificado.getTitulo() != null) {
      hechoOriginal.setTitulo(hechoModificado.getTitulo());
    }
    if (hechoModificado.getDescripcion() != null) {
      hechoOriginal.setDescripcion(hechoModificado.getDescripcion());
    }
    if (hechoModificado.getEtiquetas() != null) {
      hechoOriginal.setEtiquetas(hechoModificado.getEtiquetas());
    }
    if (hechoModificado.getUbicacion() != null) {
      hechoOriginal.setUbicacion(hechoModificado.getUbicacion());
    }
    if (hechoModificado.getFechasuceso() != null) {
      if (hechoModificado.getFechasuceso()
                         .isBefore(hechoOriginal.getFechasuceso())) {

        hechoOriginal.setFechasuceso(hechoModificado.getFechasuceso());
      }
    }
    List<Multimedia> fotosNuevas = hechoModificado.getFotos();
    if (fotosNuevas != null && !fotosNuevas.isEmpty()) {
      fotosNuevas.forEach(foto -> hechoOriginal.agregarFoto(foto));
    }
    if (!hechoModificado.getCategoria()
                        .isBlank()) {
      hechoOriginal.setCategoria(hechoModificado.getCategoria());
    }
    if (!hechoModificado.getDireccion()
                        .isBlank()) {
      hechoOriginal.setDireccion(hechoModificado.getDireccion());
    }
    if (hechoModificado.getProvincia() != null && !hechoModificado.getProvincia()
                                                                  .isBlank()) {
      hechoOriginal.setProvincia(hechoModificado.getProvincia());
    }
    return hechoOriginal;
  }

  private void validarHecho(Hecho hecho) {
    if (hecho.getFechasuceso()
             .isAfter(LocalDateTime.now())) {
      throw new RuntimeException("EL HECHO NO PUEDE SUCEDER EN EL FUTURO");
    }
  }

  public List<String> getCategorias() {
    return HechoRepository.instance()
                          .getCategorias();
  }

  public List<String> getEtiquetas() {
    return HechoRepository.instance()
                          .getEtiquetas();
  }

  public void editarHecho(Context ctx, Map<String, Object> model) {
    Long idHecho = Long.parseLong(ctx.pathParam("id"));
    Long idUsuario = ctx.sessionAttribute("usuario_id");
    Hecho hecho = this.findById(idHecho);

    if (hecho.getFechasuceso() != null) {
      String fechaFormateada = hecho.getFechasuceso()
                                    .truncatedTo(ChronoUnit.MINUTES) // Elimina segundos y nanos
                                    .toString();

      model.put("fechaLimite", fechaFormateada);
    }

    if (!hecho.getAutor()
              .getId()
              .equals(idUsuario)) {
      ctx.status(HttpStatus.FORBIDDEN);
    }
    model.put("hecho", hecho);
    if (hecho.getEtiquetas() != null) {
      String etiquetasStr = String.join(
          ", ",
          hecho.getEtiquetas()
               .stream()
               .map(e -> e.getNombre())
               .toList()
      );
      model.put("etiquetasTexto", etiquetasStr);
    }
    ctx.render("/hecho-editar.hbs", model);
  }

  public void actualizarHecho(Context ctx) {
    Long idHecho = Long.parseLong(ctx.pathParam("id"));
    Hecho hechoOriginal = findById(idHecho);
    Hecho hechoModificado = new Hecho();

    hechoModificado.setTitulo(ctx.formParam("titulo"));
    hechoModificado.setCategoria(ctx.formParam("categoria"));
    hechoModificado.setDescripcion(ctx.formParam("descripcion"));

    String etiquetasInput = ctx.formParam("etiquetas");
    if (etiquetasInput != null) {
      List<Etiqueta> nuevasEtiquetas = Arrays.stream(etiquetasInput.split(","))
                                             .map(String::trim)
                                             .filter(s -> !s.isEmpty())
                                             .map(nombre -> new Etiqueta(nombre)) // Instanciamos tu clase Etiqueta
                                             .collect(Collectors.toList());

      hechoModificado.setEtiquetas(nuevasEtiquetas); // Asumo que tienes setEtiquetas(List<Etiqueta>)
    }
    String fechaStr = ctx.formParam("fechaSuceso");

    if (fechaStr != null && !fechaStr.isBlank()) {
      hechoModificado.setFechasuceso(LocalDateTime.parse(fechaStr));
    }

    List<String> indicesBorrar = ctx.formParams("fotos_borrar");
    if (!indicesBorrar.isEmpty() && hechoOriginal.getFotos() != null) {
      List<Integer> indices = indicesBorrar.stream()
                                           .map(Integer::parseInt)
                                           .sorted(Comparator.reverseOrder())
                                           .collect(Collectors.toList());

      for (int i : indices) {
        if (i >= 0 && i < hechoOriginal.getFotos()
                                       .size()) {
          hechoOriginal.quitar(i);
        }
      }
    }

    List<UploadedFile> archivos = ctx.uploadedFiles("nuevas_fotos");
    List<Multimedia> listaNuevasFotos = new ArrayList<>();
    for (UploadedFile archivo : archivos) {
      if (archivo.size() > 0) {
        try {
          byte[] bytesImagen = archivo.content()
                                      .readAllBytes();
          Multimedia nuevaFoto = new Multimedia(
              archivo.filename(),
              archivo.contentType(),
              bytesImagen
          );
          // Agregamos a la lista temporal, NO guardamos todavía
          listaNuevasFotos.add(nuevaFoto);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
    if (!listaNuevasFotos.isEmpty()) {
      hechoModificado.setFotos(listaNuevasFotos);
    }

    String direccion = ctx.formParam("direccion");
    hechoModificado.setDireccion(direccion);
    Double lat = Double.valueOf(Objects.requireNonNull(ctx.formParam("latitud")));
    Double lng = Double.valueOf(Objects.requireNonNull(ctx.formParam("longitud")));

    hechoModificado.setUbicacion(new PuntoGeografico(lat, lng));
    hechoModificado.setProvincia(ctx.formParam("provincia"));

    modificarHecho(hechoOriginal, hechoModificado);
    HechoRepository.instance()
                   .save(hechoOriginal);
    ctx.redirect("/");
  }

  public void getFoto(Context ctx) {
    Long idHecho = Long.parseLong(ctx.pathParam("id"));
    int indice = Integer.parseInt(ctx.pathParam("indice"));

    Hecho hecho = this.findById(idHecho);

    if (hecho != null && hecho.getFotos() != null && indice < hecho.getFotos()
                                                                   .size()) {
      Multimedia foto = hecho.getFotos()
                             .get(indice);

      ctx.contentType(foto.getMimetype()); // ej: "image/jpeg"
      ctx.result(foto.getDatos());         // Los bytes de la DB
    } else {
      ctx.status(404);
    }
  }

  public void verHecho(Context ctx, Map<String, Object> model) {
    Long idHecho = Long.parseLong(ctx.pathParam("id"));
    Hecho hecho = findById(idHecho);
    if (hecho == null) {
      throw new NotFoundResponse("Hecho no encontrado");
    }

    model.put("hecho", hecho);

    Long usuarioId = ctx.sessionAttribute("usuario_id");
    boolean esPropietario = hecho.getAutor() != null && hecho.getAutor()
                                                             .getId()
                                                             .equals(usuarioId);
    model.put("esPropietario", esPropietario);

    if (hecho.getFechasuceso() != null) {
      java.time.format.DateTimeFormatter formatter =
          java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
      model.put("fechaFormateada",
                hecho.getFechasuceso()
                     .format(formatter)
      );
    }

    ctx.render("hecho-detail.hbs", model);
  }
}
