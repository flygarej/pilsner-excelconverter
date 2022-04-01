You can build and deploy to kubernetes.
Change the container-image and kubernetes properties in application.properties as 
needed, create the required namespace in your k8s, then build with the command

```
quarkus build -Dquarkus.container-image.push=true
quarkus build -Dquarkus.kubernetes.deploy=true
```

Caveat: if you need to redeploy you must remove the old workload and nodeport definition first!