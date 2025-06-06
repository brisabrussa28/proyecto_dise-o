package ar.edu.utn.frba.dds.domain.serviciometamapa;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.HechoQuerys;
import ar.edu.utn.frba.dds.domain.hecho.ListadoDeHechos;
import ar.edu.utn.frba.dds.domain.reportes.Solicitud;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServicioMetaMapa {
  private static ServicioMetaMapa instancia = null;
  private final Retrofit retrofit;

  private ServicioMetaMapa(String urlApi) {
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
        .create();

    this.retrofit = new Retrofit.Builder()
        .baseUrl(urlApi)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build();
  }

  public static ServicioMetaMapa instancia(String urlApi) {
    if (instancia == null) {
      instancia = new ServicioMetaMapa(urlApi);
    }
    return instancia;
  }

  public static void reiniciarInstancia(String urlApi) {
    instancia = new ServicioMetaMapa(urlApi);
  }

  public ListadoDeHechos listadoDeHechos(HechoQuerys querys) throws IOException {
    MetaMapaService metaMapaService = this.retrofit.create(MetaMapaService.class);
    Call<ListadoDeHechos> requestListadoDeHechos = metaMapaService.hechos(
        querys.getCategoria(),
        querys.getUbicacion(),
        querys.getFechaReporteDesde(),
        querys.getFechaReporteHasta(),
        querys.getFechaAcontecimientoDesde(),
        querys.getFechaAcontecimientoHasta()
    );
    Response<ListadoDeHechos> listadoDeHechosResponse = requestListadoDeHechos.execute();
    ListadoDeHechos listadoDeHechos = listadoDeHechosResponse.body();

    // Ensure the returned object is not null
    if (listadoDeHechos == null) {
      listadoDeHechos = new ListadoDeHechos();
    }

    // Safely iterate over the list using getHechos
    for (Hecho hecho : listadoDeHechos.getHechos()) {
      System.out.println("Hecho: " + hecho);
    }

    return listadoDeHechos;
  }

  public ListadoDeHechos listadoDeHechosPorColeccion(int id, HechoQuerys querys) throws IOException {
    MetaMapaService metaMapaService = this.retrofit.create(MetaMapaService.class);
    Call<ListadoDeHechos> requestListadoDeHechos = metaMapaService.hechos(
        id,
        querys.getCategoria(),
        querys.getUbicacion(),
        querys.getFechaReporteDesde(),
        querys.getFechaReporteHasta(),
        querys.getFechaAcontecimientoDesde(),
        querys.getFechaAcontecimientoHasta()
    );
    Response<ListadoDeHechos> listadoDeHechosResponse = requestListadoDeHechos.execute();
    ListadoDeHechos listadoDeHechos = listadoDeHechosResponse.body();

    // Ensure the returned object is not null
    if (listadoDeHechos == null) {
      listadoDeHechos = new ListadoDeHechos();
    }

    // Safely iterate over the list using getHechos
    for (Hecho hecho : listadoDeHechos.getHechos()) {
      System.out.println("Hecho: " + hecho);
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
