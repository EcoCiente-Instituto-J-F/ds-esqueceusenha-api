package br.com.ecociente.senha.config.security;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

    if (authorization == null || !authorization.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = authorization.substring(7);
    try{
      if (jwtService.isTokenValido(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
        Claims claims = jwtService.extrairTodosClaims(token);
        String email = claims.getSubject();
        String perfil = claims.get("perfil",String.class);

        var authorities = perfil != null ? List.of(new SimpleGrantedAuthority("ROLE_"+perfil)) :List.<SimpleGrantedAuthority>of();
        var principal = User.builder()
          .username(email)
          .password("")
          .authorities(authorities)
          .build();
        
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(principal, null, authorities);
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
      }
    } catch(Exception e){
      SecurityContextHolder.clearContext();
    }    

    filterChain.doFilter(request, response);
  }
}

