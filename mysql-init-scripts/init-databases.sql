-- ./mysql-init-scripts/init-databases.sql

CREATE DATABASE IF NOT EXISTS keycloak CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'keycloak_user'@'%' IDENTIFIED BY 'keycloak_user';
GRANT ALL PRIVILEGES ON keycloak.* TO 'keycloak_user'@'%';

CREATE DATABASE IF NOT EXISTS profile_service CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'profile_service_user'@'%' IDENTIFIED BY 'profile_service_user';
GRANT ALL PRIVILEGES ON profile_service.* TO 'profile_service_user'@'%';

FLUSH PRIVILEGES;