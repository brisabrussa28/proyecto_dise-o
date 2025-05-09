# LectorCSV  
Tiene como fin subir la información de los datasets, convertir la información en hechos y subirla a una fuente estática (**importar()**).
Separa la lógica de importación de datos desde archivos para no mezclar responsabilidades en otras clases

# Colección  
Nace de la documentación, son conjuntos de hechos. También indica su fuente de origen, categoría (sirve para saber qué hechos de la fuente tomar), título y descripción.  
Tiene entre sus funciones almacenar y administrar hechos (**agregarHecho()**, **eliminarHecho()**, **cumpleCriterioDePertenencia()**).  

# Fuente  
Clase abstracta y es padre de `Fuente Estática`, `Fuente Dinámica`, `Fuente Proxy` y el `Servicio de Agregación`, ya que comparten funciones y atributos (**obtenerHechos()**, **eliminarHecho()**, nombre y lista de hechos).  

# Fuente Estática  
Nace de la documentación, es donde se ubican los hechos provenientes de un dataset. Puede haber más de una fuente estática.  

# Fuente Dinámica  
Nace de la documentación, es donde se ubican los hechos creados por los contribuyentes. Puede haber más de una fuente dinámica.  

# Fuente Proxy *(no desarrollada)*  
Nace de la documentación, tiene como objetivo la integración con servicios de fuentes de datos y datasets provistos por otras ONGs.  

# Servicio de Agregación  
Nace de la documentación, combina las fuentes para obtener los hechos cargados en dichas fuentes (**agregarFuente()**, **obtenerHechos()**). Puede haber más de una.  

# Hecho  
Nace de la documentación, pieza de información que almacena título, descripción, categoría, dirección, ubicación (latitud y longitud), fecha del suceso, fecha en la que se cargó, el origen, etiquetas, id y vigencia (este atributo se utiliza a la hora de eliminar hechos).  

# Punto Geográfico  
Contiene dos atributos, latitud y longitud, los cuales serán vinculados a un hecho para mayor precisión en su ubicación.
Encapsula la información de latitud y longitud

# Origen  
Es un `enum`, se utiliza para indicar a la hora de crear un hecho cuál es su origen.
Se define como enum para asegurar que los hechos tengan un origen válido y acotado. Esto evita errores de carga
Si el hecho fue creado por un contribuyente: **"PROVISTO_CONTRIBUYENTE"**.  
Si fue cargado de un dataset: **"DATASET"**.  
Si fue cargado por un administrador: **"CARGA_MANUAL"**.  

# Gestor de reportes  
Nace de la documentación (recepción de denuncias). Es único (por decisión de diseño) y tiene el fin de almacenar (**agregarSolicitud()**) y gestionar cada solicitud (**gestionarSolicitud()** aceptando o rechazando solicitudes y eliminándolas en el proceso).  

# Solicitud  
Es un documento que contiene al contribuyente que solicitó la eliminación, el hecho involucrado, el motivo de la solicitud.  
Cada vez que se crea una nueva solicitud se valida con **validarMotivo()** que no esté vacío y no tenga menos de 500 caracteres.  

# Persona  
Clase abstracta y es padre de `Administrador` y `Visualizador`, ya que ambas comparten los atributos nombre y email.  

# Administrador  
Nace de la documentación. Cada administrador tiene el objetivo de crear colecciones (**crearColección()**), importar los hechos de los datasets (**importarDesdeCSV()**) y gestionar las solicitudes (**obtenerSolicitud()**, **obtenerSolicitudPorPosicion()**, **gestionarSolicitud()**).  

# Visualizador  
Nace de la documentación y es padre de `Contribuyente` (ya que un contribuyente también puede leer información). Puede crear hechos (como anónimo, **agregarHechoAFuente()**), visualizar información (**visualizarHecho()**) y filtrar los hechos (**filtrar()**, **filtrarPorCategoria()**, **filtrarPorLugar()**, etc.).  

# Contribuyente  
Nace de la documentación. Cada contribuyente es capaz de crear un hecho (**crearHecho()**) y solicitar la eliminación de un hecho (**solicitarEliminación()**).  
