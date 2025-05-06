package ar.edu.utn.frba.dds;

import ar.edu.utn.frba.dds.domain.info.Etiqueta;
import ar.edu.utn.frba.dds.domain.CSV.MapeoCSV;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import ar.edu.utn.frba.dds.domain.Coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.fuentes.*;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
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
        Objects.equals(hechoTest.getDireccion(), "Avenida Siempreviva 742") &&
        hechoTest.getUbicacion() == pgAux &&
        Objects.equals(hechoTest.getFechaSuceso(), LocalDate.now().atStartOfDay()) &&
        Objects.equals(hechoTest.getFechaCarga(), LocalDate.now().atStartOfDay()) &&
        hechoTest.getEtiquetas() == etiquetasAux &&
        hechoTest.getOrigen().equals(fuenteAuxD.getNombre()));
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

  //Se importan hechos correctamente Incidentes Vehiculares
  @Test
  public void testImportarDesdeCSVIncidentesVehicular() {
    Administrador admin = new Administrador("Admin", "admin@mail.com");
    String rutaCSV = "src/test/resources/SAT-MV-BU_2017-2023.csv";
    String separador = ";";
    String nombreFuente = "Fuente de Prueba";

    //Definicion de Mapeo
    MapeoCSV mapeo = new MapeoCSV();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy HH:mm:ss");

    mapeo.obtenerTitulo = fila -> "Hecho " + fila.get("id_hecho");
    mapeo.obtenerDescripcion = fila -> "Tipo de persona: " + fila.get("tipo_persona") + ". " +
                                                        "Tipo de lugar: " + fila.get("tipo_lugar") + ". " +
                                                        "Calle interseccion: " + fila.get("calle_interseccion_nombre") + ". " +
                                                        "Estado de semaforo: " + fila.get("semaforo_estado") + ". " +
                                                        "Modo de produccion del Hecho: " + fila.get("modo_produccion_hecho_ampliada") + ". " + fila.get("modo_produccion_hecho_otro") + ". " +
                                                        "Condicion del Clima: " + fila.get("clima_condicion") + ". " + fila.get("clima_otro") + ". " +
                                                        "Motivo origen de registro: " + fila.get("motivo_origen_registro") + ". " + fila.get("motivo_origen_registro_otro") + ". " +
                                                        "Vehiculo de la victima: " + fila.get("victima_vehiculo_ampliado") + ". " +
                                                        "Identidad de genero de la Victima: " + fila.get("victima_identidad_genero") + ". " +
                                                        "Rango de edad de la Victima: : " + fila.get("victima_tr_edad") + ". " +
                                                        "Vehiculo del inculpado: " + fila.get("inculpado_vehiculo") + ". " +
                                                        "Identidad de genero del inculpado: : " + fila.get("inculpado_identidad_genero") + ". " +
                                                        "Rango de edad del inculpado: : " + fila.get("inculpado_tr_edad") + ". ";
    mapeo.obtenerDireccion = fila -> fila.get("calle_nombre") + " " + fila.get("calle_altura") + ", " + fila.get("localidad_nombre") + ", " + fila.get("departamento_nombre") + ", " + fila.get("provincia_nombre");
    mapeo.obtenerFecha = fila -> {
      String fecha = fila.get("fecha_hecho").trim();
      String hora = fila.get("hora_hecho").trim();
      try {
        return LocalDateTime.parse(fecha + " " + hora, formatter);
      } catch (DateTimeParseException e) {
        throw new RuntimeException("Fecha inválida: " + fila.get("fecha_hora"));
      }
    };
    mapeo.obtenerUbicacion = fila -> new PuntoGeografico(
        Double.parseDouble(fila.get("latitud")),
        Double.parseDouble(fila.get("longitud"))
    );
    mapeo.obtenerEtiquetas = fila -> {
      List<Etiqueta> etiquetas = new ArrayList<>();

      // valores simples
      Stream.of(
              fila.get("tipo_persona"),
              fila.get("provincia_"),
              fila.get("localidad_nombre"),
              fila.get("semaforo_estado"),
              fila.get("tipo_lugar")
          ).filter(v -> v != null && !v.isBlank())
          .forEach(v -> etiquetas.add(new Etiqueta(v)));

      // Agregar (si no es "No corresponde")
      String victimaClase = fila.get("victima_clase");
      if (victimaClase != null && !victimaClase.isBlank() && !victimaClase.equalsIgnoreCase("No corresponde")) {
        etiquetas.add(new Etiqueta("victima " + victimaClase));
      }

      // Agregar (si no es "No corresponde")
      String victimaVehiculo = fila.get("victima_vehiculo_ampliado");
      if (victimaVehiculo != null && !victimaVehiculo.isBlank() && !victimaVehiculo.equalsIgnoreCase("No corresponde")) {
        etiquetas.add(new Etiqueta(victimaVehiculo));
      }

      // Agregar (si no es "No corresponde")
      String inculpadoVehiculo = fila.get("inculpado_vehiculo_ampliado");
      if (inculpadoVehiculo != null && !inculpadoVehiculo.isBlank() && !inculpadoVehiculo.equalsIgnoreCase("No corresponde")) {
        etiquetas.add(new Etiqueta(inculpadoVehiculo));
      }

      return etiquetas;

    };


    FuenteEstatica fuente = admin.importarDesdeCSV(rutaCSV, mapeo, separador, nombreFuente);
    List<Hecho> hechos = fuente.obtenerHechos();
    assertEquals(52027, hechos.size());

    Hecho primerHecho = hechos.get(0);
    assertEquals("Hecho 15448", primerHecho.getTitulo());
    assertEquals("Tipo de persona: Imputado. Tipo de lugar: Ruta Provincial. Calle interseccion: EX PEAJE. Estado de semaforo: Sin semáforo. Modo de produccion del Hecho: Colisión vehículo-vehículo. . Condicion del Clima: Bueno. . Motivo origen de registro: Intervención policial. . Vehiculo de la victima: -----. Identidad de genero de la Victima: No corresponde. Rango de edad de la Victima: : No corresponde. Vehiculo del inculpado: Camioneta. Identidad de genero del inculpado: : Varón. Rango de edad del inculpado: : 25-29.", primerHecho.getDescripcion());
    assertEquals("accidentes vehiculares", primerHecho.getCategoria());
    assertEquals("RUTA PROVINCIAL 7 , Neuquén, Confluencia, Neuquén", primerHecho.getDireccion());
    assertEquals(-39, primerHecho.getUbicacion().getLatitud());
    assertEquals(-68, primerHecho.getUbicacion().getLongitud());
    assertEquals(LocalDateTime.of(2017, 1, 5, 8, 30), primerHecho.getFechaSuceso());
    assertEquals(5, primerHecho.getEtiquetas().size());

    Hecho segundoHecho = hechos.get(1);
    assertEquals("Hecho 13161", segundoHecho.getTitulo());
    assertEquals("Tipo de persona: Víctima. Tipo de lugar: Ruta Provincial. Calle interseccion: EX PEAJE. Estado de semaforo: Sin semáforo. Modo de produccion del Hecho: Colisión vehículo-vehículo. . Condicion del Clima: Bueno. . Motivo origen de registro: Intervención policial. . Vehiculo de la victima: Camioneta. Identidad de genero de la Victima: Varón. Rango de edad de la Victima: : 30-34. Vehiculo del inculpado: No corresponde. Identidad de genero del inculpado: : No corresponde. Rango de edad del inculpado: : No corresponde.", segundoHecho.getDescripcion());
    assertEquals("accidentes vehiculares", segundoHecho.getCategoria());
    assertEquals("RUTA PROVINCIAL 7 , Neuquén, Confluencia, Neuquén", segundoHecho.getDireccion());
    assertEquals(-39, segundoHecho.getUbicacion().getLatitud());
    assertEquals(-68, segundoHecho.getUbicacion().getLongitud());
    assertEquals(LocalDateTime.of(2017, 1, 5, 8, 30), segundoHecho.getFechaSuceso());
    assertEquals(6, segundoHecho.getEtiquetas().size());

    Hecho tercerHecho = hechos.get(2);
    assertEquals("Hecho 13161", tercerHecho.getTitulo());
    assertEquals("Tipo de persona: Imputado. Tipo de lugar: Calle. Calle interseccion: REPUBLICA DE ITALIA. Estado de semaforo: Sin semáforo. Modo de produccion del Hecho: Colisión vehículo-vehículo. . Condicion del Clima: Bueno. . Motivo origen de registro: Intervención policial. . Vehiculo de la victima: -----. Identidad de genero de la Victima: No corresponde. Rango de edad de la Victima: : No corresponde. Vehiculo del inculpado: Automóvil. Identidad de genero del inculpado: : Mujer. Rango de edad del inculpado: : 30-34.", tercerHecho.getDescripcion());
    assertEquals("accidentes vehiculares", tercerHecho.getCategoria());
    assertEquals("PICUN LEUFU , Neuquén, Confluencia, Neuquén", tercerHecho.getDireccion());
    assertEquals(-34.6071, tercerHecho.getUbicacion().getLatitud());
    assertEquals(-58.3802, tercerHecho.getUbicacion().getLongitud());
    assertEquals(LocalDateTime.of(2017, 1, 27, 19, 37), tercerHecho.getFechaSuceso());
    assertEquals(5, tercerHecho.getEtiquetas().size());
  }

  //Se procesa la solicitud correctamente

  //TEST VISUALIZADOR
  //Se visualiza correctamente
  //Se filtra por etiqueta correctamente
  //Se filtra por categoria correctamente
}
