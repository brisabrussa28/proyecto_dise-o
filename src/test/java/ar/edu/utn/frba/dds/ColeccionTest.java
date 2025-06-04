package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.domain.coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.detectorSpam.DetectorSpam;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import ar.edu.utn.frba.dds.domain.reportes.GestorDeReportes;
import ar.edu.utn.frba.dds.domain.rol.Rol;
import ar.edu.utn.frba.dds.main.Usuario;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ColeccionTest {
  Usuario iluminati = new Usuario("△", "libellumcipher@incognito.com", Set.of(Rol.ADMINISTRADOR, Rol.CONTRIBUYENTE));
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
  private GestorDeReportes gestor;
  private DetectorSpam detectorSpam;

  @BeforeEach
  void initFileSystem() {
    detectorSpam = mock(DetectorSpam.class);
    gestor = new GestorDeReportes(detectorSpam);
  }


  @Test
  public void coleccionCreadaCorrectamente() {
    Coleccion bonaerense = iluminati.crearColeccion(
        "Robos",
        "Un día más siendo del conurbano",
        "Robos",
        fuenteAuxD
    );

    boolean igual = bonaerense.getTitulo()
                              .equals("Robos") &&
        bonaerense.getDescripcion()
                  .equals("Un día más siendo del conurbano") &&
        bonaerense.getCategoria()
                  .equals("Robos");

    assertTrue(igual);
  }

  @Test
  public void coleccionContieneUnHecho() {
    Coleccion coleccion = iluminati.crearColeccion("Robos", "Descripcion", "Robos", fuenteAuxD);
    Hecho hecho = new Hecho(
        "titulo",
        "desc",
        "Robos",
        "direccion",
        null,
        horaAux,
        horaAux,
        null,
        etiquetasAux
    );
    fuenteAuxD.agregarHecho(hecho);
    assertTrue(coleccion.contieneA(hecho, gestor));
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
    Hecho hecho = new Hecho(
        "titulo",
        "desc",
        "Robos",
        "direccion",
        pgAux,
        horaAux,
        horaAux,
        Origen.PROVISTO_CONTRIBUYENTE,
        etiquetasAux
    );
    fuenteAuxD.agregarHecho(hecho);
    when(detectorSpam.esSpam(anyString())).thenReturn(false);
    gestor.marcarComoEliminado(hecho);
    assertFalse(coleccion.contieneA(hecho, gestor));
  }

  @Test
  public void coleccionContieneFuenteCorrecta() {
    Coleccion coleccion = iluminati.crearColeccion("Robos", "Descripcion", "Robos", fuenteAuxD);
    assertTrue(coleccion.contieneFuente(fuenteAuxD));
  }
}
