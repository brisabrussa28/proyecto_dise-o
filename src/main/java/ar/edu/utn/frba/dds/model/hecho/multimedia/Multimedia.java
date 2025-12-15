package ar.edu.utn.frba.dds.model.hecho.multimedia;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.Embeddable;
import javax.persistence.Lob;

@Embeddable
public class Multimedia {

  String alt;
  String mimetype;
  @Lob
  @JsonIgnore
  private byte[] contenido;

  protected Multimedia() {
  }

  public Multimedia(String alt, String mimetype, byte[] contenido) {
    this.alt = alt;
    this.mimetype = mimetype;
    this.contenido = contenido;
  }

  public String getMimetype() {
    return this.mimetype;
  }

  public String getAlt() {
    return this.alt;
  }

  public byte[] getDatos() {
    return this.contenido;
  }

  @JsonProperty("esVideo")
  public boolean esVideo() {
    return this.mimetype.equals("video/mp4");
  }
}
