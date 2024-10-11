package com.nttdata.AccountMs.controller;

import com.nttdata.AccountMs.api.AccountsApiDelegate;
import com.nttdata.AccountMs.model.CuentaRequest;
import com.nttdata.AccountMs.model.CuentaResponse;
import com.nttdata.AccountMs.model.TransaccionRequest;
import com.nttdata.AccountMs.service.CuentaService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("cuentas")
public class CuentaController implements AccountsApiDelegate {

    @Autowired
    private CuentaService cuentaService;

    @Override
    @PostMapping
    public ResponseEntity<CuentaResponse> crearCuenta(@Valid @RequestBody CuentaRequest cuentaRequest) {
        CuentaResponse response = cuentaService.crearCuenta(cuentaRequest);
        return ResponseEntity.status(201).body(response);
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<CuentaResponse> obtenerCuenta(@PathVariable Integer id) {
        return cuentaService.obtenerCuenta(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCuenta(@PathVariable Integer id) {
        boolean eliminado = cuentaService.eliminarCuenta(id); // Lógica para eliminar la cuenta
        if (eliminado) {
            return ResponseEntity.noContent().build(); // 204 No Content
        } else {
            return ResponseEntity.notFound().build(); // 404 Not Found
        }
    }

    @Override
    @GetMapping
    public ResponseEntity<List<CuentaResponse>> listarCuentas() {
        List<CuentaResponse> cuentas = cuentaService.listarCuentas();
        return ResponseEntity.ok(cuentas);
    }


    @PutMapping("/{cuentaId}/depositar")
    public ResponseEntity<Void> depositar(@PathVariable Integer cuentaId, @RequestBody TransaccionRequest transaccionRequest) {
        if (transaccionRequest.getMonto() <= 0) {
            return ResponseEntity.badRequest().build();
        }
        cuentaService.depositar(cuentaId, transaccionRequest.getMonto());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{cuentaId}/retirar")
    public ResponseEntity<Void> retirar(@PathVariable Integer cuentaId, @RequestBody TransaccionRequest transaccionRequest) {
        if (transaccionRequest.getMonto() <= 0) {
            return ResponseEntity.badRequest().build();
        }
        boolean result = cuentaService.retirar(cuentaId, transaccionRequest.getMonto());
        if (!result) {
            return ResponseEntity.badRequest().build(); // Sin cuerpo de respuesta
        }
        return ResponseEntity.ok().build();
    }
}