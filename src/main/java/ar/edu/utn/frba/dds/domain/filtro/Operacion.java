package ar.edu.utn.frba.dds.domain.filtro;


import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.List;

public interface Operacion {
  List<Hecho> ejecutar(List<Hecho> hechos);
}