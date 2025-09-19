package ar.edu.utn.frba.dds.domain.centralDeEstadisicas;

import ar.edu.utn.frba.dds.domain.coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.reportes.RepositorioDeSolicitudes;
import com.opencsv.CSVWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class CentralDeEstadisticas {

  public void setRepo(RepositorioDeSolicitudes repo) {
    this.repo = repo;
  }

  private RepositorioDeSolicitudes repo;

  public List<Hecho> getAllHechos(List<Coleccion> colecciones) {
    return colecciones.stream().
                      flatMap(lista-> lista.getHechos(repo).stream()).collect(Collectors.toList());
  }

  public Map<String, Long> hechosPorProvinciaDeUnaColeccion(Coleccion coleccion){
    return coleccion.getHechos(repo).stream()
                    .collect(Collectors.groupingBy(Hecho::getProvincia, Collectors.counting()));
  }

  public Map.Entry<String, Long> provinciaConMasHechos(Coleccion coleccion){
    return hechosPorProvinciaDeUnaColeccion(coleccion).entrySet().stream()
                                               .max(Map.Entry.comparingByValue())
                                               .orElse(null);
  }



  public Map<String, Long> hechosPorCategoria(List<Coleccion> colecciones){
    return getAllHechos(colecciones).stream().collect(Collectors.groupingBy(Hecho::getCategoria, Collectors.counting()));
  }

  public Map.Entry<String, Long> categoriaConMasHechos(List<Coleccion> colecciones){
    return hechosPorCategoria(colecciones).entrySet().stream()
                                                      .max(Map.Entry.comparingByValue())
                                                      .orElse(null);
  }



  public Map<String, Long> hechosPorProvinciaSegunCategoria(List<Coleccion> colecciones, String categoria){
    return getAllHechos(colecciones).stream().filter(hecho -> Objects.equals(hecho.getCategoria(), categoria))
                    .collect(Collectors.groupingBy(Hecho::getProvincia, Collectors.counting()));
  }

  public Map.Entry<String, Long> provinciaConMasHechosDeCiertaCategoria(List<Coleccion> colecciones, String categoria){
    return hechosPorProvinciaSegunCategoria(colecciones, categoria).entrySet().stream()
                                          .max(Map.Entry.comparingByValue())
                                          .orElse(null);
  }



  public Map<String, Long> hechosPorHora(List<Coleccion> colecciones, String categoria){
    return getAllHechos(colecciones).stream().filter(hecho -> Objects.equals(hecho.getCategoria(), categoria))
                    .collect(Collectors.groupingBy(hecho -> String.format("%02d", hecho.getFechaSuceso().getHour()), Collectors.counting()));
  }

  public Map.Entry<String, Long> horaConMasHechosDeCiertaCategoria(List<Coleccion> colecciones, String categoria){
    return hechosPorHora(colecciones, categoria).entrySet().stream()
                                                                   .max(Map.Entry.comparingByValue())
                                                                   .orElse(null);
  }


  public double porcentajeDeSolicitudesSpam(){
    return (double) repo.cantidadDeSpamDetectado() / (repo.cantidadDeSpamDetectado() + repo.cantidadSolicitudes()) * 100;
  }

  public void export(Map<String, Long> datos, String rutaArchivo, String[] encabezado) {
    try (CSVWriter writer = new CSVWriter(new FileWriter(rutaArchivo))) {
      writer.writeNext(encabezado);
      for (Map.Entry<String, Long> entry : datos.entrySet()) {
        writer.writeNext(new String[]{entry.getKey(), entry.getValue().toString()});
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}

/*
Específicamente, se piden obtener datos que permitan responder las siguientes preguntas:
De una colección, ¿en qué provincia se agrupan la mayor cantidad de hechos reportados?
¿Cuál es la categoría con mayor cantidad de hechos reportados?
¿En qué provincia se presenta la mayor cantidad de hechos de una cierta categoría?
¿A qué hora del día ocurren la mayor cantidad de hechos de una cierta categoría?
¿Cuántas solicitudes de eliminación son spam?

* */



