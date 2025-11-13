package ar.edu.utn.frba.dds.server;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.BadJWTException;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class KeycloakTokenVerifier {
  private final String issuer;
  private final ConfigurableJWTProcessor<SecurityContext> jwtProcessor;

  public KeycloakTokenVerifier(String issuerUrl) throws Exception {
    this.issuer = issuerUrl;
    this.jwtProcessor = new DefaultJWTProcessor<>();
    JWKSource<SecurityContext> jwkSource = new RemoteJWKSet<>(new URL(issuerUrl + "/protocol/openid-connect/certs"));
    JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, jwkSource);
    jwtProcessor.setJWSKeySelector(keySelector);
  }

  public boolean hasRealmRole(String token, String requiredRole) throws Exception {
    if (token == null || token.isEmpty()) {
      return false;
    }
    JWTClaimsSet claims;
    try {
      claims = jwtProcessor.process(token, null);
    } catch (BadJWTException e) {
      return false;
    }

    if (claims.getIssuer() == null || !claims.getIssuer()
                                             .startsWith(this.issuer)) {
      return false;
    }

    Object realmAccessObj = claims.getClaim("realm_access");
    if (realmAccessObj instanceof Map) {
      Map<?, ?> realmAccess = (Map<?, ?>) realmAccessObj;
      Object rolesObj = realmAccess.get("roles");
      if (rolesObj instanceof List) {
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) rolesObj;
        return roles.stream()
                    .anyMatch(r -> r.equalsIgnoreCase(requiredRole));
      }
    }

    Object resourceAccessObj = claims.getClaim("resource_access");
    if (resourceAccessObj instanceof Map) {
      Map<?, ?> resourceAccess = (Map<?, ?>) resourceAccessObj;
      for (Object clientData : resourceAccess.values()) {
        if (clientData instanceof Map) {
          Object rolesObj = ((Map<?, ?>) clientData).get("roles");
          if (rolesObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) rolesObj;
            if (roles.stream()
                     .anyMatch(r -> r.equalsIgnoreCase(requiredRole))) {
              return true;
            }
          }
        }
      }
    }

    return false;
  }
}
