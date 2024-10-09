package com.nttdata.AccountMs.service;

import com.nttdata.AccountMs.model.Cuenta;
import com.nttdata.AccountMs.model.TipoCuentaEnum;
import com.nttdata.AccountMs.model.CuentaRequest;
import com.nttdata.AccountMs.model.CuentaResponse;
import com.nttdata.AccountMs.repository.CuentaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CuentaService {
    @Autowired
    private CuentaRepository cuentaRepository;

    public CuentaResponse crearCuenta(CuentaRequest cuentaRequest) {
        Cuenta cuenta = new Cuenta();
        cuenta.setClienteId(cuentaRequest.getClienteId());
        cuenta.setTipoCuenta(convertirTipoCuenta(cuentaRequest.getTipoCuenta()));
        cuenta.setNumeroCuenta(cuentaRequest.getTipoCuenta().name());
        cuenta.setSaldo(cuenta.getSaldo()); // Cambiado aquí

        Cuenta savedCuenta = cuentaRepository.save(cuenta);
        return convertToResponse(savedCuenta);
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

        if (cuenta.getSaldo() < monto) {
            return false; // Fondos insuficientes
        }

        cuenta.setSaldo(cuenta.getSaldo() - monto); // Restar el monto del saldo
        cuentaRepository.save(cuenta);
        return true;
    }
}
