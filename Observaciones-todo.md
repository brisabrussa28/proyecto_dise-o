## OBSERVACIONES
- [x] El checkstyle para el tp anual es sugestivo.
- [x] Cuidado con clases Sistemas
- [x] Cuidado con las clases Rol
- [x] Cuidado con las fachadas
- [x] Objeto y sujeto (actor) no son lo mismo, pueden haber casos de sujetos para los que no existen objetos.
- [x] Los requerimientos estan en el dominio de un caso de uso
- [x] El visualizador no existe porque no tiene comportamiento propio.
- [x] NO QUEREMOS EL MAPEO DE UN ROL -> UNA CLASE
- [x] Cada operacion que haga una persona va a tener un metodo dentro de un objeto (FACHADA)
- [x] No usar assertTrue.
- [x] Hacer tests de fuenteEstatica y fuenteDinamica (leer hechos filtrando)
- [x] Buscar una lib para leer CSVs
- [x] FuenteEstatica debe leer el CSV para buscar hechos
- [x] Terminología como visualizar y/o mostrar no se usa, es de presentación.

## OBSERVACIONES PT2
- [x] Es innecesario validar el rol, todo lo relacionado con permisos se valida en una capa superior.
- [x] No es Gestor, es Repositorio
- [x] Evitar misplaced methods
- [x] No usar el singleton para servicioMetamapa, usar inyección de dependencias en cada clase que lo vaya a usar.
- [x] NO usar systemprintln
- [x] Reemplazar scheduler con CRON.
- [x] Liquidar clase FuenteProxy.
- [x] Separar hechos para que queden en revisión y pueda ser editado.
- [x] No hace falta analizar la nulidad de las colecciones.

## OBSERVACIONES PT3
- [x] Fuente cacheable no se llamaría caché, ya que es una fuente que no guarda los hechos realmente como una caché, es más una copia local (completa) de los datos.
- [x] ServicioDeCopiasLocales -> ServicioBackup.
- [x] El cron tiene que hacer un forEach() para todas las fuentes.
- [X] Se podría ir pensando en implementar loggers.
- [x] Try/catch de fuenteCacheable en la linea 40 sobra, ya que hay una excepcion runtime que ya sería lanzada.
- [x] Ese mismo try/catch se podría hacer en el mismo forEach().
- [x] No utilizar List.copyOf() si sólo se va a leer, es más conveniente utilizar el método Collection.unmodifiableList().
- [X] En fuente dinámica se podría, en lugar se podría redefinir al ḿetodo para que no haga nada.
- [ ] Se podría agregar otra interfaz para buscar hechos para los casos donde no haya un algoritmo de consenso definido.
- [x] Algoritmo Absoluto está realizando varias operaciones de más
- [x] Los algoritmos se deberían ejecutar en un cron.
- [x] Por más que tengo un algoritmo de consenso, tengo que poder visualizar todos los hechos aunque no se cumpla con el algoritmo si así se desea.
- Nodo -> Computadora ejecutando.

## COSAS PT4
- [x] Agregar componente de estadisticas (Con el tipo de estadisticas incluido)
- [x] Agregar provincia al hecho
- [x] Agregar modulo de exportacion de csvs (seria mejor directamente que sea capaz de exportar cualquier obj)
- [X] Sumar busqueda por full text search (al menos del titulo y descripcion del hecho pero extensible a todos los demas campos) -> Definir idioma ->
- [X] Se deberán persistir las entidades del modelo planteado. Para ello se debe utilizar un ORM.
- [X] TESTS DE TODO 
- [X] Diagrama de clases actualizado
- [X] Justificacion de disenio (no tengo enie)
- [X] Diagrama der fisico


## OBSERVACIONES PT4
- [ ] Agregar test de hecho editable
- [ ] Colección debería ser clase abstracta
- [ ] Al serializar no cambiar el modelo -> ver si se puede no serializar
- [ ] getFiltro --> this.condicion
- [ ] Befor load y after load --> combinar con transient --> preferiblemente @convert
- [ ] Persistir filtros
- [ ] En un parcial hacer mapeo de herencia
- [x] Geolocalizar via api --> Para los csv que no tengan provincia --> Podemos utilizar APIS, bibliotecas, etc, lo decimos nosotros
- [ ] Agregar PostreSQL
- [ ] Realizar Querys para las estadisticas --> SQL
- [ ] Persistir hechos eliminados, solicitudes SPAM
- [ ] fletar backups, no tienen proposito con la bd
- [ ] No persistir hechos de csv, de fuentes externas, cache, hechos de fuentes estaticas. --> Persistir la informacion de las fuentes, NO SUS HECHOS
- [ ] Para las estadisticas privilegiar la informacion acumulativa
- [ ] Averiguar donde tiene los datos Lucene, si en memoria o en la bd.
- [ ] Mandar nuestra estrategia respecto el almacenamiento de datos a bulgarelli --> ¿Qué datos vamos a guardar? ¿Cuánto tiempo? ¿Por qué? 
- [ ] Refactorizar colecciones y hacerlo mas expresivo
- [ ] Hacer mas expresivo algoritmos de concenso -> ver si se puede aplicar SQL
- [ ] Hacer mas expresivo a los exportadores
- [ ] Quiza no refactorizar CSV (pq funcionar funciona), pero por ahi hacerle un facade o algo que encapsule su comportamiento
- [ ] Revisar que las provincias esten bien incluidas en todos lados
- [ ] Refactizar y repensar todo lo relacionado a gestion de solicitudes
