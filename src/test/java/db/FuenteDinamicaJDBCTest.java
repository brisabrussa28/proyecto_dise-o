package db;

import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.HechoBuilder;
import ar.edu.utn.frba.dds.domain.hecho.Origen;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.repos.FuentesRepository;
import ar.edu.utn.frba.dds.domain.utils.DBUtils;
import java.time.LocalDateTime;
import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.Test;

public class FuenteDinamicaJDBCTest {

  @Test
  public void puedoPersistirUnHechoEnFuenteDinamica() {
    // Create DataSource with settings matching persistence.xml
    BasicDataSource dataSource = new BasicDataSource();
    dataSource.setDriverClassName("org.postgresql.Driver");
    dataSource.setUrl("jdbc:postgresql://localhost:5432/ddstpa");
    dataSource.setUsername("postgres");
    dataSource.setPassword("1234abcd");

    // Initialize repository with datasource
    var em = DBUtils.getEntityManager();

    FuentesRepository repo = new FuentesRepository(dataSource, em);
    FuenteDinamica fuente = new FuenteDinamica("Fuente de prueba");
    PuntoGeografico ubicacion = new PuntoGeografico(33.0, 44.0);
    LocalDateTime fechaSuceso = LocalDateTime.now()
                                             .minusDays(5);
    LocalDateTime fechaCarga = LocalDateTime.now();

    Hecho hecho = new HechoBuilder()
        .conTitulo("Robo")
        .conDescripcion("Robo a mano armada")
        .conCategoria("DELITO")
        .conDireccion("Calle falsa 123")
        .conUbicacion(ubicacion)
        .conFechaSuceso(fechaSuceso)
        .conFechaCarga(fechaCarga)
        .conFuenteOrigen(Origen.PROVISTO_CONTRIBUYENTE)
        .build();
    hecho.setTitulo("Hecho de prueba");
    fuente.agregarHecho(hecho);
    DBUtils.comenzarTransaccion(em);
    em.persist(fuente);
    DBUtils.commit(em);
  }

}
