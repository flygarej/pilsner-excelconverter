Commands to build docker image and push to docker hub.

```
docker build -f src/main/docker/Dockerfile.jvm -t jflygare/pilsnerconverter:VERSION .

docker push jflygare/pilsnerconverter:VERSION
```