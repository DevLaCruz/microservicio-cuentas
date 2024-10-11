package com.nttdata.AccountMs.controller;

import com.nttdata.AccountMs.model.CuentaRequest;
import com.nttdata.AccountMs.model.CuentaResponse;
import com.nttdata.AccountMs.service.CuentaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class CuentaControllerTest {

    @InjectMocks
    private CuentaController cuentaController;

    @Mock
    private CuentaService cuentaService;

    private CuentaRequest cuentaRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        cuentaRequest = new CuentaRequest();
        cuentaRequest.setClienteId(1);
        cuentaRequest.setTipoCuenta(CuentaRequest.TipoCuentaEnum.AHORROS);
    }

    @Test
    void testCrearCuenta() {
        CuentaResponse cuentaResponse = new CuentaResponse();
        when(cuentaService.crearCuenta(any(CuentaRequest.class))).thenReturn(cuentaResponse);

        ResponseEntity<CuentaResponse> responseEntity = cuentaController.crearCuenta(cuentaRequest);

        assertNotNull(responseEntity.getBody());
        assertEquals(201, responseEntity.getStatusCodeValue());
        verify(cuentaService, times(1)).crearCuenta(any(CuentaRequest.class));
    }

    @Test
    void testObtenerCuenta() {
        CuentaResponse cuentaResponse = new CuentaResponse();
        when(cuentaService.obtenerCuenta(1)).thenReturn(Optional.of(cuentaResponse));

        ResponseEntity<CuentaResponse> responseEntity = cuentaController.obtenerCuenta(1);

        assertNotNull(responseEntity.getBody());
        assertEquals(200, responseEntity.getStatusCodeValue());
    }

    @Test
    void testObtenerCuentaNoExistente() {
        when(cuentaService.obtenerCuenta(1)).thenReturn(Optional.empty());

        ResponseEntity<CuentaResponse> responseEntity = cuentaController.obtenerCuenta(1);

        assertEquals(404, responseEntity.getStatusCodeValue());
    }
}
