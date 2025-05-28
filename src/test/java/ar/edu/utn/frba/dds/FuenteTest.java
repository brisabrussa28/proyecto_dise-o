package ar.edu.utn.frba.dds;

import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import ar.edu.utn.frba.dds.domain.reportes.Solicitud;
import ar.edu.utn.frba.dds.domain.fuentes.ServicioDeAgregacion;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class FuenteTest {
  PuntoGeografico pgAux = new PuntoGeografico(33.39627891281455, 44.48695991794239);
  FuenteDinamica fuenteAuxD = new FuenteDinamica("Julio Cesar", null);
  LocalDateTime horaAux = LocalDateTime.of(2025, 5, 6, 20, 9);
  List<String> etiquetasAux = List.of(
      "#ancianita",
      "#robo_a_mano_armada",
      "#violencia",
      "#leyDeProtecciónALasAncianitas",
      "#NOalaVIOLENCIAcontraABUELITAS"
  );

  @Test
  public void fuenteDinamicaAgregaYObtieneHechos() {
    FuenteDinamica fuente = new FuenteDinamica("MiFuente", null);
    Hecho hecho = new Hecho("titulo", "desc", "Robos", "direccion", pgAux, horaAux, horaAux, Origen.CARGA_MANUAL, etiquetasAux);
    fuente.agregarHecho(hecho);
    assertTrue(fuente.obtenerHechos().contains(hecho));
  }

  @Test
  public void seEliminaUnHecho() {
    FuenteDinamica fuente = new FuenteDinamica("MiFuente", null);
    Hecho hecho = new Hecho("titulo", "desc", "Robos", "direccion", pgAux, horaAux, horaAux, Origen.CARGA_MANUAL, etiquetasAux);
    fuente.agregarHecho(hecho);
    Solicitud solicitud = new Solicitud(null, hecho.getId(), fuente, "a".repeat(600));
    fuente.eliminarHecho(hecho.getId());
    assertFalse(fuente.contiene(hecho));
  }

  @Test
  public void seAgregaHechoAFuente() {
    FuenteDinamica fuente = new FuenteDinamica("MiFuente", null);
    Hecho hecho = new Hecho("titulo", "desc", "Robos", "direccion", pgAux, horaAux, horaAux, Origen.CARGA_MANUAL, etiquetasAux);
    fuente.agregarHecho(hecho);
    assertTrue(fuente.contiene(hecho));
  }

  @Test
  public void seAgregaLaFuenteCorrectamente() {
    ServicioDeAgregacion servicio = new ServicioDeAgregacion("Juan");
    FuenteDinamica nuevaFuente = new FuenteDinamica("Juan", null);
    servicio.agregarFuente(nuevaFuente);
    assertTrue(servicio.getFuentesCargadas().contains(nuevaFuente));
  }
}