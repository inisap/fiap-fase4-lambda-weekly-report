# =========================
# S3 Bucket - Relatórios
# =========================
resource "aws_s3_bucket" "relatorios" {
  bucket = var.relatorios_bucket_name

  tags = {
    Application = "avaliacao-feedback"
    Environment = var.environment
    Purpose     = "relatorios-semanais"
  }
}

# =========================
# Versionamento
# =========================
resource "aws_s3_bucket_versioning" "relatorios" {
  bucket = aws_s3_bucket.relatorios.id

  versioning_configuration {
    status = "Enabled"
  }
}

# =========================
# Bloqueio de acesso público
# =========================
resource "aws_s3_bucket_public_access_block" "relatorios" {
  bucket = aws_s3_bucket.relatorios.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# =========================
# Criptografia em repouso
# =========================
resource "aws_s3_bucket_server_side_encryption_configuration" "relatorios" {
  bucket = aws_s3_bucket.relatorios.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}
