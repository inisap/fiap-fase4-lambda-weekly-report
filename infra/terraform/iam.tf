# =========================
# IAM Role da Lambda
# =========================
resource "aws_iam_role" "lambda_exec" {
  name = "avaliacao-relatorio-feedback-lambda-role-${var.environment}"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Principal = {
        Service = "lambda.amazonaws.com"
      }
      Action = "sts:AssumeRole"
    }]
  })

  tags = {
    Application = "avaliacao-relatorio-feedback"
    Environment = var.environment
  }
}

# =========================
# CloudWatch Logs
# =========================
resource "aws_iam_role_policy_attachment" "basic_logs" {
  role       = aws_iam_role.lambda_exec.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

# =========================
# Policy específica do projeto
# =========================
resource "aws_iam_role_policy" "lambda_policy" {
  name = "avaliacao-relatorio-feedback-policy-${var.environment}"
  role = aws_iam_role.lambda_exec.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [

      # DynamoDB
      {
        Effect = "Allow"
        Action = [
          "dynamodb:GetItem",
          "dynamodb:Query"
        ]
        Resource = data.terraform_remote_state.infra.outputs.dynamodb_avaliacoes_arn
      },

      # S3
      {
        Effect = "Allow"
        Action   = "s3:PutObject"
        Resource = "${aws_s3_bucket.relatorios.arn}/*"
      },

      #SQS
      {
        Effect = "Allow"
        Action = [
          "sqs:ReceiveMessage",
          "sqs:DeleteMessage",
          "sqs:GetQueueAttributes",
          "sqs:ChangeMessageVisibility"
        ]
        Resource = aws_sqs_queue.weekly_report.arn
      }
    ]
  })
}

