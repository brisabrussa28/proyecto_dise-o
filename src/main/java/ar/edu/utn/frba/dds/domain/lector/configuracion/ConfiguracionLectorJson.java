package ar.edu.utn.frba.dds.domain.lector.configuracion;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.lector.Lector;
import ar.edu.utn.frba.dds.domain.lector.json.LectorJson;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;

//@Entity
//@DiscriminatorValue("JSON")
public class ConfiguracionLectorJson extends ConfiguracionLector {

  @Override
  public <T> Lector<T> build(Class<T> clazz) {
    // La creación de un LectorJson genérico requiere un TypeReference.
    if (clazz.equals(Hecho.class)) {
      // El cast es seguro porque está validado por la clase.
      @SuppressWarnings("unchecked")
      Lector<T> lector = (Lector<T>) new LectorJson<>(new TypeReference<List<Hecho>>() {
      });
      return lector;
    }
    // Se podría agregar un 'else if' para otras clases.

    throw new IllegalArgumentException("No hay un TypeReference de LectorJson definido para la clase: " + clazz.getName());
  }
}