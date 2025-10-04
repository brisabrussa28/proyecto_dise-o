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

## Correcciones PT4
- [X] Evitar la serialización directa de la lógica de filtros.
- [x] Implementar la reconstrucción de filtros con callbacks de JPA (@PostLoad).
- [X] Definir estrategia de mapeo de herencia para entidades.
- [X] Los Hechos de fuentes externas (CSV, API, caché) no se persisten.
- [x] Refactorizar getFiltro para que devuelva this.condicion directamente. *~KUKOIDE*
- [X] Mejorar la expresividad y diseño del módulo de Colecciones (evaluar convertirla en clase abstracta).
- [X] Mejorar la expresividad de los Algoritmos de Consenso (evaluar uso de SQL).
- [x] Pensar y enviarle a Bulgarelli la estrategia de almacenamiento de datos (qué se guarda, por cuánto tiempo y por qué).
- [x] Geolocalizar via api --> Para los csv que no tengan provincia --> Podemos utilizar APIS, bibliotecas, etc, lo decimos nosotros
- [X] Migrar la configuración de la base de datos a PostgreSQL.
- [X] Eliminar la lógica de backups por archivo a backup por DB.
- [X] Mejorar la expresividad y diseño de los Exportadores.
- [X] Mejorar la expresividad y diseño de los Lectores.
- [X] Solo leer del csv en fuente estatica cuando se crea, despues persistir en bdd los hechos
- [X] Corregir api provincia
- [ ] Persistir Colecciones, Estadísticas, Solicitudes de Eliminación (incluyendo SPAM) y Hechos Eliminados.
- [ ] Hacer el cálculo de estadísticas utilizando consultas SQL nativas.
- [ ] Investigar el método de almacenamiento de índices de Hibernate Search (Lucene).
- [ ] Rediseñar y refactorizar por completo el módulo de gestión de solicitudes.
- [ ] Agregar los tests para Hecho Editable.
- [ ] Revisar y asegurar la consistencia del campo provincia en todo el sistema.
- [ ] Para las estadísticas, priorizar el almacenamiento de valores absolutos en lugar de porcentajes.
- [ ] Clase Estadistica es un bruto code smell, sólo tiene accessors y no comportamiento.
- [ ] Algoritmo de concenso deberia revisar todas las fuentes del nodo (instancia) osea todas las fuentes del sistema
        -> necesitamos un repo de fuentes
- [ ] Repositorios para persistir las clases
