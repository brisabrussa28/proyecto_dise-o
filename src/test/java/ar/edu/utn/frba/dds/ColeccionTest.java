package ar.edu.utn.frba.dds;

import ar.edu.utn.frba.dds.domain.coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.main.Administrador;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ColeccionTest {

  Administrador iluminati = new Administrador("△", "libellumcipher@incognito.com");
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
  public void coleccionCreadaCorrectamente() {
      Coleccion bonaerense = iluminati.crearColeccion("Robos", "Un día más siendo del conurbano", "Robos", fuenteAuxD);
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
}