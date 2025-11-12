package ar.edu.utn.frba.dds.controller;

import ar.edu.utn.frba.dds.dto.HechoDTO;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.repositories.HechoRepository;
import java.util.List;
import java.util.Optional;

public class HechoController {
  public Hecho subirHecho(Hecho hecho) {
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
}
