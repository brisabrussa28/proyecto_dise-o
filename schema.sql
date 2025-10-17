
    create table AlgoritmoDeConsenso (
       tipo_algoritmo varchar(31) not null,
        algoritmo_id  bigserial not null,
        primary key (algoritmo_id)
    );

    create table Coleccion (
       coleccion_id  bigserial not null,
        coleccion_categoria varchar(255),
        coleccion_descripcion varchar(255),
        coleccion_titulo varchar(255),
        coleccion_algoritmo int8,
        coleccion_condicion_id int8,
        coleccion_fuente int8,
        primary key (coleccion_id)
    );

    create table Condicion (
       tipo_condicion varchar(31) not null,
        id  bigserial not null,
        campo varchar(255),
        operador varchar(255),
        valor varchar(255),
        condicion_id int8,
        condicion_padre_id int8,
        primary key (id)
    );

    create table Estadistica (
       estadistica_id  bigserial not null,
        estadistica_nombre varchar(255),
        estadistica_valor int8,
        primary key (estadistica_id)
    );

    create table Fuente (
       tipo_fuente varchar(31) not null,
        fuente_id  bigserial not null,
        fuente_nombre varchar(255),
        fuente_ruta_archivo varchar(255),
        fuente_fuente_cargada int8,
        primary key (fuente_id)
    );

    create table Hecho (
       hecho_id  bigserial not null,
        hecho_estado varchar(255) not null,
        hecho_origen varchar(255) not null,
        hecho_categoria varchar(255),
        hecho_descripcion varchar(255),
        hecho_direccion varchar(255),
        hecho_fecha_carga timestamp,
        hecho_fecha_suceso timestamp,
        hecho_provincia varchar(255),
        hecho_titulo varchar(255),
        latitud float8 not null,
        longitud float8 not null,
        hecho_fuente int8,
        primary key (hecho_id)
    );

    create table Hecho_etiquetas (
       Hecho_hecho_id int8 not null,
        etiqueta_nombre varchar(255)
    );

    create table Solicitud (
       solicitud_id  bigserial not null,
        estado varchar(255) not null,
        razonEliminacion varchar(1024),
        hecho_id int8,
        primary key (solicitud_id)
    );

    alter table Coleccion 
       add constraint FKq9sr0dntsmx5799hg23vcydxo 
       foreign key (coleccion_algoritmo) 
       references AlgoritmoDeConsenso;

    alter table Coleccion 
       add constraint FK62udxxwqn4y3et9d1osw4efu2 
       foreign key (coleccion_condicion_id) 
       references Condicion;

    alter table Coleccion 
       add constraint FK6vm8xr6y2fuv3w9aoy28w6yqo 
       foreign key (coleccion_fuente) 
       references Fuente;

    alter table Condicion 
       add constraint FKi4nyes4c8x7rcoa61sipoe1gb 
       foreign key (condicion_id) 
       references Condicion;

    alter table Condicion 
       add constraint FK2mcd1944yeg0v6no6gf02kv13 
       foreign key (condicion_padre_id) 
       references Condicion;

    alter table Fuente 
       add constraint FKabkm7gsw7cksa6rdhv3ewn7ks 
       foreign key (fuente_fuente_cargada) 
       references Fuente;

    alter table Hecho 
       add constraint FKsx0jlcv15831wqwou790luroa 
       foreign key (hecho_fuente) 
       references Fuente;

    alter table Hecho_etiquetas 
       add constraint FK2sta5r7dmxcyfecqxrv0gblld 
       foreign key (Hecho_hecho_id) 
       references Hecho;

    alter table Solicitud 
       add constraint FKpbsvdn3rrh2a6uipcgrj6xdp9 
       foreign key (hecho_id) 
       references Hecho;
