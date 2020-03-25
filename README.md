# login-oidc-customizer-test
https://spring.io/guides/tutorials/spring-boot-oauth2/ - with authorization request customizer

# setup
provide your GitHub app id and secret in `application.yml`

# proper flow 
with `WebConfig.USE_CUSTOMIZER` set to **false**
- run application
- open localhost:8080

app should
- redirect to GitHub login page
- log "attributes" containing the 'registration_id' entry

# bug replication 
with `WebConfig.USE_CUSTOMIZER` set to **true**
- run application
- open localhost:8080

app should
- fail to finish oidc logic
- throw `java.lang.IllegalArgumentException: registrationId cannot be empty`
- log empty "attributes"

the probable reason is that customizer replaces the `attributesConsumer` (capturing the attributes, then returning them with `putAll`) set by `DefaultOAuth2AuthorizationRequestResolver` in line 167 (`builder.attributes(attributes)`), thus losing the registration_id kept by replaced consumer 
 
