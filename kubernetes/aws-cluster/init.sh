#!/bin/sh

kube-aws init \
--cluster-name=howami \
--external-dns-name=cluster.howami.danielsomerfield.com \
--hosted-zone-id=ZC5DML8FUHLLJ \
--region=us-west-2 \
--availability-zone=us-west-2c \
--key-name=kube-aws \
--kms-key-arn="arn:aws:kms:us-west-2:125558680652:key/fc59a9a9-627e-4803-bb9d-2fb63a75a9bd"