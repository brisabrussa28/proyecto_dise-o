package ar.edu.utn.frba.dds.domain.csv;
import com.opencsv.bean.CsvBindAndJoinByName;
import java.util.List;
import java.util.Map;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import com.opencsv.bean.CsvNumber;
import java.util.Date;

public class BindCsv {

  @CsvBindByName(column = "titulo")
  private String titulo;

  @CsvBindByName(column = "descripcion")
  private String descripcion;

  @CsvBindByName(column = "categoria")
  private String categoria;

  @CsvBindByName(column = "fechaSuceso")
  //@CsvDate("dd/MM/yyyy")
  //private Date fechaSuceso;
  private String fechaSuceso;

  @CsvBindByName(column = "latitud")
  //private float latitud;
  private String latitud;

  @CsvBindByName(column = "longitud")
  //private float longitud;
  private String longitud;



  // Getters and setters go here.

  public String getTitulo() {
    return titulo;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public String getCategoria() {
    return categoria;
  }
/*
  public Date getFechaSuceso() {
    return fechaSuceso;
  }*/
  public String getFechaSuceso() {
    return fechaSuceso;
  }
/*
  public float getLatitud() {
    return latitud;
  }

  public float getLongitud() {
    return longitud;
  }*/

  public String getLatitud() {
    return latitud;
  }

  public String getLongitud() {
    return longitud;
  }

  public void setTitulo(String titulo) {
    this.titulo = titulo;
  }

  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }

  public void setCategoria(String categoria) {
    this.categoria = categoria;
  }
/*
  public void setFechaSuceso(Date fechaSuceso) {
    this.fechaSuceso = fechaSuceso;
  }*/
  public void setFechaSuceso(String fechaSuceso) {
    this.fechaSuceso = fechaSuceso;
  }

  public void setLatitud(String latitud) {
    this.latitud = latitud;
  }

  public void setLongitud(String longitud) {
    this.longitud = longitud;
  }
  /*
  public void setLatitud(float latitud) {
    this.latitud = latitud;
  }

  public void setLongitud(float longitud) {
    this.longitud = longitud;
  }
   */

    @CsvBindAndJoinByName(elementType = String.class)
    private Map<String, String> columnas;

    public String obtenerValorPorColumna(String nombreColumna) {
      if (columnas != null && columnas.containsKey(nombreColumna)) {
        return columnas.get(nombreColumna);
      }
      return null;
    }

    public String concatenarColumnas(List<String> nombresColumnas, String separador) {
      StringBuilder resultado = new StringBuilder();
      for (String nombreColumna : nombresColumnas) {
        String valor = obtenerValorPorColumna(nombreColumna);
        if (valor != null && !valor.isBlank()) {
          if (resultado.length() > 0) {
            resultado.append(separador);
          }
          resultado.append(valor);
        }
      }
      return resultado.toString();
    }
}


