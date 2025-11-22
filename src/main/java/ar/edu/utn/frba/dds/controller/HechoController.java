package ar.edu.utn.frba.dds.controller;

import ar.edu.utn.frba.dds.dto.HechoDTO;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.model.hecho.multimedia.Multimedia;
import ar.edu.utn.frba.dds.repositories.HechoRepository;
import io.javalin.http.UploadedFile;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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

  public Hecho modificarHecho(Hecho hechoOriginal, HechoDTO hechoModificado) {
    if (!hechoOriginal.esEditable()) {
      throw new RuntimeException("EL HECHO SE ENCUENTRA FUERA DE RANGO PARA SER EDITADO");
    }
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
    if (hechoModificado.getFechaSuceso() != null) {
      if (hechoModificado.getFechaSuceso()
                         .isBefore(hechoOriginal.getFechasuceso())) {

        hechoOriginal.setFechasuceso(hechoModificado.getFechaSuceso());
      }
    }
    HechoRepository.instance()
                   .save(hechoOriginal);
    return hechoOriginal;
  }

  private void validarHecho(Hecho hecho) {
    if (hecho.getFechasuceso()
             .isAfter(LocalDateTime.now())) {
      throw new RuntimeException("EL HECHO NO PUEDE SUCEDER EN EL FUTURO");
    }
  }

  public List<String> getCategorias() {
    return HechoRepository.instance().getCategorias();
  }
}
