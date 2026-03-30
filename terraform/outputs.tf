output "app_url" {
  description = "URL to access the Spring Boot application"
  value       = "http://localhost:${var.app_port}"
}

output "grafana_url" {
  description = "URL to access Grafana dashboards"
  value       = "http://localhost:${var.grafana_port}"
}

output "prometheus_url" {
  description = "URL to access Prometheus metrics"
  value       = "http://localhost:${var.prometheus_port}"
}

output "zipkin_url" {
  description = "URL to access Zipkin tracing"
  value       = "http://localhost:${var.zipkin_port}"
}

output "network_name" {
  description = "Docker network name"
  value       = docker_network.ecommerce.name
}
