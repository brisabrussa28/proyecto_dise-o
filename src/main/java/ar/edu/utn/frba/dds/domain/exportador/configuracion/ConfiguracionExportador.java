package ar.edu.utn.frba.dds.domain.exportador.configuracion;

import ar.edu.utn.frba.dds.domain.exportador.Exportador;

import javax.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_exportador")
public abstract class ConfiguracionExportador {

  @Id
  @GeneratedValue
  private Long id;

  /**
   * Este es el método clave. Actúa como una fábrica.
   * Cada subclase (CSV, JSON) sabrá cómo construir su
   * exportador lógico correspondiente.
   */
  public abstract <T> Exportador<T> build();

  public Long getId() {
    return id;
  }
}