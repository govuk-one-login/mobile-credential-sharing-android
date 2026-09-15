# Provision DVS reader certificate

> [!WARNING]
> This action provisions private key material into the **test verifier** app, which is for **non-production / internal use only**. Production verifier apps are built and maintained by separate teams that consume this SDK. Never use this action, or the app it provisions, in production.

This composite action provisions a short-lived DVS L4 reader-auth leaf certificate for the test verifier and writes it into the app's bundled assets.

## Flow

1. Generate an EC key pair and a PKCS#10 certificate signing request (CSR) with openssl.
2. Assume an AWS role using the workflow's GitHub OIDC token (`aws-actions/configure-aws-credentials`).
3. Send the CSR to the L3 CA `POST /issue-reader-cert` endpoint. The request is signed with AWS SigV4 by `curl` using the assumed credentials.
4. Write the issued certificate chain and the generated leaf private key into the environment's assets, then remove the working directory.

Only the L4 leaf private key is ever bundled. The CA private keys are never exposed by the endpoint.

## Inputs

| Input          | Required | Default                | Description                                       |
| -------------- | -------- | ---------------------- | ------------------------------------------------- |
| `environment`  | yes      |                        | `dev` or `integration`.                           |
| `endpoint-url` | yes      |                        | Base URL of the issuance API (no trailing slash). |
| `aws-role-arn` | yes      |                        | ARN of the AWS role to assume using OIDC.           |
| `aws-region`   | no       | `eu-west-2`            | AWS region hosting the endpoint.                  |
| `assets-dir`   | no       | `app/src/main/assets`  | Directory holding the bundled reader-auth assets. |
| `subject`      | no       | GDS distinguished name | Subject for the CSR.                              |
| `ec-curve`     | no       | `prime256v1`           | Named curve for the generated key pair.            |

The job that calls this action must grant `id-token: write` so the OIDC token can be minted.

## Written assets

For a given `environment` the action overwrites the committed placeholders:

```
reader_dvs_<environment>_chain.der       # issued chain, leaf first
reader_dvs_<environment>_leaf_key.pem    # generated leaf private key
```

## Certificate lifecycle

The issued leaf is short-lived (90 day validity), matching the DVS issuance flow. Provisioned app builds should be treated as deprecated once the leaf expires, and re-provisioned by re-running the pipeline. The action fails closed if the endpoint returns a non-200 response, an empty chain, or a leaf that is already expired, so a build cannot be published with unusable material.

## Example

```yaml
- name: Provision reader certificate
  uses: ./.github/actions/provision-reader-cert
  with:
    environment: integration
    endpoint-url: ${{ vars.READER_CERT_ENDPOINT_URL }}
    aws-role-arn: ${{ vars.READER_CERT_ROLE_ARN }}
```
