package ar.edu.utn.frba.dds;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import ar.edu.utn.frba.dds.domain.Coleccion;
import ar.edu.utn.frba.dds.domain.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.Hecho;
import ar.edu.utn.frba.dds.domain.PuntoGeografico;
import ar.edu.utn.frba.dds.main.Administrador;
import ar.edu.utn.frba.dds.main.Contribuyente;


public class TPATest {
  //Recursos utilizados
  Contribuyente contribuyenteA = new Contribuyente(null, null);
  Contribuyente contribuyenteB = new Contribuyente("Roberto", "roberto@gmail.com");
  FuenteDinamica fuenteAuxD = new FuenteDinamica("Julio Cesar");
  List<String> etiquetasAux = List.of("#ancianita", "#robo_a_mano_armada", "#violencia", "#leyDeProtecciónALasAncianitas", "#NOalaVIOLENCIAcontraABUELITAS");
  PuntoGeografico pgAux = new PuntoGeografico(33.39627891281455, 44.48695991794239);
  Administrador iluminati = new Administrador("△", "libellumcipher@incognito.com");

  //TEST CONTRIBUYENTE
  //Se determina la identidad (anonimo/registrado) correctamente
  @Test
  public void identidadDelContribuyenteAEsAnonima() {
    assertTrue(contribuyenteA.esAnonimo());
  }
  @Test
  public void identidadDelContribuyenteBEsRegistrado() {
    assertFalse(contribuyenteB.esAnonimo());
  }
  //Se crea un hecho correctamente


  @Test
  public void hechoCreadoCorrectamente() {
    Hecho hechoTest = contribuyenteA.crearHecho( "Robo",
        "Hombre blanco asalta ancianita indefensa",
        "ROBO",
        "Avenida Siempreviva 742",
        pgAux,
        LocalDate.now().atStartOfDay(),
        etiquetasAux,
        fuenteAuxD);

    boolean igual = (Objects.equals(hechoTest.getTitulo(), "Robo") &&
        Objects.equals(hechoTest.getDescripcion(), "Hombre blanco asalta ancianita indefensa") &&
        Objects.equals(hechoTest.getCategoria(), "ROBO") &&
        Objects.equals(hechoTest.getDirecccion(), "Avenida Siempreviva 742") &&
        hechoTest.getUbicacion() == pgAux &&
        Objects.equals(hechoTest.getFecha(), LocalDate.now().atStartOfDay()) &&
        hechoTest.getEtiquetas() == etiquetasAux &&
        hechoTest.getOrigen() == fuenteAuxD);
    assertTrue(igual); //Si "igual" es true es que estan correctos los datos
  }
  //Se confecciona la solicitud correctamente
  /*
  @Test
  public void solicitudCreadaCorrectamente() {

    assertFalse(contribuyenteB.esAnonimo());
  }*/

  //TEST ADMINISTRADOR
  //Se crea la colección correctamente
  @Test
  public void coleccionCreadaCorrectamente() {
    Coleccion bonaerense = iluminati.crearColeccion("Robos", "Un día más siendo del conurbano");
    boolean igual = (Objects.equals(bonaerense.getTitulo(), "Robos") && Objects.equals(bonaerense.getDescripcion(), "Un día más siendo del conurbano"));
    assertTrue(igual);
  }
  //Se importan hechos correctamente
  //Se procesa la solicitud correctamente

  //TEST VISUALIZADOR
  //Se visualiza correctamente
  //Se filtra por etiqueta correctamente
  //Se filtra por categoria correctamente
}
