package ar.edu.utn.frba.dds.model.hecho.multimedia;

import javax.persistence.Embeddable;

@Embeddable
public class Multimedia {
  private String url;
  private String alt;

  public Multimedia(String url, String alt) {
    this.url = url;
    this.alt = alt;
  }
}
