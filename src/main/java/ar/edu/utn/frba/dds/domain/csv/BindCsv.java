package ar.edu.utn.frba.dds.domain.csv;
import com.opencsv.bean.CsvBindAndJoinByName;
import java.util.List;
import java.util.Map;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import com.opencsv.bean.CsvNumber;
import java.util.Date;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

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




    private final List<Map<String, String>> filas = new ArrayList<>();

    public BindCsv(String path, char separator) throws IOException {
      try (CSVReader reader = new CSVReader(new FileReader(path))) {
        String[] headers = reader.readNext();
        if (headers == null) throw new IllegalArgumentException("El archivo CSV no tiene encabezados");

        String[] fila;
        while ((fila = reader.readNext()) != null) {
          Map<String, String> filaMap = new HashMap<>();
          for (int i = 0; i < headers.length && i < fila.length; i++) {
            filaMap.put(headers[i].trim(), fila[i].trim());
          }
          filas.add(filaMap);
        }
      } catch (CsvValidationException e) {
        throw new RuntimeException("Error al parsear el CSV", e);
      }
    }

    public List<Map<String, String>> getFilas() {
      return filas;
    }
}




