package ar.edu.utn.frba.dds.domain.csv;
import ar.edu.utn.frba.dds.domain.hecho.CampoHecho;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import java.io.FileReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.*;

public class SuperCsv {

  private String[] lineas = null; //Almacena cada línea leída

  public List<Hecho> importar(String rutaArchivo, Map<CampoHecho, List<String>> mapeoColumnas, String formatoFecha) {
    List<Hecho> hechosImportados = new ArrayList<>();

    try {
      CSVReader reader = new CSVReaderBuilder(new FileReader(rutaArchivo))
          .withCSVParser(new CSVParserBuilder().withSeparator(',').build())
          .build();

      String[] columnas = reader.readNext();
      //String[] valores;

      while ((lineas = reader.readNext()) != null) {
        int indiceLatitud = List.of(columnas).indexOf("latitud");

        double latitud = (mapeoColumnas.containsKey(CampoHecho.LATITUD) && lineas[List.of(columnas).indexOf("latitud")] != "")
            ? Double.parseDouble(lineas[indiceLatitud].trim())
            : 0.0;

        int indiceLongitud = List.of(columnas).indexOf("longitud");
        double longitud = (mapeoColumnas.containsKey(CampoHecho.LONGITUD) && lineas[indiceLongitud] != "")
            ? Double.parseDouble(lineas[indiceLongitud].trim())
            : 0.0;

        PuntoGeografico ubicacion = new PuntoGeografico(latitud, longitud);

        String titulo = mapeoColumnas.containsKey(CampoHecho.TITULO)
            ? concatenarColumnas(mapeoColumnas.get(CampoHecho.TITULO), columnas, lineas, " ")
            : "";

        String descripcion = mapeoColumnas.containsKey(CampoHecho.DESCRIPCION)
            ? concatenarColumnas(mapeoColumnas.get(CampoHecho.DESCRIPCION), columnas, lineas, " ")
            : "";

        String categoria = mapeoColumnas.containsKey(CampoHecho.CATEGORIA)
            ? concatenarColumnas(mapeoColumnas.get(CampoHecho.CATEGORIA), columnas, lineas, " ")
            : "";

        String fechaSuceso = mapeoColumnas.containsKey(CampoHecho.FECHA_SUCESO) && !lineas[List.of(columnas).indexOf("fechaSuceso")].isBlank()
            ? concatenarColumnas(mapeoColumnas.get(CampoHecho.FECHA_SUCESO), columnas, lineas, "/")
            : null;

        String direccion = mapeoColumnas.containsKey(CampoHecho.DIRECCION)
            ? concatenarColumnas(mapeoColumnas.get(CampoHecho.DIRECCION), columnas, lineas, ", ")
            : "";
        // agregar esta validacion para todos los demas campos
        // el formatter no tiene hora
        LocalDateTime fechaHora = null;
        if (fechaSuceso!= null && !fechaSuceso.isBlank()) {
          DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatoFecha);
          LocalDate fecha = LocalDate.parse(fechaSuceso, formatter);
          fechaHora = fecha.atStartOfDay();
        }
        List<String> etiquetasVacias = new ArrayList<>();



        imprimirLinea();                  //Vemos como se separaron
        System.out.println();             //al printearlos en la terminal
      }//Imprime hasta que no hayan más líneas por imprimir (llegar a línea null)
    } catch (Exception e) {
      JOptionPane.showMessageDialog(null, e);
    }
    return hechosImportados;
  }

  public void imprimirLinea(){
    for (int i = 0; i < lineas.length; i++){
      System.out.print(lineas[i]+"   |   ");
    }
  }

private String concatenarColumnas(List<String> columnasMapeadas, String[] columnas, String[] valores, String separadorConcatenacion) {
  StringBuilder resultado = new StringBuilder();
  for (String columna : columnasMapeadas) {
    int indice = List.of(columnas).indexOf(columna);
    if (indice != -1 && !valores[indice].isBlank()) {
      if (!resultado.isEmpty()) {
        resultado.append(separadorConcatenacion);
      }
      resultado.append(valores[indice]);
    }
  }
  return resultado.toString();
}
}