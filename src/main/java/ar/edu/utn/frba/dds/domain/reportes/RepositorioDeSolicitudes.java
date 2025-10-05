package ar.edu.utn.frba.dds.domain.reportes;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Repositorio de Solicitudes. Responsable únicamente del almacenamiento
 * y recuperación de las solicitudes.
 */
public class RepositorioDeSolicitudes {

  private final Set<Solicitud> solicitudes = new HashSet<>();

  public void guardar(Solicitud solicitud) {
    // Si ya existe, la elimina para luego agregar la versión actualizada.
    solicitudes.remove(solicitud);
    solicitudes.add(solicitud);
  }

  public Optional<Solicitud> buscarPorHechoYRazon(Hecho hecho, String razon) {
    return solicitudes.stream()
                      .filter(s -> s.getHechoSolicitado().equals(hecho) && s.getRazonEliminacion().equals(razon))
                      .findFirst();
  }

  public List<Solicitud> obtenerTodas() {
    return new ArrayList<>(solicitudes);
  }

  public List<Solicitud> obtenerPorEstado(EstadoSolicitud estado) {
    return solicitudes.stream()
                      .filter(s -> s.getEstado() == estado)
                      .collect(Collectors.toList());
  }

  public int cantidadTotal() {
    return solicitudes.size();
  }
}