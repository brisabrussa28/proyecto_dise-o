package ar.edu.utn.frba.dds.controller;

import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.repositories.HechoRepository;
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
}
