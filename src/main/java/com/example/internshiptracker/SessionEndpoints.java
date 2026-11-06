package com.example.internshiptracker;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.web.csrf.CsrfToken;

@RestController
public class SessionEndpoints {
    @GetMapping(value="/login", produces="text/html") public String login(CsrfToken token) {
        return """
            <!doctype html><html lang="en"><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
            <title>Private Tracker — Sign in</title><style>body{font:18px system-ui;background:#f3f6fa;color:#173e3c;max-width:420px;margin:12vh auto;padding:24px}form{display:grid;gap:18px}input,button{font:inherit;padding:12px;border:1px solid #aac3c0;border-radius:8px}button{background:#23685f;color:white}label{display:grid;gap:6px}</style>
            <h1>Private Tracker</h1><p>Sign in to manage your applications.</p>
            <form action="/login" method="post"><label>Username<input name="username" autocomplete="username" required></label>
            <label>Password<input type="password" name="password" autocomplete="current-password" required></label>
            <input type="hidden" name="_csrf" value="%s"><button>Sign in</button></form>
            <p>Use your owner account. Muse can use its separately authorized access token.</p></html>
            """.formatted(token == null ? "" : token.getToken());
    }
    @GetMapping("/healthz") public Map<String,String> health() { return Map.of("status", "ok"); }
    @Value("${app.mode:local}") String mode;

    @GetMapping("/api/session") public Map<String,String> session(CsrfToken token) {
        Map<String,String> out = new HashMap<>();
        out.put("mode", mode);
        if (token != null) { out.put("headerName", token.getHeaderName()); out.put("token", token.getToken()); }
        return out;
    }
}
