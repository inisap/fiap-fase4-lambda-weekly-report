resource "aws_lambda_function" "avaliacao-relatorio" {
  function_name = "avaliacao-relatorio-feedback-${var.environment}"

  role = aws_iam_role.lambda_exec.arn

  runtime = "java17"
  handler = "io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler"

  architectures = ["x86_64"]

  s3_bucket = data.terraform_remote_state.infra.outputs.artifacts_bucket_name
  s3_key    = "avaliacao-report/v0.0.1/function.zip"

  memory_size = 512
  timeout     = 10

  environment {
    variables = {
      DYNAMODB_TABLE = data.terraform_remote_state.infra.outputs.dynamodb_avaliacoes_name
      SQS_QUEUE_URL  = aws_sqs_queue.weekly_report.url
      RELATORIO_BUCKET = aws_s3_bucket.relatorios.bucket
    }
  }
}

# SQS → Lambda trigger
resource "aws_lambda_event_source_mapping" "weekly_report_trigger" {
  event_source_arn = aws_sqs_queue.weekly_report.arn
  function_name    = aws_lambda_function.avaliacao-relatorio.arn

  batch_size                         = 10
  maximum_batching_window_in_seconds = 30
  enabled                            = true
}