import * as Auth from "./auth";
import * as Catalogos from "./catalogos";
import * as Hechos from "./hechos";
import * as Gestion from "./gestion";

export const service = {
  // Auth
  login: Auth.login,
  register: Auth.register,
  logout: Auth.logout,
  // Catálogos
  obtenerCategorias: Catalogos.obtenerCategorias,
  obtenerFuentes: Catalogos.obtenerFuentes,
  // Hechos
  crearHecho: Hechos.crearHecho,
  obtenerHechos: Hechos.obtenerHechos,
  obtenerHecho: Hechos.obtenerHecho,
  // Gestión (solicitudes, colecciones, estadísticas)
  crearSolicitud: Gestion.crearSolicitud,
  crearColeccion: Gestion.crearColeccion,
  aprobarSolicitud: Gestion.aprobarSolicitud,
  rechazarSolicitud: Gestion.rechazarSolicitud,
  consultarEstadistica: Gestion.consultarEstadistica,
};

export type { LoginRequest, LoginResponse, RegisterRequest, RegisterResponse } from "../types/auth";
export type { Categoria, Fuente } from "../types/catalogos";
export type { Hecho, HechoCreate } from "../types/hechos";
export type { Solicitud, SolicitudCreate, Coleccion, ColeccionCreate, EstadisticaRespuesta } from "../types/gestion";

export default service;
