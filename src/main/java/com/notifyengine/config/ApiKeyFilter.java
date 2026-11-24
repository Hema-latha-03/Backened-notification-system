package com.notifyengine.config;

import com.notifyengine.model.Client;
import com.notifyengine.model.Status;
import com.notifyengine.repository.ClientRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * /api/v1/**    -> needs header X-API-KEY (an ACTIVE client)
 * /api/admin/** -> needs header X-ADMIN-KEY
 */
@Component
public class ApiKeyFilter extends OncePerRequestFilter {
    private final ClientRepository clients;

    @Value("${notification.admin-key}")
    private String adminKey;

    public ApiKeyFilter(ClientRepository clients) {
        this.clients = clients;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String path = req.getRequestURI();

        if (path.startsWith("/api/admin")) {
            if (!adminKey.equals(req.getHeader("X-ADMIN-KEY"))) {
                reject(res, "Invalid or missing X-ADMIN-KEY");
                return;
            }
        } else if (path.startsWith("/api/v1")) {
            String key = req.getHeader("X-API-KEY");
            Optional<Client> client = (key == null || key.isBlank())
                    ? Optional.empty()
                    : clients.findByApiKeyAndStatus(key, Status.ACTIVE);
            if (client.isEmpty()) {
                reject(res, "Invalid or missing X-API-KEY");
                return;
            }
            req.setAttribute("client", client.get());
        }
        chain.doFilter(req, res);
    }

    private void reject(HttpServletResponse res, String message) throws IOException {
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.setContentType("application/json");
        res.getWriter().write("{\"status\":401,\"error\":\"" + message + "\"}");
    }
}
