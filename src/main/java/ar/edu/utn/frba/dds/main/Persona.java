package ar.edu.utn.frba.dds.main;

public class Persona {
}

class Administrador {

  void crearColeccion() {}
}

class Visualizador { //No requiere dejar datos personales y puede subir hechos, esto lo vuelve un contribuyente.

}

class Contribuyente { //También se las considera personas humanas (se podría tener una etiqueta para identificar contribuyentes???)
  String nombre;

  Contribuyente(String nombre) {
    this.nombre = nombre;
  }
}


