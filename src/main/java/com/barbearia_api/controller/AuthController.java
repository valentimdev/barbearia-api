package com.barbearia_api.controller;

import com.barbearia_api.dto.auth.LoginDto;
import com.barbearia_api.model.Usuario;
import com.barbearia_api.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
// Importe a anotação CrossOrigin
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
// Adicione a anotação aqui para aplicar a todos os endpoints deste controller (/auth/login)
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    // Não é necessário @CrossOrigin aqui se já estiver na classe,
    // a menos que você queira uma configuração diferente para este endpoint específico.
    public ResponseEntity<?> login(@RequestBody LoginDto dto) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getSenha()));

        Usuario usuario = (Usuario) auth.getPrincipal();
        String token = jwtUtil.generateToken(usuario);

        return ResponseEntity.ok(Map.of("token", token));
    }
}