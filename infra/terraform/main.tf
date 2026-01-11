terraform {
  required_version = ">= 1.5.0"

  cloud {
    organization = "inisap"

    workspaces {
      name = "posfiap-feedback-lambda-avaliacao-report"
    }
  }

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}