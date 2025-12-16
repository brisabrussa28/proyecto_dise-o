package ar.edu.utn.frba.dds.model.lector.csv.filaconverter.model.fuentes.apis.serviciometamapa;

import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.model.reportes.Solicitud;

import java.time.LocalDateTime;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MetaMapaService {
  @GET("hechos")
  Call<List<Hecho>> hechos(
      @Query("categoria") String categoria,
      @Query("ubicacion") String ubicacion,
      @Query("fechaReporteDesde") LocalDateTime fechaReporteDesde,
      @Query("fechaReporteHasta") LocalDateTime fechaReporteHasta,
      @Query("fechaAcontecimientoDesde") LocalDateTime fechaAcontecimientoDesde,
      @Query("fechaAcontecimientoHasta") LocalDateTime fechaAcontecimientoHasta
  );

  @GET("colecciones/{id}/hechos")
  Call<List<Hecho>> hechos(
      @Path("id") int id,
      @Query("categoria") String categoria,
      @Query("ubicacion") String ubicacion,
      @Query("fechaReporteDesde") LocalDateTime fechaReporteDesde,
      @Query("fechaReporteHasta") LocalDateTime fechaReporteHasta,
      @Query("fechaAcontecimientoDesde") LocalDateTime fechaAcontecimientoDesde,
      @Query("fechaAcontecimientoHasta") LocalDateTime fechaAcontecimientoHasta
  );

  @POST("solicitudes")
  Call<Void> crearSolicitud(@Body Solicitud solicitud);
}