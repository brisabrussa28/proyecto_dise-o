package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ar.edu.utn.frba.dds.domain.coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.exceptions.EtiquetaInvalidaException;
import ar.edu.utn.frba.dds.domain.exceptions.RazonInvalidaException;
import ar.edu.utn.frba.dds.domain.exceptions.SolicitudInexistenteException;
import ar.edu.utn.frba.dds.domain.filtro.FiltroDeCategoria;
import ar.edu.utn.frba.dds.domain.filtro.FiltroDeDireccion;
import ar.edu.utn.frba.dds.domain.filtro.FiltroDeEtiqueta;
import ar.edu.utn.frba.dds.domain.filtro.FiltroDeFecha;
import ar.edu.utn.frba.dds.domain.filtro.FiltroDeFechaDeCarga;
import ar.edu.utn.frba.dds.domain.filtro.FiltroDeLugar;
import ar.edu.utn.frba.dds.domain.filtro.FiltroDeOrigen;
import ar.edu.utn.frba.dds.domain.filtro.FiltroDeTitulo;
import ar.edu.utn.frba.dds.domain.filtro.FiltroListaAnd;
import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import ar.edu.utn.frba.dds.domain.exceptions.ArchivoVacioException;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.reportes.GestorDeReportes;
import ar.edu.utn.frba.dds.domain.reportes.Solicitud;
import ar.edu.utn.frba.dds.main.Administrador;
import ar.edu.utn.frba.dds.main.Contribuyente;
import ar.edu.utn.frba.dds.main.Visualizador;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import ar.edu.utn.frba.dds.domain.filtro.Filtro;
import org.junit.jupiter.api.Test;


public class TPATest {
  //Recursos utilizados
  Contribuyente contribuyenteA = new Contribuyente(null, null);
  Contribuyente contribuyenteB = new Contribuyente("Roberto", "roberto@gmail.com");
  Visualizador visualizadorA = new Visualizador(null, null);
  List<String> etiquetasAux = List.of(
      "#ancianita",
      "#robo_a_mano_armada",
      "#violencia",
      "#leyDeProtecciónALasAncianitas",
      "#NOalaVIOLENCIAcontraABUELITAS"
  );
  PuntoGeografico pgAux = new PuntoGeografico(33.39627891281455, 44.48695991794239);
  Administrador iluminati = new Administrador("△", "libellumcipher@incognito.com");
  Administrador admin = new Administrador("pipocapo", "makenipipo@gmail.com");
  LocalDateTime horaAux = LocalDateTime.of(2025, 5, 6, 20, 9);
  Hecho hechoAux = new Hecho("Jorge", "Choreo", "ROBO", "Av 9 de Julio", pgAux, LocalDateTime.now(), LocalDateTime.now(), Origen.CARGA_MANUAL, etiquetasAux);
  List <Hecho> listaHechoAux = List.of(hechoAux);
  FuenteDinamica fuenteAuxD = new FuenteDinamica("Julio Cesar", null);


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
    Hecho hechoTest = contribuyenteA.crearHecho(
        "Robo",
        "Hombre blanco asalta ancianita indefensa",
        "ROBO",
        "Avenida Siempreviva 742",
        pgAux, //Ubicación
        LocalDate.now().atStartOfDay(), //Fecha
        etiquetasAux,  //Etiquetas
        fuenteAuxD); //Fuente

    boolean igual = (
        Objects.equals(hechoTest.getTitulo(), "Robo") &&
            Objects.equals(hechoTest.getDescripcion(), "Hombre blanco asalta ancianita indefensa") &&
            Objects.equals(hechoTest.getCategoria(), "ROBO") &&
            Objects.equals(hechoTest.getDireccion(), "Avenida Siempreviva 742") &&
            hechoTest.getUbicacion() == pgAux &&
            Objects.equals(hechoTest.getFechaSuceso(), LocalDate.now().atStartOfDay()) &&
            Objects.equals(hechoTest.getFechaCarga().toLocalDate(), LocalDate.now()) &&
            hechoTest.getEtiquetas().size() == etiquetasAux.size() &&
            hechoTest.getOrigen() == Origen.PROVISTO_CONTRIBUYENTE
    );
    assertTrue(igual); //Si "igual" es true es que estan correctos los datos
  }

  //TEST ADMINISTRADOR
  @Test
  public void importarCSV() {
    Fuente csv = admin.importardesdeCsv("src/main/java/ar/edu/utn/frba/dds/domain/csv/ejemplo.csv", ",", "bene");
    FiltroDeDireccion filtroDireccion = new FiltroDeDireccion("EL NESTORNAUTA");
    List<Hecho> hechosFiltrados = filtroDireccion.filtrar(csv.obtenerHechos());
    Hecho hecho = hechosFiltrados.get(0);

    boolean hechoCorrecto = Objects.equals(hecho.getCategoria(), "buenardo");
    boolean tiene5hechos = csv.obtenerHechos().size() == 5;
    assertTrue(tiene5hechos && hechoCorrecto);
  }

  //Se crea la colección correctamente
  @Test
  public void coleccionCreadaCorrectamente() {
    Coleccion bonaerense = iluminati.crearColeccion("Robos", "Un día más siendo del conurbano", "Robos", fuenteAuxD);
    boolean igual = (Objects.equals(bonaerense.getTitulo(), "Robos") &&
        Objects.equals(bonaerense.getDescripcion(), "Un día más siendo del conurbano") &&
        Objects.equals(bonaerense.getCategoria(), "Robos"));
    assertTrue(igual);
  }

  //TEST VISUALIZADOR
  public List<Hecho> crearColeccionHechoYDevolverlo() {
    contribuyenteA.crearHecho("titulo", "Un día más siendo del conurbano", "Robos", "dire", pgAux, horaAux, etiquetasAux, fuenteAuxD);
    Coleccion bonaerense = iluminati.crearColeccion("Robos", "Un día más siendo del conurbano", "Robos", fuenteAuxD);
    return visualizadorA.visualizarHechos(bonaerense);
  }

  @Test
  public void direccionIdentica() {
    Hecho hecho = new Hecho("titulo", "Un día más siendo del conurbano", "Robos", "dire", pgAux, horaAux, horaAux, null, etiquetasAux);
    assertFalse(hecho.sucedioEn("Mozart 2300"));
  }

  //Se visualiza correctamente
  @Test
  public void visualizarCorrectamente() {
    contribuyenteA.crearHecho("titulo", "Un día más siendo del conurbano", "Robos", "dire", pgAux, horaAux, etiquetasAux, fuenteAuxD);
    Coleccion bonaerense = iluminati.crearColeccion("Robos", "Un día más siendo del conurbano", "Robos", fuenteAuxD);
    List<Hecho> hechos = visualizadorA.visualizarHechos(bonaerense);
    assertFalse(hechos.isEmpty());
  }

  //Se filtra por categoria correctamente
  @Test
  public void filtraPorCategoriaCorrectamente() {
    List<Hecho> hechos = crearColeccionHechoYDevolverlo();
    FiltroDeCategoria filtroCategoria = new FiltroDeCategoria("Robos");
    assertFalse(filtroCategoria.filtrar(hechos).isEmpty());
  }

  //Se filtra por dirección correctamente
  @Test
  public void filtraPorDireccionCorrectamente() {
    List<Hecho> hechos = crearColeccionHechoYDevolverlo();
    FiltroDeDireccion filtroDireccion = new FiltroDeDireccion("Mozart 2300");
    assertTrue(filtroDireccion.filtrar(hechos).isEmpty());
  }

  //Se filtra por etiqueta correctamente
  @Test
  public void filtraPorEtiquetaCorrectamente() {
    List<Hecho> hechos = crearColeccionHechoYDevolverlo();
    FiltroDeEtiqueta filtroEtiqueta = new FiltroDeEtiqueta(etiquetasAux.get(0));
    assertFalse(filtroEtiqueta.filtrar(hechos).isEmpty());
  }

  //Se filtra por categoria correctamente
  @Test
  public void filtraPorFechaCorrectamente() {
    List<Hecho> hechos = crearColeccionHechoYDevolverlo();
    FiltroDeFecha filtroFecha = new FiltroDeFecha(horaAux);
    assertFalse(filtroFecha.filtrar(hechos).isEmpty());
  }

  //Se filtra por categoria correctamente
  @Test
  public void filtraPorFechaCargaCorrectamente() {
    List<Hecho> hechos = crearColeccionHechoYDevolverlo();
    FiltroDeFechaDeCarga filtroFecha = new FiltroDeFechaDeCarga(LocalDateTime.now());
    assertFalse(filtroFecha.filtrar(hechos).isEmpty());
  }

  //Se filtra por categoria correctamente
  @Test
  public void filtraPorLugarCorrectamente() {
    List<Hecho> hechos = crearColeccionHechoYDevolverlo();
    FiltroDeLugar filtroLugar = new FiltroDeLugar(pgAux);
    assertFalse(filtroLugar.filtrar(hechos).isEmpty());
  }

  //Se filtra por categoria correctamente
  @Test
  public void filtraPorOrigenCorrectamente() {
    List<Hecho> hechos = crearColeccionHechoYDevolverlo();
    FiltroDeOrigen filtroOrigen = new FiltroDeOrigen(Origen.CARGA_MANUAL);
    assertTrue(filtroOrigen.filtrar(hechos).isEmpty());
  }

  //Se filtra por categoria correctamente
  @Test
  public void filtraPorTituloCorrectamente() {
    List<Hecho> hechos = crearColeccionHechoYDevolverlo();
    FiltroDeTitulo filtroTitulo = new FiltroDeTitulo("titulo");
    assertFalse(filtroTitulo.filtrar(hechos).isEmpty());
  }

  //Se aplican varios filtros correctamente el GTA 6 no es clave
  @Test
  public void aplicaVariosFiltrosCorrectamente() {
    List<Hecho> hechos = crearColeccionHechoYDevolverlo();
    FiltroDeCategoria filtroCategoria = new FiltroDeCategoria("Robos");
    FiltroDeDireccion filtroDireccion = new FiltroDeDireccion("dire");
    FiltroDeEtiqueta filtroEtiqueta = new FiltroDeEtiqueta(etiquetasAux.get(0));
    List<Filtro> filtros = new ArrayList<>();
    filtros.add(filtroCategoria);
    filtros.add(filtroDireccion);
    filtros.add(filtroEtiqueta);
    FiltroListaAnd filtroListaAnd = new FiltroListaAnd(filtros);
    assertFalse(filtroListaAnd.filtrar(hechos).isEmpty());
  }

  @Test
  public void seImportaunaFuenteEstaticaCorrectamentePeroVacia() {
    assertThrows(ArchivoVacioException.class, () -> iluminati.importardesdeCsv("src/main/java/ar/edu/utn/frba/dds/domain/CSV/prueba2.csv", ",", "datos.gob.ar"));
  }

  @Test
  public void seImportaUnaFuenteEstaticaIncorrectamente() {
    assertThrows(RuntimeException.class, () -> iluminati.importardesdeCsv("src/main/java/ar/edu/utn/frba/dds/domain/CSV/ejemplo2.csv", ",", "datos.gob.ar"));
  }

  @Test
  public void visualizadorVeTodosLosHechosDeUnaColeccion() { // FANATICO DE CARLOS
    FuenteDinamica otraFuenteAux = new FuenteDinamica("Calos", listaHechoAux);
    Coleccion coleccionAux = new Coleccion("Pepito", otraFuenteAux, "Pedro", "ROBO");
    List<Hecho> listaHechos = visualizadorA.visualizarHechos(coleccionAux);
    assertFalse(listaHechos.isEmpty());
  }


  //Test Contribuyente
  @Test
  public void solicitarEliminacionDeHechoCorrectamente() {
    String motivo = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaabaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
    contribuyenteA.crearHecho("titulo", "Un día más siendo del conurbano", "Robos", "dire", pgAux, horaAux, etiquetasAux, fuenteAuxD);
    Coleccion bonaerense = iluminati.crearColeccion("Robos", "Un día más siendo del conurbano", "Robos", fuenteAuxD);
    contribuyenteA.solicitarEliminacion(bonaerense.getHechos().get(0), motivo, fuenteAuxD);
    assertEquals( 1, GestorDeReportes.getInstancia().cantidadSolicitudes());
  }

  @Test
  public void gestorDeReportesNoObtieneSolicitudes() {
    String motivo = "a".repeat(5); // motivo invalido
    contribuyenteA.crearHecho("titulo", "desc", "Robos", "direccion", pgAux, horaAux, etiquetasAux, fuenteAuxD);
    Coleccion coleccion = iluminati.crearColeccion("Robos", "desc", "Robos", fuenteAuxD);
    assertThrows(RazonInvalidaException.class, () -> contribuyenteA.solicitarEliminacion(coleccion.getHechos().get(0), motivo, fuenteAuxD));
  }

  @Test
  public void gestorDeReportesNoTieneSolicitud() {
    String motivo = "perú es clave".repeat(50);
    Solicitud soli = new Solicitud(contribuyenteA,hechoAux.getId(),fuenteAuxD,motivo);
    assertThrows(SolicitudInexistenteException.class, () -> GestorDeReportes.gestionarSolicitud(soli,true));
  }

  @Test
  public void coleccionContieneUnHecho() {
    Coleccion coleccion = iluminati.crearColeccion("Robos", "Descripcion", "Robos", fuenteAuxD);
    Hecho hecho = contribuyenteA.crearHecho("titulo", "desc", "Robos", "direccion", pgAux, horaAux, etiquetasAux, fuenteAuxD);
    fuenteAuxD.agregarHecho(hecho);
    assertTrue(coleccion.contieneA(hecho));
  }

  @Test
  public void coleccionEsDeCategoriaCorrectamente() {
    Coleccion coleccion = iluminati.crearColeccion("Robos", "Descripcion", "Robos", fuenteAuxD);
    assertEquals("Robos", coleccion.getCategoria());
    assertNotEquals(coleccion.getCategoria(), "Violencia");
  }

  @Test
  public void nombreColeccionNoEsNull() {
    Coleccion coleccion = iluminati.crearColeccion("Robos", "Descripcion", "Robos", fuenteAuxD);
    assertNotNull(coleccion.getTitulo());
  }


  // Test Fuente
  @Test
  public void fuenteDinamicaAgregaYObtieneHechos() {
    FuenteDinamica fuente = new FuenteDinamica("MiFuente", new ArrayList<>());
    Hecho hecho = contribuyenteA.crearHecho("titulo", "desc", "Robos", "direccion", pgAux, horaAux, etiquetasAux, fuenteAuxD);
    fuente.agregarHecho(hecho);
    assertTrue(fuente.obtenerHechos().contains(hecho));
  }

  @Test
  public void etiquetaInvalida() {
    assertThrows(EtiquetaInvalidaException.class, () -> new Hecho("XD",  "Choreo", "ROBO", "Av 9 de Julio", pgAux, LocalDateTime.now(), LocalDateTime.now(), Origen.CARGA_MANUAL, new ArrayList<>()));
  }
}

// test coleccion
