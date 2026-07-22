package com.baize.flux.common;
/** Base exception for user-visible Flux failures. */
public class FluxException extends RuntimeException { public FluxException(String message) { super(message); } public FluxException(String message, Throwable cause) { super(message, cause); } }
