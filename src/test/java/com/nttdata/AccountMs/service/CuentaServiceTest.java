package com.nttdata.AccountMs.service;

import com.nttdata.AccountMs.exception.CuentaException;
import com.nttdata.AccountMs.model.Cuenta;
import com.nttdata.AccountMs.model.CuentaRequest;
import com.nttdata.AccountMs.model.CuentaResponse;
import com.nttdata.AccountMs.model.TipoCuentaEnum;
import com.nttdata.AccountMs.repository.CuentaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CuentaServiceTest {

    @InjectMocks
    private CuentaService cuentaService;

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private RestTemplate restTemplate;

    private CuentaRequest cuentaRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        cuentaRequest = new CuentaRequest();
        cuentaRequest.setClienteId(1);
        cuentaRequest.setTipoCuenta(CuentaRequest.TipoCuentaEnum.AHORROS);
    }

    @Test
    void testCrearCuentaClienteExistente() {
        // Simulamos que el cliente existe en otro servicio
        when(restTemplate.getForEntity(anyString(), eq(Void.class))).thenReturn(ResponseEntity.ok().build());

        // Simulamos el guardado de la cuenta en el repositorio
        when(cuentaRepository.save(any(Cuenta.class))).thenReturn(new Cuenta());

        CuentaResponse cuentaResponse = cuentaService.crearCuenta(cuentaRequest);

        assertNotNull(cuentaResponse);
        verify(cuentaRepository, times(1)).save(any(Cuenta.class));
    }

    @Test
    void testCrearCuentaClienteNoExistente() {
        // Simulamos que el cliente no existe
        when(restTemplate.getForEntity(anyString(), eq(Void.class))).thenReturn(ResponseEntity.notFound().build());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            cuentaService.crearCuenta(cuentaRequest);
        });

        assertEquals("El cliente no existe", exception.getMessage());
        verify(cuentaRepository, times(0)).save(any(Cuenta.class));
    }

    @Test
    void testObtenerCuentaExistente() {
        // Simulamos que la cuenta existe
        Cuenta cuenta = new Cuenta();
        cuenta.setId(1);
        when(cuentaRepository.findById(1)).thenReturn(Optional.of(cuenta));

        Optional<CuentaResponse> cuentaResponse = cuentaService.obtenerCuenta(1);

        assertTrue(cuentaResponse.isPresent());
    }

    @Test
    void testObtenerCuentaNoExistente() {
        when(cuentaRepository.findById(1)).thenReturn(Optional.empty());

        Optional<CuentaResponse> cuentaResponse = cuentaService.obtenerCuenta(1);

        assertFalse(cuentaResponse.isPresent());
    }
}
