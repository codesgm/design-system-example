#!/bin/bash
set -e

BUCKET_NAME="imports-bucket"
REGION="us-east-2"

until awslocal s3 ls 2>/dev/null; do sleep 2; done

if ! awslocal s3 ls "s3://${BUCKET_NAME}" 2>/dev/null; then
    awslocal s3 mb "s3://${BUCKET_NAME}" --region ${REGION}
fi

awslocal s3 ls
echo "S3 init done"
