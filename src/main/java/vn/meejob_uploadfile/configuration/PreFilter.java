    package vn.meejob_uploadfile.configuration;

    import jakarta.servlet.FilterChain;
    import jakarta.servlet.ServletException;
    import jakarta.servlet.http.HttpServletRequest;
    import jakarta.servlet.http.HttpServletResponse;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.apache.commons.lang3.StringUtils;
    import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
    import org.springframework.security.core.authority.SimpleGrantedAuthority;
    import org.springframework.security.core.context.SecurityContextHolder;
    import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
    import org.springframework.stereotype.Component;
    import org.springframework.web.filter.OncePerRequestFilter;
    import vn.meejob_uploadfile.service.JwtService;
    import vn.meejob_uploadfile.service.RedisService;

    import java.io.IOException;
    import java.util.List;

    @Component
    @Slf4j
    @RequiredArgsConstructor
    public class PreFilter extends OncePerRequestFilter {

        private final JwtService jwtService;
        private final RedisService redisService;
        private static final String AT_BLACKLIST_PREFIX = "BLACKLIST_ACCESS_TOKEN:";

        @Override
        protected void doFilterInternal(HttpServletRequest request,
                                        HttpServletResponse response,
                                        FilterChain filterChain) throws ServletException, IOException {

            String authHeader = request.getHeader("Authorization");
            String path = request.getRequestURI();

            // Bỏ qua swagger
            if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")) {
                filterChain.doFilter(request, response);
                return;
            }

            if (StringUtils.isBlank(authHeader) || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = authHeader.substring(7);

            try {
                String username = jwtService.extractUsername(token);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    Object blacklistedToken = redisService.get(AT_BLACKLIST_PREFIX + username);
                    if (token.equals(blacklistedToken)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token has been revoked");
                        return;
                    }

                    String role = jwtService.extractRole(token);
                    List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }

            } catch (Exception e) {
                log.error("JWT validation failed: {}", e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                return;
            }

            filterChain.doFilter(request, response);
        }
    }
