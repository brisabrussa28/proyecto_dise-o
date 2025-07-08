## OBSERVACIONES
    - El checkstyle para el tp anual es sugestivo.
    - Cuidado con clases Sistemas
    - Cuidado con las clases Rol
    - Cuidado con las fachadas
    - Objeto y sujeto (actor) no son lo mismo, pueden haber casos de sujetos para los que no existen objetos.
    - Los requerimientos estan en el dominio de un caso de uso
    - El visualizador no existe porque no tiene comportamiento propio.
    - NO QUEREMOS EL MAPEO DE UN ROL -> UNA CLASE
    - Cada operacion que haga una persona va a tener un metodo dentro de un objeto (FACHADA)
    - No usar assertTrue.
    - Hacer tests de fuenteEstatica y fuenteDinamica (leer hechos filtrando)
    - Buscar una lib para leer CSVs
    - FuenteEstatica debe leer el CSV para buscar hechos
    - Terminología como visualizar y/o mostrar no se usa, es de presentación.

## OBSERVACIONES PT2
    - Es innecesario validar el rol, todo lo relacionado con permisos se valida en una capa superior.
    - No es Gestor, es Repositorio
    - Evitar misplaced methods
    - No usar el singleton para servicioMetamapa, usar inyección de dependencias en cada clase que lo vaya a usar.
    - NO usar systemprintln
    - Reemplazar scheduler con CRON.
    - Liquidar clase FuenteProxy.
    - Separar hechos para que queden en revisión y pueda ser editado.
    - No hace falta analizar la nulidad de las colecciones.

## OBSERVACIONES PT3
    - Fuente cacheable no se llamaría caché, ya que es una fuente que no guarda los hechos realmente como una caché, es más una copia local (completa) de los datos.
    - ServicioDeCopiasLocales -> ServicioBackup.
    - El cron tiene que hacer un forEach() para todas las fuentes.
    - Se podría ir pensando en implementar loggers.
    - Try/catch de fuenteCacheable en la linea 40 sobra, ya que hay una excepcion runtime que ya sería lanzada.
    - Ese mismo try/catch se podría hacer en el mismo forEach().
    - No utilizar List.copyOf() si sólo se va a leer, es más conveniente utilizar el método Collection.unmodifiableList().
    - En fuente dinámica se podría, en lugar se podría redefinir al ḿetodo para que no haga nada.
    - Se podría agregar otra interfaz para buscar hechos para los casos donde no haya un algoritmo de consenso definido.
    - Algoritmo Absoluto está realizando varias operaciones de más
    - Los algoritmos se deberían ejecutar en un cron.
    - Por más que tengo un algoritmo de consenso, tengo que poder visualizar todos los hechos aunque no se cumpla con el algoritmo si así se desea.
    - Nodo -> Computadora ejecutando.