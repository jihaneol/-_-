rootProject.name = "card-service"

include(
    "domain",
    "application",
    "admin-api",
    "shop-api",
    "batch",
    "infra",
    "external",
)

project(":domain").projectDir = file("modules/domain")
project(":application").projectDir = file("modules/application")
project(":admin-api").projectDir = file("modules/admin-api")
project(":shop-api").projectDir = file("modules/shop-api")
project(":batch").projectDir = file("modules/batch")
project(":infra").projectDir = file("modules/infra")
project(":external").projectDir = file("modules/external")
