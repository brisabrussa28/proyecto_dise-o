package ar.edu.utn.frba.dds.domain.csv;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import com.opencsv.bean.CsvToBeanBuilder;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;



public class BindLectorCsv {
  public static void main(String[] args, char separador, String formatoFecha) {
    List<Hecho> hechosImportados = new ArrayList<>();
    try {
      List<BindCsv> csvBeanReader = new CsvToBeanBuilder(new FileReader(
          "C:\\Users\\User\\Desktop\\brisa\\ugkbbgu\\src\\main\\java\\ar\\edu\\utn\\frba\\dds\\domain\\csv\\ejemploReordenado.csv"
      )).withSeparator(separador).withIgnoreEmptyLine(true).withType(BindCsv.class).build().parse();
      //withIgnoreEmptyLine(true)--->Ignora las líneas completamente vacías

      for (BindCsv cr : csvBeanReader) {
        System.out.println(cr.getTitulo() + "   |   " + cr.getDescripcion() + "   |   " + cr.getCategoria() + "   |   "
            + cr.getLatitud() + "   |   " + cr.getLongitud() + "   |   " + cr.getFechaSuceso());
        float latitud = 0;
        float longitud = 0;
        if(!cr.getLatitud().isBlank() && cr.getLatitud()!= null){
          latitud = Float.parseFloat(cr.getLatitud());
        }
        if(!cr.getLongitud().isBlank() && cr.getLongitud()!= null){
          longitud = Float.parseFloat(cr.getLongitud());
        }

        SimpleDateFormat formato = new SimpleDateFormat(formatoFecha);
        Date fechaSuceso = null;
        try {
          if(cr.getFechaSuceso() != null){
            fechaSuceso = formato.parse(cr.getFechaSuceso());
          }
        } catch (ParseException e) {
          throw new RuntimeException(e);
        }

        PuntoGeografico ubicacion = new PuntoGeografico(latitud, longitud);
        List<String> etiquetasVacias = new ArrayList<>();
        String direccion = null;

        Hecho hecho = new Hecho(
            cr.getTitulo(),
            cr.getDescripcion(),
            cr.getCategoria(),
            direccion,
            ubicacion,
            fechaSuceso,//Creo que hay que dejarla como Date o LocalDate, ya que no especifican horas los csvs
            Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()),
            Origen.DATASET,
            etiquetasVacias
        );

        hechosImportados.add(hecho);
        //System.out.println(hechosImportados);
      }
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  private String[] lineas = null; //Almacena cada línea leída

}