package ar.edu.utn.frba.dds.domain.calendarizacion;

import ar.edu.utn.frba.dds.domain.serviciodeagregacion.ServicioDeAgregacion;

public class app {

  public static void main(String[] args) {
    ServicioDeAgregacion servicioDeAgregacion = new ServicioDeAgregacion();

    servicioDeAgregacion.actualizarDatos();
        /*Debemos actualizarlos cada 1 hora los datos (el tiempo está sujeto al período de actualización
    de las fuentes que abarque, en este caso, nos regimos bajo el único que conocemos, que es el de la fuente proxy).
     */
  }

  //mvn clean package
  //crontab -e
  //java -jar dirección_del_archivo

}