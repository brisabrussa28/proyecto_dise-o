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