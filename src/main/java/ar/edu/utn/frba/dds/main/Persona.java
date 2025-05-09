package ar.edu.utn.frba.dds.main;

/**
 * Persona.
 * */
public abstract class Persona {
  protected String nombre;
  protected String email;

  /**
   * Constructor Persona.
   * */
  public Persona(String nombre, String email) {
    this.nombre = nombre;
    this.email = email;
  }

  public String getNombre() {
    return nombre;
  }

  public String getEmail() {
    return email;
  }
}