# Algoritmos de Consenso

## Algoritmo de Consenso

Interfaz la cual implementan los algoritmos a continuación, indica que todo algoritmo debe tener la función `listaDeHechosConsensuados`

## Mayoria Simple

Clase encargada de devolver una lista con hechos tal que, si al menos la mitad de las fuentes del nodo contienen el mismo hecho, se lo considera consensuado.

## Multiples Menciones

Clase encargada de devolver una lista con hechos tal que, si al menos dos fuentes del nodo contienen un mismo hecho y ninguna otra fuente del nodo contiene otro de igual título pero diferentes atributos, se lo considera consensuado.

## Absoluta

Clase encargada de devolver una lista con hechos tal que, si todas las fuentes del nodo contienen el mismo, se lo considera consensuado.

# Calendarización --> App

Clase que actúa como un orquestador que se configura y luego ejecuta una tarea específica. Es el punto de entrada para ejecutar actualizaciones de fuentes desde una tarea programada.

# Colección

Nace de la documentación, son conjuntos de hechos. También indica su fuente de origen, categoría (sirve para saber qué hechos de la fuente tomar), título y descripción.  

# Lector CSV

Tiene como fin subir la información de los datasets, convertir la información en hechos y subirla a una fuente estática (**importar()**).
Separa la lógica de importación de datos desde archivos para no mezclar responsabilidades en otras clases.

# Detector de Spam

Interfaz que tiene la finalidad de determinar si una solicitud es o no spam.

# Filtro

## Filtro

Interfaz la cual implementan los filtros, indica que todo filtro debe tener la función `filtrar`

## Filtros

Son distintas clases cada una con su definición de "filtrar". Los filtros son los siguientes:
De categoria, de dirección, de etiqueta, de fecha, de fecha de carga, de id, de lugar, de origen, de titulo, de identidad, de igual hecho, lista and, lista or y not. 

# Fuentes

## Fuente

Interfaz la cual implementan las fuentes, indica que toda fuente debe tener una funcion `obtenerhechos()` y `validarFuente`.  

## Conexión

Es una clase con una unica función que tiene como objetivo devolver un mapa con los atributos de un hecho, indexados por nombre de atributo

## Fuente Cacheable

Clase que encapsula la lógica de caching para una fuente de datos.

## Fuente de Agregación

Clase hija de Fuente Cacheable, tiene como objetivo enlistar todas las fuentes cargadas al sistema.

## Fuente Demo

Clase hija de Fuente Cacheable que tiene como objetivo facilitar la obtención de hechos de servicios externos.

## Fuente Dinámica

Nace de la documentación, es donde se ubican los hechos creados por los contribuyentes. Puede haber más de una fuente dinámica.  

## Fuente Estática

Nace de la documentación, es donde se ubican los hechos provenientes de un dataset. Puede haber más de una fuente estática.  

## Fuente MetaMapa

Fuente iene como objetivo provver la capacidad de comunicarse con otras instancias de MetaMapa.

# Hecho

## Hecho 

Nace de la documentación, pieza de información que almacena título, descripción, categoría, dirección, ubicación (latitud y longitud), fecha del suceso, fecha en la que se cargó, el origen, etiquetas, id y vigencia (este atributo se utiliza a la hora de eliminar hechos).  

# Reportes

## Repositorio de Solicitudes

Nace de la documentación (recepción de denuncias). Es único (por decisión de diseño) y tiene el fin de almacenar (**agregarSolicitud()**) y gestionar cada solicitud (**gestionarSolicitud()** aceptando o rechazando solicitudes y eliminándolas en el proceso).

## Solicitud

Es un documento que contiene al contribuyente que solicitó la eliminación, el hecho involucrado, el motivo de la solicitud.  
Cada vez que se crea una nueva solicitud se valida con **validarMotivo()** que no esté vacío y no tenga menos de 500 caracteres.  

# Servicio de Copias Locales

Clase encargada de realizar copias de los hechos utilizados por las fuentes cacheables.

# Servicio de Visualización

Clase que permite al usuario visualizar los hechos de las distintas colecciones.

# Servicio Meta Mapa

## MetaMapa Service

Interfaz necesaria para el correcto funcionamiento de Servicio MetaMapa.(**visualizarHecho()**) y filtrar los hechos (**filtrar()**, **filtrarPorCategoria()**, **filtrarPorLugar()**, etc.).  

# Contribuyente  
Nace de la documentación. Cada contribuyente es capaz de crear un hecho (**crearHecho()**) y solicitar la eliminación de un hecho (**solicitarEliminación()**).  
