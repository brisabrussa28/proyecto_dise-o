package ar.edu.utn.frba.dds.main;

import ar.edu.utn.frba.dds.domain.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.Hecho;
import ar.edu.utn.frba.dds.domain.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.Solicitud;
import java.time.LocalDateTime;
import java.util.List;


//NOTE: Contribuyente deberia extender de Visualizador
// ya que tiene el mismo comportamiento que el visualizador además de poder cargar archivos, y
// necesitamos almacenar sí o sí el nombre sin importar que esté registrado o no y tmb otros
// datos que están en el enunciado que no son obligatorios.

public class Contribuyente extends Visualizador {
  public Contribuyente(String nombre, String email) {
    super(nombre, email);
  }

  public boolean esAnonimo() {
    return (this.getNombre() == null || this.getNombre().isBlank()) && (this.getEmail() == null || this.getEmail().isBlank());
  }

  public Hecho crearHecho(
      String titulo,
      String descripcion,
      String categoria,
      String direccion,
      PuntoGeografico ubicacion,
      LocalDateTime fecha,
      List<String> etiquetas,
      FuenteDinamica fuente
  ) {
    Hecho hecho = new Hecho(titulo, descripcion, categoria, direccion, ubicacion, fecha, fuente, etiquetas);
    fuente.agregarHecho(hecho);
    return hecho;
  }

  void solicitarEliminacion(Hecho hecho, String motivo) {
    Solicitud solicitudEliminacion = new Solicitud(hecho, motivo);
    //
  }
}



class MotivoException extends RuntimeException {
  String motivo;
  public MotivoException(String motivo) {
    super(motivo);
  }
}
