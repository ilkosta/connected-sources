rootProject.name = "backend"
include(//"backend-api",
    "backend-shared",
    "backend-core:backend-core-user",
    "backend-core:backend-core-content",
    "backend-tenant-api",
    "backend-tenant-fs-impl",
    "backend-tenant-db-impl",
    "backend-starter",
    "backend-logging-api",
    "backend-logging-fs-impl",
//    "backend-notification",
    "backend-infra-fs")