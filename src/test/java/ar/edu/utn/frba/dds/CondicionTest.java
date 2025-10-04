package ar.edu.utn.frba.dds;

import static ar.edu.utn.frba.dds.domain.filtro.condiciones.Operador.IGUAL;
import static ar.edu.utn.frba.dds.domain.filtro.condiciones.Operador.MAYOR_QUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ar.edu.utn.frba.dds.domain.filtro.condiciones.CondicionAnd;
import ar.edu.utn.frba.dds.domain.filtro.condiciones.CondicionGenerica;
import ar.edu.utn.frba.dds.domain.filtro.condiciones.CondicionNot;
import ar.edu.utn.frba.dds.domain.filtro.condiciones.CondicionOr;
import ar.edu.utn.frba.dds.domain.filtro.condiciones.CondicionPredicado;
import ar.edu.utn.frba.dds.domain.filtro.condiciones.Operador;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.Origen;
import ar.edu.utn.frba.dds.domain.hecho.etiqueta.Etiqueta;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class CondicionTest {

  private Hecho hechoRobo;
  private Hecho hechoPrueba;
  private LocalDateTime fechaSuceso;

  @BeforeEach
  void setUp() {
    fechaSuceso = LocalDateTime.now()
                               .minusDays(1);
    hechoRobo = new Hecho(
        "Prueba Robo", "Descripción Robo", "Robos", "123", "PBA",
        new PuntoGeografico(12.3, 12.3), fechaSuceso, LocalDateTime.now(),
        Origen.PROVISTO_CONTRIBUYENTE, List.of(new Etiqueta("#ROBO"))
    );
    hechoPrueba = new Hecho(
        "Prueba General", "Descripción Prueba", "Pruebas", "direccion", "PBA",
        new PuntoGeografico(12.3, 12.3), fechaSuceso, LocalDateTime.now(),
        Origen.PROVISTO_CONTRIBUYENTE, List.of(new Etiqueta("#PRUEBA"))
    );
  }

  @Nested
  @DisplayName("Tests para CondicionGenerica")
  class CondicionGenericaTest {

    @Test
    void evaluaCorrectamenteIgualdad() {
      CondicionGenerica condicion = new CondicionGenerica("categoria", IGUAL, "Robos");
      assertTrue(condicion.evaluar(hechoRobo));
      assertFalse(condicion.evaluar(hechoPrueba));
    }

    @Test
    void evaluaCorrectamenteFechas() {
      CondicionGenerica condicion = new CondicionGenerica("fechasuceso", IGUAL, fechaSuceso);
      assertTrue(condicion.evaluar(hechoRobo));
    }

    @Test
    void evaluaMayorQueParaFechas() {
      CondicionGenerica condicion = new CondicionGenerica("fechasuceso", MAYOR_QUE, fechaSuceso.minusDays(1));
      assertTrue(condicion.evaluar(hechoRobo));
    }

  }

  @Nested
  @DisplayName("Tests para CondicionCompuesta (AND y OR)")
  class CondicionCompuestaTest {

    @Test
    void condicionAndEvaluaTrueCuandoTodasSonTrue() {
      CondicionAnd condicionAnd = new CondicionAnd();
      condicionAnd.agregarCondicion(new CondicionGenerica("provincia", IGUAL, "PBA"));
      condicionAnd.agregarCondicion(new CondicionGenerica("categoria", IGUAL, "Robos"));
      assertTrue(condicionAnd.evaluar(hechoRobo));
    }

    @Test
    void condicionAndEvaluaFalseSiUnaEsFalse() {
      CondicionAnd condicionAnd = new CondicionAnd();
      condicionAnd.agregarCondicion(new CondicionGenerica("provincia", IGUAL, "PBA"));
      condicionAnd.agregarCondicion(new CondicionGenerica("categoria", IGUAL, "Homicidio"));
      assertFalse(condicionAnd.evaluar(hechoRobo));
    }

    @Test
    void condicionOrEvaluaTrueSiUnaEsTrue() {
      CondicionOr condicionOr = new CondicionOr();
      condicionOr.agregarCondicion(new CondicionGenerica("categoria", IGUAL, "Categoria_Inexistente"));
      condicionOr.agregarCondicion(new CondicionGenerica("direccion", IGUAL, "123"));
      assertTrue(condicionOr.evaluar(hechoRobo));
    }

    @Test
    void condicionAndVaciaDevuelveTrue() {
      CondicionAnd condicionAnd = new CondicionAnd();
      assertTrue(condicionAnd.evaluar(hechoRobo));
    }

    @Test
    void condicionOrVaciaDevuelveTrue() {
      CondicionOr condicionOr = new CondicionOr();
      assertTrue(condicionOr.evaluar(hechoRobo));
    }

  }

  @Nested
  @DisplayName("Tests para CondicionNot")
  class CondicionNotTest {

    @Test
    void invierteCorrectamenteElResultado() {
      var condicionInterna = new CondicionGenerica("categoria", IGUAL, "Robos");
      var condicionNot = new CondicionNot();
      condicionNot.setCondicion(condicionInterna);
      assertFalse(condicionNot.evaluar(hechoRobo));
      assertTrue(condicionNot.evaluar(hechoPrueba));
    }

    @Test
    void condicionNotConCondicionNulaRetornaFalse() {
      CondicionNot condicionNot = new CondicionNot();
      condicionNot.setCondicion(null);
      assertFalse(condicionNot.evaluar(hechoRobo));
    }
  }

  @Nested
  @DisplayName("Tests para CondicionPredicado")
  class CondicionPredicadoTest {

    @Test
    void evaluaCorrectamenteUnPredicado() {
      var condicionPredicado = new CondicionPredicado(h -> h.getTitulo()
                                                            .startsWith("Prueba"));
      assertTrue(condicionPredicado.evaluar(hechoRobo));
      assertTrue(condicionPredicado.evaluar(hechoPrueba));
    }
  }
}