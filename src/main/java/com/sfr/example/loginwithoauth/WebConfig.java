package com.sfr.example.loginwithoauth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI;

@Configuration
public class WebConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests((requests) -> requests.anyRequest().authenticated());
        http.oauth2Login(Customizer.withDefaults());
        http.oauth2Login().authorizationEndpoint().authorizationRequestResolver(resolver());
        http.oauth2Client();
    }

    private OAuth2AuthorizationRequestResolver resolver() {
        DefaultOAuth2AuthorizationRequestResolver delegate = new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, DEFAULT_AUTHORIZATION_REQUEST_BASE_URI);

        // if customizer is set, attributes logged (in line 52) will not contain OAuth2ParameterNames.REGISTRATION_ID
        // delegate.setAuthorizationRequestCustomizer(builder -> builder.attributes(map -> map.put("test", "value")));

        return new AttributesLoggingResolver(delegate);
    }

    static class AttributesLoggingResolver implements OAuth2AuthorizationRequestResolver {
        private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AttributesLoggingResolver.class);
        private final OAuth2AuthorizationRequestResolver delegate;

        public AttributesLoggingResolver(OAuth2AuthorizationRequestResolver delegate) {
            this.delegate = delegate;
        }

        @Override
        public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
            OAuth2AuthorizationRequest result = delegate.resolve(request);
            if (result != null) {
                log.info("attributes: {}", result.getAttributes().keySet());
            }
            return result;
        }

        @Override
        public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
            return delegate.resolve(request, clientRegistrationId);
        }
    }

}
