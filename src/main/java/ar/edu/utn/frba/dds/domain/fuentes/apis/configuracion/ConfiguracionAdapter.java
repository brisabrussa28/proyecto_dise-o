package ar.edu.utn.frba.dds.domain.fuentes.apis.configuracion;

import ar.edu.utn.frba.dds.domain.fuentes.apis.FuenteAdapter;

//@Entity
//@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
//@DiscriminatorColumn(name = "tipo_adapter")
public abstract class ConfiguracionAdapter {
//  @Id
//  @GeneratedValue
//  private Long id;

  public abstract FuenteAdapter build();
}