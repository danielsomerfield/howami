#!/usr/bin/env python3
import argparse
from builtins import len
from kubernetes import config
from kubernetes.client import api_client
from kubernetes.client.apis import batch_v1_api, core_v1_api
from time import sleep

IMAGE_NAME = 'danielsomerfield/howami-e2e:'
JOB_MANIFEST = {
    'apiVersion': 'batch/v1',
    'kind': 'Job',
    'metadata': {
        'name': 'smoke',
        'group': 'howami-all'
    },
    'spec': {
        'template': {
            'metadata': {'labels':

                {
                    'app': 'smoke',
                    'group': 'howami-all'
                }
            },
            'spec': {
                'containers': [
                    {
                        'name': 'e2e',
                     # 'image': 'danielsomerfield/howami-e2e:dev-1519001136'
                     }
                ],

                'restartPolicy': 'Never'
            },
            'backoffLimit': 4
        }
    }
}


def delete_job(client):
    batch_api = batch_v1_api.BatchV1Api(client)
    core_api = core_v1_api.CoreV1Api(client)
    delete_job_retry(batch_api)
    delete_pod_retry(core_api)


def delete_pod_retry(core_api):
    while (len(get_pods(core_api)) > 0):
        sleep(2)
        core_api.delete_collection_namespaced_pod(
            namespace='default',
            label_selector='job-name=smoke'
        )


def get_pods(core_api):
    items = core_api.list_namespaced_pod(namespace='default', label_selector='job-name=smoke').items
    return items


def delete_job_retry(batch_api):
    while (len(get_jobs(batch_api)) > 0):
        sleep(2)
        batch_api.delete_collection_namespaced_job(
            namespace='default',
            label_selector='job-name=smoke'
        )


def get_jobs(batch_api):
    items = batch_api.list_namespaced_job(namespace='default', label_selector='job-name=smoke').items
    return items


def run_new_job(client, version):
    batch_api = batch_v1_api.BatchV1Api(client)

    JOB_MANIFEST['spec']['template']['spec']['containers'][0]['image'] = IMAGE_NAME + version
    batch_api.create_namespaced_job(
        namespace="default",
        body=JOB_MANIFEST
    )

    wait_for_job(batch_api)


def wait_for_job(batch_api):
    running = False
    while not running:

        status = batch_api.read_namespaced_job_status(namespace='default', name='smoke')
        if not status.status.start_time:
            print("Waiting for job to run")
            sleep(2)
        else:
            print("Job started")
            running = True


def run():
    args = parse_args()

    config.load_kube_config()
    client = api_client.ApiClient()

    delete_job(client)
    run_new_job(client, args.version)


def parse_args():
    parser = argparse.ArgumentParser()
    parser.add_argument('--version', required=True)
    return parser.parse_args()


run()
