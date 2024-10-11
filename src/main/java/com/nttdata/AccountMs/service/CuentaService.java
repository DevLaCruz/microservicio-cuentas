package com.nttdata.AccountMs.service;

import com.nttdata.AccountMs.exception.CuentaException;
import com.nttdata.AccountMs.model.Cuenta;
import com.nttdata.AccountMs.model.TipoCuentaEnum;
import com.nttdata.AccountMs.model.CuentaRequest;
import com.nttdata.AccountMs.model.CuentaResponse;
import com.nttdata.AccountMs.repository.CuentaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CuentaService {
    @Autowired
    private CuentaRepository cuentaRepository;

    @Autowired
    private RestTemplate restTemplate; // Agregar RestTemplate

    private final String URL_CLIENTE_SERVICE = "http://localhost:8080/clientes/"; // URL del otro microservicio

    public CuentaResponse crearCuenta(CuentaRequest cuentaRequest) {

        ResponseEntity<Void> response = restTemplate.getForEntity(URL_CLIENTE_SERVICE + cuentaRequest.getClienteId(), Void.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            // El cliente existe, puedes proceder a crear la cuenta
        } else {
            throw new IllegalArgumentException("El cliente no existe");
        }

        Cuenta cuenta = new Cuenta();
        cuenta.setClienteId(cuentaRequest.getClienteId());
        cuenta.setTipoCuenta(convertirTipoCuenta(cuentaRequest.getTipoCuenta()));
        // Generar el número de cuenta automáticamente
        String numeroCuenta = generarNumeroCuenta();
        cuenta.setNumeroCuenta(numeroCuenta);

        cuenta.setSaldo(0.0);

        Cuenta savedCuenta = cuentaRepository.save(cuenta);
        return convertToResponse(savedCuenta);
    }

    private String generarNumeroCuenta() {
        String numeroSecuencial = String.valueOf(System.currentTimeMillis()); // Ejemplo simple usando timestamp
        return numeroSecuencial;
    }


    public Optional<CuentaResponse> obtenerCuenta(Integer id) {
        return cuentaRepository.findById(id).map(this::convertToResponse);
    }


    public List<CuentaResponse> listarCuentas() {
        return cuentaRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public boolean eliminarCuenta(Integer id) {
        if (cuentaRepository.existsById(id)) {
            cuentaRepository.deleteById(id);
            return true; // Eliminación exitosa
        }
        return false; // No se encontró la cuenta
    }



    private CuentaResponse convertToResponse(Cuenta cuenta) {
        CuentaResponse response = new CuentaResponse();
        response.setId(cuenta.getId());
        response.setClienteId(cuenta.getClienteId());
        response.setTipoCuenta(convertirTipoCuentaResponse(cuenta.getTipoCuenta())); // Conversión de TipoCuentaEnum a CuentaResponse.TipoCuentaEnum
        response.setNumeroCuenta(cuenta.getNumeroCuenta());
        response.setSaldo(cuenta.getSaldo());
        return response;
    }

    // Conversión de CuentaRequest.TipoCuentaEnum a TipoCuentaEnum
    private TipoCuentaEnum convertirTipoCuenta(CuentaRequest.TipoCuentaEnum tipoCuentaRequest) {
        switch (tipoCuentaRequest) {
            case AHORROS:
                return TipoCuentaEnum.AHORROS;
            case CORRIENTE:
                return TipoCuentaEnum.CORRIENTE;
            default:
                throw new IllegalArgumentException("Tipo de cuenta no válido");
        }
    }

    // Conversión de TipoCuentaEnum a CuentaResponse.TipoCuentaEnum
    private CuentaResponse.TipoCuentaEnum convertirTipoCuentaResponse(TipoCuentaEnum tipoCuenta) {
        switch (tipoCuenta) {
            case AHORROS:
                return CuentaResponse.TipoCuentaEnum.AHORROS;
            case CORRIENTE:
                return CuentaResponse.TipoCuentaEnum.CORRIENTE;
            default:
                throw new IllegalArgumentException("Tipo de cuenta no válido");
        }
    }


    public boolean depositar(Integer cuentaId, Double cantidad) {
        Optional<Cuenta> cuentaOptional = cuentaRepository.findById(cuentaId);

        if (cuentaOptional.isPresent()) {
            Cuenta cuenta = cuentaOptional.get();

            // Validar que el saldo no sea nulo antes de proceder
            if (cuenta.getSaldo() == null) {
                cuenta.setSaldo(0.0);  // Inicializar el saldo a 0 si es nulo
            }

            // Realizar el depósito
            cuenta.setSaldo(cuenta.getSaldo() + cantidad);
            cuentaRepository.save(cuenta);
            return true;
        }
        return false;
    }


    public boolean retirar(Integer cuentaId, Double monto) {
        Cuenta cuenta = cuentaRepository.findById(cuentaId)
                .orElseThrow(() -> new EntityNotFoundException("Cuenta no encontrada"));

        // Permitir sobregiro solo para cuentas corrientes
        if (cuenta.getTipoCuenta() == TipoCuentaEnum.CORRIENTE) {
            if (cuenta.getSaldo() - monto < -500) {
                return false; // Fondos insuficientes para sobregiro
            }
        } else { // Para otras cuentas, no permitir sobregiro
            if (cuenta.getSaldo() < monto) {
                throw new CuentaException("Fondos insuficientes para realizar el retiro.");
            }
        }

        // Realizar la retirada
        cuenta.setSaldo(cuenta.getSaldo() - monto); // Restar el monto del saldo
        cuentaRepository.save(cuenta);
        return true;
    }

}
