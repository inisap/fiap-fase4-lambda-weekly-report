# =========================
# EventBridge Rule para todos domingos a 5 da manha
# =========================
resource "aws_cloudwatch_event_rule" "weekly_report_schedule" {
  name        = "weekly-report-schedule-${var.environment}"
  description = "Dispara o processamento semanal de avaliações"

  schedule_expression = "cron(0 8 ? * SUN *)"

  tags = {
    Application = "avaliacao-feedback"
    Environment = var.environment
  }
}

# =========================
# EventBridge → SQS Target
# =========================
resource "aws_cloudwatch_event_target" "weekly_report_target" {
  rule      = aws_cloudwatch_event_rule.weekly_report_schedule.name
  target_id = "SendToSQS"
  arn       = aws_sqs_queue.weekly_report.arn

  input = jsonencode({
    tipo      = "RELATORIO_SEMANAL"
    origem    = "eventbridge"
    timestamp = "<aws.events.event.ingestion-time>"
  })
}

# =========================
# ROle para Permissão do SQS
# =========================
resource "aws_sqs_queue_policy" "allow_eventbridge" {
  queue_url = aws_sqs_queue.weekly_report.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Principal = {
        Service = "events.amazonaws.com"
      }
      Action   = "sqs:SendMessage"
      Resource = aws_sqs_queue.weekly_report.arn
      Condition = {
        ArnEquals = {
          "aws:SourceArn" = aws_cloudwatch_event_rule.weekly_report_schedule.arn
        }
      }
    }]
  })
}
