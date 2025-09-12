package ar.edu.utn.frba.dds.domain.fuentes.apis.serviciometamapa;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.reportes.Solicitud;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServicioMetaMapa {
  private final Retrofit retrofit;
  private final String urlApi; // Guardamos la URL para poder recuperarla.

  public ServicioMetaMapa(String urlApi) {
    this.urlApi = urlApi; // Se guarda la URL al construir el objeto.
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
        .create();

    this.retrofit = new Retrofit.Builder()
        .baseUrl(this.urlApi)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build();
  }

  // Getter para que el Adapter pueda obtener la URL.
  public String getUrlApi() {
    return urlApi;
  }

  public List<Hecho> listadoDeHechos(HechoQuerys querys) throws IOException {
    MetaMapaService metaMapaService = this.retrofit.create(MetaMapaService.class);
    Call<List<Hecho>> requestListadoDeHechos = metaMapaService.hechos(
        querys.getCategoria(),
        querys.getUbicacion()
              .toString(),
        querys.getFechaReporteDesde(),
        querys.getFechaReporteHasta(),
        querys.getFechaAcontecimientoDesde(),
        querys.getFechaAcontecimientoHasta()
    );
    Response<List<Hecho>> listadoDeHechosResponse = requestListadoDeHechos.execute();
    List<Hecho> listadoDeHechos = listadoDeHechosResponse.body();

    // Ensure the returned object is not null
    if (listadoDeHechos == null) {
      listadoDeHechos = new ArrayList<>();
    }

    return listadoDeHechos;
  }

  public List<Hecho> listadoDeHechosPorColeccion(int id, HechoQuerys querys) throws IOException {
    MetaMapaService metaMapaService = this.retrofit.create(MetaMapaService.class);
    Call<List<Hecho>> requestListadoDeHechos = metaMapaService.hechos(
        id,
        querys.getCategoria(),
        querys.getUbicacion()
              .toString(),
        querys.getFechaReporteDesde(),
        querys.getFechaReporteHasta(),
        querys.getFechaAcontecimientoDesde(),
        querys.getFechaAcontecimientoHasta()
    );
    Response<List<Hecho>> listadoDeHechosResponse = requestListadoDeHechos.execute();
    List<Hecho> listadoDeHechos = listadoDeHechosResponse.body();

    if (listadoDeHechos == null) {
      listadoDeHechos = new ArrayList<>();
    }

    return listadoDeHechos;
  }

  public int enviarSolicitud(Solicitud solicitud) throws IOException {
    MetaMapaService metaMapaService = this.retrofit.create(MetaMapaService.class);
    Call<Void> request = metaMapaService.crearSolicitud(solicitud);
    Response<Void> response = request.execute();
    return response.code(); // ⬅ Devuelve el código HTTP
  }

  // Adaptador para serializar/deserializar LocalDateTime con Gson
  static class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public void write(JsonWriter out, LocalDateTime value) throws IOException {
      if (value != null) {
        out.value(value.format(formatter));
      } else {
        out.nullValue();
      }
    }

    @Override
    public LocalDateTime read(JsonReader in) throws IOException {
      String str = in.nextString();
      return (str != null && !str.isEmpty()) ? LocalDateTime.parse(str, formatter) : null;
    }
  }
}
