package ar.edu.utn.frba.dds.domain.exportador.configuracion;


import ar.edu.utn.frba.dds.domain.exportador.Exportador;
import ar.edu.utn.frba.dds.domain.exportador.csv.ExportadorCSV;
import ar.edu.utn.frba.dds.domain.exportador.csv.modoexportacion.*;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.DiscriminatorValue;

@Entity
@DiscriminatorValue("CSV")
public class ConfiguracionExportadorCsv extends ConfiguracionExportador {

  private char separador;
  private char quote;

  @Enumerated(EnumType.STRING)
  private ModoExportacionEnum modo;

  // Constructor para JPA
  public ConfiguracionExportadorCsv() {}

  // Constructor para uso general
  public ConfiguracionExportadorCsv(char separador, char quote, ModoExportacionEnum modo) {
    this.separador = separador;
    this.quote = quote;
    this.modo = modo;
  }

  @Override
  public <T> Exportador<T> build() {
    // Usa sus propios datos de configuración para construir el exportador lógico.
    ModoExportacion modoStrategy = crearModoStrategy();
    return new ExportadorCSV<>(this.separador, this.quote, modoStrategy);
  }

  private ModoExportacion crearModoStrategy() {
    switch (this.modo) {
      case ANEXAR: return new ModoAnexar();
      case SOBRESCRIBIR: return new ModoSobrescribir();
      case NUMERAR: return new ModoNumerar();
      case TIMESTAMP: return new ModoTimestamp();
      default: throw new IllegalStateException("Modo de exportación desconocido: " + this.modo);
    }
  }
}