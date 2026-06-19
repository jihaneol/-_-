rootProject.name = "card-service"

include(
    "domain",
    "application",
    "bootstrap",
    "batch",
    "infra",
    "external",
)

project(":domain").projectDir = file("modules/domain")
project(":application").projectDir = file("modules/application")
project(":bootstrap").projectDir = file("modules/bootstrap")
project(":batch").projectDir = file("modules/batch")
project(":infra").projectDir = file("modules/infra")
project(":external").projectDir = file("modules/external")
