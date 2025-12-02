package ar.edu.utn.frba.dds.model.usuario;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import org.apache.commons.codec.digest.DigestUtils;

@Entity
@Table(name = "Usuario") // Nombre de la tabla en la BD
public class Usuario {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "usuario_nombre", unique = true, nullable = false)
  private String userName;

  @Column(name = "usuario_email", unique = true, nullable = false)
  private String email;

  @Column(name = "usuario_password", nullable = false)
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(name = "usuario_rol", nullable = false)
  private Rol rol;


  public Usuario() {
  }

  public Usuario(String email, String userName, String password, Rol rol) {
    this.email = email;
    this.userName = userName;
    this.passwordHash = DigestUtils.sha256Hex(password);
    this.rol = rol;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return passwordHash;
  }

  public void setPassword(String password) {
    this.passwordHash = DigestUtils.sha256Hex(password);
  }

  public Rol getRol() {
    return rol;
  }

  public void setRol(Rol rol) {
    this.rol = rol;
  }

  public boolean chequearPassword(String passwordIntento) {
    return this.passwordHash.equals(DigestUtils.sha256Hex(passwordIntento));
  }

  public String getUserName() {
    return this.userName;
  }

  public void setUserName(String newUserName) {
    this.userName = newUserName;
  }
}