package com.nttdata.AccountMs.exception;

public class CuentaException extends RuntimeException {
    public CuentaException(String mensaje) {
        super(mensaje);
    }

    public CuentaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}

