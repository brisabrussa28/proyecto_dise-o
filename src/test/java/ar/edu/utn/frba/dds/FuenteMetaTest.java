package ar.edu.utn.frba.dds;

import ar.edu.utn.frba.dds.domain.entities.HechoQuerys;
import ar.edu.utn.frba.dds.domain.entities.ListadoDeHechos;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import ar.edu.utn.frba.dds.domain.reportes.Solicitud;
import ar.edu.utn.frba.dds.domain.services.ServicioMetaMapa;
import ar.edu.utn.frba.dds.main.Usuario;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FuenteMetaTest {
  HechoQuerys filtros = new HechoQuerys("desastres", null, null, null, null, null);

  @Test
  public void obtenerHechos() throws IOException {
    ServicioMetaMapa servicioMetaMapa = ServicioMetaMapa.instancia("https://api-ddsi.disilab.ar/public/api/");
    ListadoDeHechos listadoDeHechos = servicioMetaMapa.listadoDeHechos(filtros);

    for (Hecho unHecho : listadoDeHechos.hechos) {
      System.out.println(unHecho.getId());
    }

  }

  @Test
  public void obtenerHechosDeColeccionUno() throws IOException {
    ServicioMetaMapa servicioMetaMapa = ServicioMetaMapa.instancia("https://api-ddsi.disilab.ar/public/api/");
    ListadoDeHechos listadoDeHechosPorColeccion = servicioMetaMapa.listadoDeHechosPorColeccion(1, filtros);
    Assertions.assertNotNull(listadoDeHechosPorColeccion);
  }

  @Test
  public void seCreaSolicitud() throws IOException {
    PuntoGeografico pgAux = new PuntoGeografico(33.39627891281455, 44.48695991794239);
    ServicioMetaMapa servicioMetaMapa = ServicioMetaMapa.instancia("https://api-ddsi.disilab.ar/public/api/");
    String motivo = "a".repeat(600);
    List<String> etiquetasAux = List.of(
        "#ancianita",
        "#robo_a_mano_armada",
        "#violencia",
        "#leyDeProtecciónALasAncianitas",
        "#NOalaVIOLENCIAcontraABUELITAS"
    );
    Hecho hecho = new Hecho("titulo", "desc", "Robos", "direccion", pgAux, LocalDateTime.now(), LocalDateTime.now(), Origen.PROVISTO_CONTRIBUYENTE, etiquetasAux);
    FuenteDinamica fuente = new FuenteDinamica("MiFuente", null);
    fuente.agregarHecho(hecho);

    Solicitud solicitud = new Solicitud(new Usuario("Niko Bellic", ""), hecho, motivo);
    int codigo = servicioMetaMapa.enviarSolicitud(solicitud);
    Assertions.assertEquals(201, codigo);
  }
}
