# ─── Network ─────────────────────────────────────────────
resource "docker_network" "ecommerce" {
  name = "ecommerce-network"
}

# ─── Docker Images ───────────────────────────────────────
resource "docker_image" "mysql" {
  name         = "mysql:8.0"
  keep_locally = true
}

resource "docker_image" "redis" {
  name         = "redis:7.2-alpine"
  keep_locally = true
}

resource "docker_image" "prometheus" {
  name         = "prom/prometheus:latest"
  keep_locally = true
}

resource "docker_image" "grafana" {
  name         = "grafana/grafana:latest"
  keep_locally = true
}

resource "docker_image" "zipkin" {
  name         = "openzipkin/zipkin:latest"
  keep_locally = true
}

resource "docker_image" "app" {
  name         = "ecommerce-app:latest"
  keep_locally = true
}

# ─── Volumes ─────────────────────────────────────────────
resource "docker_volume" "mysql_data" {
  name = "ecommerce-mysql-data"
}

resource "docker_volume" "redis_data" {
  name = "ecommerce-redis-data"
}

resource "docker_volume" "prometheus_data" {
  name = "ecommerce-prometheus-data"
}

resource "docker_volume" "grafana_data" {
  name = "ecommerce-grafana-data"
}

# ─── MySQL ───────────────────────────────────────────────
resource "docker_container" "mysql" {
  name  = "ecommerce-mysql"
  image = docker_image.mysql.image_id

  env = [
    "MYSQL_ROOT_PASSWORD=${var.mysql_root_password}",
    "MYSQL_DATABASE=${var.mysql_database}",
    "MYSQL_USER=${var.mysql_user}",
    "MYSQL_PASSWORD=${var.mysql_password}",
  ]

  ports {
    internal = 3306
    external = 3306
  }

  volumes {
    volume_name    = docker_volume.mysql_data.name
    container_path = "/var/lib/mysql"
  }

  networks_advanced {
    name = docker_network.ecommerce.name
  }

  restart = "unless-stopped"

  healthcheck {
    test     = ["CMD", "mysqladmin", "ping", "-h", "localhost", "-uroot", "-p${var.mysql_root_password}"]
    interval = "10s"
    timeout  = "5s"
    retries  = 5
  }
}

# ─── Redis ───────────────────────────────────────────────
resource "docker_container" "redis" {
  name  = "ecommerce-redis"
  image = docker_image.redis.image_id

  command = ["redis-server", "--appendonly", "yes", "--requirepass", var.redis_password]

  ports {
    internal = 6379
    external = 6379
  }

  volumes {
    volume_name    = docker_volume.redis_data.name
    container_path = "/data"
  }

  networks_advanced {
    name = docker_network.ecommerce.name
  }

  restart = "unless-stopped"

  healthcheck {
    test     = ["CMD", "redis-cli", "--raw", "incr", "ping"]
    interval = "10s"
    timeout  = "5s"
    retries  = 5
  }
}

# ─── Prometheus ──────────────────────────────────────────
resource "docker_container" "prometheus" {
  name  = "ecommerce-prometheus"
  image = docker_image.prometheus.image_id

  command = [
    "--config.file=/etc/prometheus/prometheus.yml",
    "--storage.tsdb.path=/prometheus",
    "--web.enable-lifecycle",
  ]

  ports {
    internal = 9090
    external = var.prometheus_port
  }

  volumes {
    host_path      = abspath("${path.module}/../monitoring/prometheus/prometheus.yml")
    container_path = "/etc/prometheus/prometheus.yml"
  }

  volumes {
    volume_name    = docker_volume.prometheus_data.name
    container_path = "/prometheus"
  }

  networks_advanced {
    name = docker_network.ecommerce.name
  }

  restart = "unless-stopped"

  depends_on = [docker_container.mysql, docker_container.redis]
}

# ─── Grafana ─────────────────────────────────────────────
resource "docker_container" "grafana" {
  name  = "ecommerce-grafana"
  image = docker_image.grafana.image_id

  env = [
    "GF_SECURITY_ADMIN_USER=admin",
    "GF_SECURITY_ADMIN_PASSWORD=admin",
    "GF_USERS_ALLOW_SIGN_UP=false",
  ]

  ports {
    internal = 3000
    external = var.grafana_port
  }

  volumes {
    volume_name    = docker_volume.grafana_data.name
    container_path = "/var/lib/grafana"
  }

  volumes {
    host_path      = abspath("${path.module}/../monitoring/grafana/provisioning")
    container_path = "/etc/grafana/provisioning"
  }

  volumes {
    host_path      = abspath("${path.module}/../monitoring/grafana/dashboards")
    container_path = "/var/lib/grafana/dashboards"
  }

  networks_advanced {
    name = docker_network.ecommerce.name
  }

  restart = "unless-stopped"

  depends_on = [docker_container.prometheus]
}

# ─── Zipkin ──────────────────────────────────────────────
resource "docker_container" "zipkin" {
  name  = "ecommerce-zipkin"
  image = docker_image.zipkin.image_id

  env = [
    "STORAGE_TYPE=mem",
  ]

  ports {
    internal = 9411
    external = var.zipkin_port
  }

  networks_advanced {
    name = docker_network.ecommerce.name
  }

  restart = "unless-stopped"

  healthcheck {
    test     = ["CMD", "wget", "--quiet", "--tries=1", "--spider", "http://localhost:9411/health"]
    interval = "10s"
    timeout  = "5s"
    retries  = 5
  }
}

# ─── Spring Boot App ────────────────────────────────────
resource "docker_container" "app" {
  name  = "ecommerce-app"
  image = docker_image.app.image_id

  env = [
    "SPRING_PROFILES_ACTIVE=docker",
    "DB_USERNAME=${var.mysql_user}",
    "DB_PASSWORD=${var.mysql_password}",
    "SPRING_DATASOURCE_URL=jdbc:mysql://ecommerce-mysql:3306/${var.mysql_database}?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
    "SPRING_DATASOURCE_USERNAME=${var.mysql_user}",
    "SPRING_DATASOURCE_PASSWORD=${var.mysql_password}",
    "SPRING_DATA_REDIS_HOST=ecommerce-redis",
    "SPRING_DATA_REDIS_PORT=6379",
    "SPRING_DATA_REDIS_PASSWORD=${var.redis_password}",
    "REDIS_HOST=ecommerce-redis",
    "REDIS_PORT=6379",
    "REDIS_PASSWORD=${var.redis_password}",
    "MYSQL_HOST=ecommerce-mysql",
    "MYSQL_PORT=3306",
    "MANAGEMENT_ZIPKIN_TRACING_ENDPOINT=http://ecommerce-zipkin:9411/api/v2/spans",
  ]

  ports {
    internal = 8080
    external = var.app_port
  }

  networks_advanced {
    name = docker_network.ecommerce.name
  }

  restart = "unless-stopped"

  depends_on = [docker_container.mysql, docker_container.redis]

  healthcheck {
    test         = ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
    interval     = "30s"
    timeout      = "10s"
    retries      = 5
    start_period = "60s"
  }
}
