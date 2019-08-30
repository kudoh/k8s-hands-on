# Tracing

## Jaeger

<https://www.jaegertracing.io/docs/1.13/operator/>
<https://hub.helm.sh/charts/stable/jaeger-operator>

```bash
helm upgrade jaeger-operator --install stable/jaeger-operator
kubectl apply -f jaeger.yaml

kubectl get deploy,pod,svc,ingress -l app.kubernetes.io/instance=frieza-jaeger
```

## api-gatway

```bash
npm install --save jaeger-client opentracing
```

## github-service

```build.gradle
implementation 'io.opentracing.contrib:opentracing-spring-jaeger-web-starter:2.0.3'
```

```application.yml
opentracing:
  jaeger:
    enabled: false
    service-name: github-servicve
    const-sampler:
      decision: true # always sampling for verification
    udp-sender:
      host: localhost
      port: 6831

# suppress stacktrace when noop tracer is enabled
logging:
  level:
    io.opentracing.contrib.tracerresolver.TracerResolver: error
```

## enabled tracing

```bash
# github-service
kubectl patch deploy github-service --patch "$(cat github-service_patch.yaml)"

# api-gateway
kubectl patch deploy api-gateway --patch "$(cat api-gateway_patch.yaml)"
```
