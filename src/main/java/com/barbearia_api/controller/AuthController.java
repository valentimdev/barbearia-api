package com.barbearia_api.controller;

import com.barbearia_api.dto.auth.LoginDto;
import com.barbearia_api.model.Usuario;
import com.barbearia_api.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDto dto) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getSenha()));

        Usuario usuario = (Usuario) auth.getPrincipal();
        String token = jwtUtil.generateToken(usuario);

        Map<String, Object> responseData = Map.of(
                "token", token,
                "userId", usuario.getId(),
                "role", usuario.getTipoPerfil().name()
        );
        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/util/hash-password")
    public ResponseEntity<?> getHashedPassword(@RequestParam String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "O parâmetro 'plainPassword' é obrigatório e não pode ser vazio."));
        }
        String hashedPassword = passwordEncoder.encode(plainPassword.trim());
        return ResponseEntity.ok(Map.of(
                "plainPassword", plainPassword,
                "hashedPassword", hashedPassword
        ));
    }
}