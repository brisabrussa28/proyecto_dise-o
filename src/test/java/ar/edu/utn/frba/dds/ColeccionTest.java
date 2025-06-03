package ar.edu.utn.frba.dds;

import ar.edu.utn.frba.dds.domain.coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import ar.edu.utn.frba.dds.domain.reportes.GestorDeReportes;
import ar.edu.utn.frba.dds.main.Usuario;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ColeccionTest {

  Usuario iluminati = new Usuario("△", "libellumcipher@incognito.com");
  PuntoGeografico pgAux = new PuntoGeografico(33.39627891281455, 44.48695991794239);
  FuenteDinamica fuenteAuxD = new FuenteDinamica("Julio Cesar", null);
  Date horaAux = Date.from(LocalDateTime.of(2025, 5, 6, 20, 9)
      .atZone(ZoneId.systemDefault())
      .toInstant());
  List<String> etiquetasAux = List.of(
      "#ancianita",
      "#robo_a_mano_armada",
      "#violencia",
      "#leyDeProtecciónALasAncianitas",
      "#NOalaVIOLENCIAcontraABUELITAS"
  );

  @Test
  public void coleccionCreadaCorrectamente() {
    Coleccion bonaerense = iluminati.crearColeccion(
        "Robos",
        "Un día más siendo del conurbano",
        "Robos",
        fuenteAuxD
    );

    boolean igual = bonaerense.getTitulo().equals("Robos") &&
        bonaerense.getDescripcion().equals("Un día más siendo del conurbano") &&
        bonaerense.getCategoria().equals("Robos");

    assertTrue(igual);
  }

  @Test
  public void coleccionContieneUnHecho() {
    Coleccion coleccion = iluminati.crearColeccion("Robos", "Descripcion", "Robos", fuenteAuxD);
    Hecho hecho = new Hecho("titulo", "desc", "Robos", "direccion", null, horaAux, horaAux, null, etiquetasAux);
    fuenteAuxD.agregarHecho(hecho);
    assertTrue(coleccion.contieneA(hecho));
  }

  @Test
  public void coleccionEsDeCategoriaCorrectamente() {
    Coleccion coleccion = iluminati.crearColeccion("Robos", "Descripcion", "Robos", fuenteAuxD);
    assertEquals("Robos", coleccion.getCategoria());
    assertNotEquals("Violencia", coleccion.getCategoria());
  }

  @Test
  public void nombreColeccionNoEsNull() {
    Coleccion coleccion = iluminati.crearColeccion("Robos", "Descripcion", "Robos", fuenteAuxD);
    assertNotNull(coleccion.getTitulo());
  }

  @Test
  public void coleccionYaNoContieneHechoEliminadoPorGestor() {
    Coleccion coleccion = iluminati.crearColeccion("Robos", "Descripcion", "Robos", fuenteAuxD);
    Hecho hecho = new Hecho("titulo", "desc", "Robos", "direccion", pgAux, horaAux, horaAux, Origen.CARGA_MANUAL, etiquetasAux);
    fuenteAuxD.agregarHecho(hecho);
    GestorDeReportes.getInstancia().eliminarHecho(hecho);
    assertFalse(coleccion.contieneA(hecho));
  }

  @Test
  public void coleccionContieneFuenteCorrecta() {
    Coleccion coleccion = iluminati.crearColeccion("Robos", "Descripcion", "Robos", fuenteAuxD);
    assertTrue(coleccion.contieneFuente(fuenteAuxD));
  }
}
