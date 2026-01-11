data "terraform_remote_state" "infra" {
  backend = "remote"

  config = {
    organization = "inisap"
    workspaces = {
      name = "posfiap-feedback-infra"
    }
  }
}
