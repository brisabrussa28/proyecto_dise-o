package ar.edu.utn.frba.dds.model.lector.csv.filaconverter.dto;

public class LoginDTO {
  private String email;
  private String password;
  private String userName;

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }


  public void setPassword(String password) {
    this.password = password;
  }

  public String getUserName() {
    return this.userName;
  }

  public void setUserName(String newUserName) {
    this.userName = newUserName;
  }
}