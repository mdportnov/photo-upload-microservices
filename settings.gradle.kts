rootProject.name = "photocloud-microservice"

include(
    "maven-zuul-server",
    "auth-service",
    "eureka-server",
    "image-service"
)