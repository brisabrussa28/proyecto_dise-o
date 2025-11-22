package ar.edu.utn.frba.dds.model.hecho.multimedia;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.Embeddable;

@Embeddable
public class Multimedia {
  @JsonProperty("url")
  private String url;

  protected Multimedia() {
  }

  public Multimedia(String url) {
    this.url = url;
  }
}
