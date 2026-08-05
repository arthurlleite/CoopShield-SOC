package com.coopshield.soc.accesscontrol.infrastructure;

/**
 * Corpo de erro generico para respostas 401/403, deliberadamente separado
 * do equivalente no modulo identity para nao criar uma dependencia entre
 * os dois modulos.
 */
public record ApiErrorBody(String error, String message) {
}
