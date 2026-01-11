# =========================
# SQS DLQ
# =========================
resource "aws_sqs_queue" "weekly_report_dlq" {
  name = "${var.sqs_queue_name}-dlq"

  message_retention_seconds = 1209600 # 14 dias

  tags = {
    Application = "avaliacao-feedback"
    Environment = var.environment
    Type        = "dlq"
  }
}

# =========================
# SQS
# =========================
resource "aws_sqs_queue" "weekly_report" {
  name = var.sqs_queue_name

  visibility_timeout_seconds = 60
  message_retention_seconds  = 345600 # 4 dias

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.weekly_report_dlq.arn
    maxReceiveCount     = 5
  })

  tags = {
    Application = "avaliacao-feedback"
    Environment = var.environment
  }
}
