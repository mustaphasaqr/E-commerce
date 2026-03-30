variable "mysql_root_password" {
  description = "MySQL root password"
  type        = string
  default     = "root"
  sensitive   = true
}

variable "mysql_database" {
  description = "MySQL database name"
  type        = string
  default     = "ecommerce"
}

variable "mysql_user" {
  description = "MySQL application user"
  type        = string
  default     = "ecommerce_user"
}

variable "mysql_password" {
  description = "MySQL application user password"
  type        = string
  default     = "ecommerce_pass"
  sensitive   = true
}

variable "redis_password" {
  description = "Redis password"
  type        = string
  default     = "ecommerce_redis_pass"
  sensitive   = true
}

variable "app_port" {
  description = "Port to expose the Spring Boot app"
  type        = number
  default     = 8080
}

variable "grafana_port" {
  description = "Port to expose Grafana"
  type        = number
  default     = 3000
}

variable "prometheus_port" {
  description = "Port to expose Prometheus"
  type        = number
  default     = 9090
}

variable "zipkin_port" {
  description = "Port to expose Zipkin"
  type        = number
  default     = 9411
}
