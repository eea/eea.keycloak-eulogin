FROM jboss/keycloak:5.0.0

ARG project_version=5.0.0

COPY target/keycloak-cas-services-eulogin-$project_version.jar /opt/jboss/keycloak/standalone/deployments/keycloak-cas-services-eulogin-$project_version.jar
