FROM alpine:3.5

RUN apk update
RUN apk add openjdk8
RUN apk add docker
RUN apk add py-pip
RUN apk add make
RUN apk add curl
RUN apk add python3
RUN pip install --upgrade pip
RUN pip3 install kubernetes

RUN curl -Lo minikube https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64 && chmod +x minikube && mv minikube /usr/local/bin/