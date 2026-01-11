variable "aws_region" {
  description = "Região AWS"
  type        = string
  default     = "us-east-1"
}

variable "environment" {
  description = "Ambiente (dev, hml, prod)"
  type        = string
}

variable "sns_topic_name" {
  description = "Nome do tópico SNS"
  type        = string
}

variable "dynamodb_table_name" {
  description = "Nome da tabela DynamoDB"
  type        = string
}

variable "sqs_queue_name" {
  description = "Nome da fila SQS principal"
  type        = string
}

variable "relatorios_bucket_name" {
  description = "Nome do bucket S3 para armazenar os relatórios semanais"
  type        = string
}

