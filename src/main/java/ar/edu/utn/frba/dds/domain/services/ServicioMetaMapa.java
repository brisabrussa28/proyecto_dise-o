package ar.edu.utn.frba.dds.domain.services;

import ar.edu.utn.frba.dds.domain.entities.HechoQuerys;
import ar.edu.utn.frba.dds.domain.entities.ListadoDeHechos;
import ar.edu.utn.frba.dds.domain.reportes.Solicitud;
import java.io.IOException;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServicioMetaMapa {
  private static ServicioMetaMapa instancia = null;
  //private static int maximaCantidadRegistrosDefault = 200;
  private final Retrofit retrofit;

  private ServicioMetaMapa(String urlApi) {
    this.retrofit = new Retrofit.Builder()
        .baseUrl(urlApi)
        .addConverterFactory(GsonConverterFactory.create())
        .build();
  }

  public static ServicioMetaMapa instancia(String urlApi) {
    if (instancia == null) {
      instancia = new ServicioMetaMapa(urlApi);
    }
    return instancia;
  }

  public ListadoDeHechos listadoDeHechos(HechoQuerys querys) throws IOException {
    MetaMapaService metaMapaService = this.retrofit.create(MetaMapaService.class);
    Call<ListadoDeHechos> requestListadoDeHechos = metaMapaService.hechos(
        querys.categoria,
        querys.ubicacion,
        querys.fecha_reporte_desde,
        querys.fecha_reporte_hasta,
        querys.fecha_acontecimiento_desde,
        querys.fecha_acontecimiento_hasta
    );
    Response<ListadoDeHechos> listadoDeHechosResponse = requestListadoDeHechos.execute(); //Ejecuto la request
    return listadoDeHechosResponse.body();
  }

  public ListadoDeHechos listadoDeHechosPorColeccion(int id, HechoQuerys querys) throws IOException {
    MetaMapaService metaMapaService = this.retrofit.create(MetaMapaService.class);
    Call<ListadoDeHechos> requestListadoDeHechos = metaMapaService.hechos(
        id,
        querys.categoria,
        querys.ubicacion,
        querys.fecha_reporte_desde,
        querys.fecha_reporte_hasta,
        querys.fecha_acontecimiento_desde,
        querys.fecha_acontecimiento_hasta
    );
    Response<ListadoDeHechos> listadoDeHechosResponse = requestListadoDeHechos.execute(); //Ejecuto la request
    return listadoDeHechosResponse.body();
  }

  public int enviarSolicitud(Solicitud solicitud) throws IOException {
    MetaMapaService metaMapaService = this.retrofit.create(MetaMapaService.class);
    Call<Solicitud> solicitudRequest = metaMapaService.crearSolicitud(solicitud);
    Response<Solicitud> solicitudResponse = solicitudRequest.execute();
    return solicitudResponse.code();
  }
}