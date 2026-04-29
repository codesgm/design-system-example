#!/bin/bash
set -e

REGION="us-east-2"
QUEUE_NAME="import-jobs"

awslocal sqs create-queue \
    --queue-name ${QUEUE_NAME} \
    --region ${REGION} \
    --attributes '{"VisibilityTimeout":"900"}' \
    2>/dev/null || true

awslocal sqs list-queues --region ${REGION}
echo "SQS init done"
