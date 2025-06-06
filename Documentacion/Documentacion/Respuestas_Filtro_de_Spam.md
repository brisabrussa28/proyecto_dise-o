- ¿Cómo se podría implementar un filtro de spam sin recurrir a servicios externos?

  Tomar una solicitud y convertir su razon de eliminación en una cadena de palabras (cada espacio es una palabra en el array)

  Hacemos uso de un vector propio de nuestro sistema que contenga palabras a eliminar de la cadena para no ser tomados en cuenta (ejemplo: y, el, tu, como, etc)

  Una vez nuestra cadena tiene unicamente las palabras importantes a analizar comenzamos a compararlo con el resto de solicitudes en el sistema...

  Si una solicitud tiene muchas palabras con TF-IDF alto, será considerado spam y será eliminada

  Otra opción es hacer una lista de palabras o frases consideradas spam a buscar dentro de una solictud, si se encuentran esa solicitud será eliminada.

- ¿Cómo se podría implementar un filtro de spam en base a consumir servicios externos en la nube? ¿Qué consecuencias desde el punto de vista de costos y seguridad de datos traería?

  En el caso de usar  un filtro spam en base a consumir servicios externos en la nube podemos usar google cloud natural languaje api o azure cognitiv services y enviarles los datos de nuestras solicitud. Estos servicios  nos diran si nuestra solicitud es spam y nosotros solo deberemos eliminar aquellas notificadas como tal.

  Los problemas con hacer uso de esa solución son varios, lo primero a resaltar es el costo. En el caso de manejar gran flujo de solicitudes se vuelve costoso ya que se debe pagar a la nube por cada texto analizar. Además, al requerir de la utilización de internet para su funcionamiento, la velocidad de respuesta cae a menos que se haga una inversion importante en los servidores.

  Lo segundo a resaltar (y lo más importante) tiene relación con la seguridad tanto de los usuarios como del sistema mismo. Al enviar nuestras solicitudes a servicios externos, estamos exponiendo sus datos a la recopilación en beneficio del servicio que utilicemos, en contra de la voluntad del usuario. Además, nos exponemos a recibir ataques y sufrir diversas vulnerabilidades en el envío y recepción de este proceso si no hacemos uso de buenos mecanismos de protección.
