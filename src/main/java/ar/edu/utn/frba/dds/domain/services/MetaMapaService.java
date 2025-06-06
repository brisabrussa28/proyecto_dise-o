package ar.edu.utn.frba.dds.domain.services;

import ar.edu.utn.frba.dds.domain.entities.ListadoDeHechos;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.reportes.Solicitud;
import java.util.Date;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;


public interface MetaMapaService {

  @GET("hechos")
  Call<ListadoDeHechos> hechos(
      @Query("categoria") String categoria,
      @Query("ubicacion") PuntoGeografico ubicacion,
      @Query("fecha_reporte_desde") Date fecha_reporte_desde,
      @Query("fecha_reporte_hasta") Date fecha_reporte_hasta,
      @Query("fecha_acontecimiento_desde") Date fecha_acontecimiento_desde,
      @Query("fecha_acontecimiento_hasta") Date fecha_acontecimiento_hasta
  ); //Devuelve los hechos

  @GET("colecciones/{id}/hechos")
  Call<ListadoDeHechos> hechos(
      @Path("id") int groupId,
      @Query("categoria") String categoria,
      @Query("ubicacion") PuntoGeografico ubicacion,
      @Query("fecha_reporte_desde") Date fecha_reporte_desde,
      @Query("fecha_reporte_hasta") Date fecha_reporte_hasta,
      @Query("fecha_acontecimiento_desde") Date fecha_acontecimiento_desde,
      @Query("fecha_acontecimiento_hasta") Date fecha_acontecimiento_hasta
  );
  @POST("solicitudes")
  Call<Solicitud> crearSolicitud(@Body Solicitud solicitud);
}
