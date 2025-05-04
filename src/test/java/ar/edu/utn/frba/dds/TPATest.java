package ar.edu.utn.frba.dds;

import ar.edu.utn.frba.dds.domain.Etiqueta;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
  FuenteDinamica fuenteAuxD = new FuenteDinamica("Julio Cesar", null);
  List<Etiqueta> etiquetasAux = List.of(
      new Etiqueta("#ancianita"),
      new Etiqueta("#robo_a_mano_armada"),
      new Etiqueta("#violencia"),
      new Etiqueta("#leyDeProtecciónALasAncianitas"),
      new Etiqueta("#NOalaVIOLENCIAcontraABUELITAS")
  );
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
        Objects.equals(hechoTest.getFechaSuceso(), LocalDate.now().atStartOfDay()) &&
        Objects.equals(hechoTest.getFechaCarga(), LocalDate.now().atStartOfDay()) &&
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
    Coleccion bonaerense = iluminati.crearColeccion("Robos", "Un día más siendo del conurbano", "Robos");
    boolean igual = (Objects.equals(bonaerense.getTitulo(), "Robos") &&
        Objects.equals(bonaerense.getDescripcion(), "Un día más siendo del conurbano") &&
        Objects.equals(bonaerense.getCategoria(), "Robos"));
    assertTrue(igual);
  }

  //Se importan hechos correctamente
  @Test
  public void testImportarDesdeCSV() {
    Administrador admin = new Administrador("Admin", "admin@mail.com");

    // Ruta relativa al proyecto
    String rutaCSV = "src/test/resources/testHechos.csv";

    List<Hecho> hechos = admin.importarDesdeCSV(rutaCSV);

    assertEquals(3, hechos.size());

    Hecho primerHecho = hechos.get(0);
    assertEquals("Inundación en zona sur", primerHecho.getTitulo());
    assertEquals("Calles anegadas por lluvias", primerHecho.getDescripcion());
    assertEquals("Clima", primerHecho.getCategoria());
    assertEquals("Av. Siempreviva 123", primerHecho.getDireccion());
    assertEquals(-34.6037, primerHecho.getUbicacion().getLatitud());
    assertEquals(-58.3816, primerHecho.getUbicacion().getLongitud());
    assertEquals(LocalDateTime.of(2024, 5, 1, 14, 30), primerHecho.getFechaSuceso());
    assertEquals(2, primerHecho.getEtiquetas().size());

    Hecho segundoHecho = hechos.get(1);
    assertEquals("Corte de luz masivo", segundoHecho.getTitulo());
    assertEquals("Sin servicio eléctrico en varios barrios", segundoHecho.getDescripcion());
    assertEquals("Infraestructura", segundoHecho.getCategoria());
    assertEquals("Calle Falsa 456", segundoHecho.getDireccion());
    assertEquals(-34.6090, segundoHecho.getUbicacion().getLatitud());
    assertEquals(-58.3923, segundoHecho.getUbicacion().getLongitud());
    assertEquals(LocalDateTime.of(2024, 5, 2, 9, 0), segundoHecho.getFechaSuceso());
    assertEquals(3, segundoHecho.getEtiquetas().size());

    Hecho tercerHecho = hechos.get(2);
    assertEquals("Protesta estudiantil", tercerHecho.getTitulo());
    assertEquals("Estudiantes reclaman mejoras edilicias", tercerHecho.getDescripcion());
    assertEquals("Social", tercerHecho.getCategoria());
    assertEquals("Av. de Mayo 789", tercerHecho.getDireccion());
    assertEquals(-34.6071, tercerHecho.getUbicacion().getLatitud());
    assertEquals(-58.3802, tercerHecho.getUbicacion().getLongitud());
    assertEquals(LocalDateTime.of(2024, 5, 3, 17, 45), tercerHecho.getFechaSuceso());
    assertEquals(2, tercerHecho.getEtiquetas().size());
  }

  //Se procesa la solicitud correctamente

  //TEST VISUALIZADOR
  //Se visualiza correctamente
  //Se filtra por etiqueta correctamente
  //Se filtra por categoria correctamente
}
