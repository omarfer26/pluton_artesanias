package com.ufps.prueba.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        HttpSession session = request.getSession(false);
        
        if (request.getRequestURI().startsWith("/empleado")) {

            if (session == null || session.getAttribute("empleado") == null) {
                response.sendRedirect("/empleado/login");
                return false;
            }
        }
        
        if (request.getRequestURI().startsWith("/cliente/dashboard")
                || request.getRequestURI().startsWith("/cliente/pedido")) {

            if (session == null || session.getAttribute("cliente") == null) {
                response.sendRedirect("/cliente/login");
                return false;
            }
        }

        return true;
    }
}
