package ar.edu.utn.frba.dds;


import static org.junit.jupiter.api.Assertions.assertFalse;

import ar.edu.utn.frba.dds.domain.coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.detectorSpam.DetectorSpam;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.reportes.GestorDeReportes;
import ar.edu.utn.frba.dds.main.Usuario;
import ar.edu.utn.frba.dds.main.Usuario;
import ar.edu.utn.frba.dds.main.Visualizador;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;


public class TPATest {
  //Recursos utilizados
  Usuario contribuyenteA = new Usuario(null, null);
  Usuario contribuyenteB = new Usuario("Roberto", "roberto@gmail.com");
  Visualizador visualizadorA = new Visualizador(null, null);
  List<String> etiquetasAux = List.of(
      "#ancianita",
      "#robo_a_mano_armada",
      "#violencia",
      "#leyDeProtecciónALasAncianitas",
      "#NOalaVIOLENCIAcontraABUELITAS"
  );
  PuntoGeografico pgAux = new PuntoGeografico(33.39627891281455, 44.48695991794239);
  Usuario iluminati = new Usuario("△", "libellumcipher@incognito.com");
  Usuario admin = new Usuario("pipocapo", "makenipipo@gmail.com");
  Date horaAux = Date.from(LocalDateTime.of(2025, 5, 6, 20, 9)
      .atZone(ZoneId.systemDefault())
      .toInstant());
  Hecho hechoAux = new Hecho("Jorge", "Choreo", "ROBO", "Av 9 de Julio", pgAux, Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()), Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()), Origen.CARGA_MANUAL, etiquetasAux);
  List <Hecho> listaHechoAux = List.of(hechoAux);
  FuenteDinamica fuenteAuxD = new FuenteDinamica("Julio Cesar", null);
  private DetectorSpam detectorSpam;
  private GestorDeReportes gestor = new GestorDeReportes(detectorSpam);



  //Se visualiza correctamente
  @Test
  public void visualizarCorrectamente() {

    contribuyenteA.crearHecho("titulo", "Un día más siendo del conurbano", "Robos", "dire", pgAux, horaAux, etiquetasAux, fuenteAuxD);
    Coleccion bonaerense = iluminati.crearColeccion("Robos", "Un día más siendo del conurbano", "Robos", fuenteAuxD);
    List<Hecho> hechos = visualizadorA.visualizarHechos(bonaerense,gestor);
    assertFalse(hechos.isEmpty());
  }



  @Test
  public void visualizadorVeTodosLosHechosDeUnaColeccion() { // FANATICO DE CARLOS
    FuenteDinamica otraFuenteAux = new FuenteDinamica("Calos", listaHechoAux);
    Coleccion coleccionAux = new Coleccion("Pepito", otraFuenteAux, "Pedro", "ROBO");
    List<Hecho> listaHechos = visualizadorA.visualizarHechos(coleccionAux,gestor);
    assertFalse(listaHechos.isEmpty());
  }

}

// test coleccion
