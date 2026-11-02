# Phase 8: Cloud-Native Delivery and Operations (Weeks 47–55)

Goal: from `git push` to a running, observable, recoverable system, and the skills to debug it when it breaks.

Setup once: kind (or minikube), kubectl, Helm, Terraform, an AWS account with budget alerts at a low limit, GitHub Actions on the ShopFlow repo. **Cloud hygiene:** tag everything, set a budget alarm before creating resources, and `terraform destroy` at the end of every session.

Conventions in this file: steps are time-boxed and sum to the session length. Commands marked *(verify against current docs)* depend on tool versions. Keep a `phase8-notes.md` in the repo for the "write from memory" steps.

---

## Week 47 — Containers for Java
**Outcome:** small, secure, well-behaved images.
**Before you start:** Docker running; ShopFlow services build with `./mvnw -q package`; a Trivy install (`brew install trivy` or the container image).

### S1 Learn (3h): image anatomy and the JVM in a container
1. (20m) Read the Docker docs "Best practices for writing Dockerfiles": layers, build cache, `.dockerignore`, multi-stage. Run `docker history <any image>` and match each line to a layer.
2. (40m) Read the Spring Boot reference "Efficient Container Images". Note the three options: Dockerfile with layered JAR, Buildpacks, Jib. Find the command that extracts layers (`java -Djarmode=tools -jar app.jar extract --layers` on recent Boot; `-Djarmode=layertools` on older) *(verify against current docs)*.
3. (30m) Read the Jib README: what it builds without a Docker daemon, how it layers dependencies vs classes.
4. (30m) Read about JVM container ergonomics: the JVM sizes the heap from the container memory limit; default `MaxRAMPercentage` is 25. Find where the flags `-XX:MaxRAMPercentage`, `-XX:+ExitOnOutOfMemoryError` and `-XX:+UseContainerSupport` are explained.
5. (30m) Read the Trivy docs: scanning an image (`trivy image`), scanning a filesystem (`trivy fs .`), severity filters, exit codes (`--exit-code 1 --severity HIGH,CRITICAL`).
6. (30m) From memory, draw in `phase8-notes.md` the layers of a multi-stage Java image (build stage vs runtime stage, what is copied between them). Compare with step 1.

**Tips and common problems**
- Reading takes the whole session → you skipped the commands → run `docker history` and `trivy image alpine:3.19` at least once; they take 2 minutes.
- Old blog posts use `layertools` or `FROM openjdk` → those images and flags changed → trust the current Boot docs and use `eclipse-temurin` images.
- `.dockerignore` missing → build context is hundreds of MB → add `target/`, `.git/`, `node_modules/`.

**Done when**
- [ ] You can explain in two sentences why dependency layers go before the application classes.
- [ ] `phase8-notes.md` has the multi-stage diagram drawn from memory.
- [ ] `trivy image` ran once and you can read a finding (package, installed version, fixed version).

### S2 Build (3h): SF-36 container images
1. (15m) `git switch -c sf-36-images`. Make sure `docker info` works.
2. (45m) Write `Dockerfile` for Orders, then copy for the others:
```dockerfile
FROM eclipse-temurin:25-jdk AS build
WORKDIR /src
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN ./mvnw -q -B dependency:go-offline
COPY src src
RUN ./mvnw -q -B package -DskipTests
RUN java -Djarmode=tools -jar target/*.jar extract --layers --destination /layers

FROM eclipse-temurin:25-jre
RUN useradd --system --uid 10001 app
WORKDIR /app
COPY --from=build /layers/dependencies/ ./
COPY --from=build /layers/spring-boot-loader/ ./
COPY --from=build /layers/snapshot-dependencies/ ./
COPY --from=build /layers/application/ ./
USER 10001
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70 -XX:+ExitOnOutOfMemoryError"
ENTRYPOINT ["java","-jar","app.jar"]
```
   (Adjust the `COPY` lines to the layout your extract command produces. *verify against current docs*.)
3. (30m) `docker build -t shopflow/orders:dev .` then `docker images shopflow/orders` (target: under 250 MB) and `docker history shopflow/orders:dev`. Change one class and rebuild: only the `application` layer should rebuild.
4. (30m) Add graceful shutdown to `application.yml`: `server.shutdown: graceful` and `spring.lifecycle.timeout-per-shutdown-phase: 30s`. Check the container user: `docker run --rm shopflow/orders:dev id` should print uid 10001.
5. (30m) `trivy image --severity HIGH,CRITICAL --exit-code 1 shopflow/orders:dev`. Fix findings by bumping the base image or dependency; if one has no fix, record it in `docs/security-exceptions.md` with a review date.
6. (30m) Write `compose.yaml` that starts the DB, Kafka, and all four services with health checks and `depends_on: condition: service_healthy`. Run `docker compose up --build`.

**Tips and common problems**
- Build downloads dependencies every time → `COPY src` before `dependency:go-offline` busts the cache → copy `pom.xml` first.
- `exec format error` on an M-series Mac → image built for the wrong architecture → build with `--platform linux/amd64` only when needed for CI parity; otherwise stay native.
- Container exits immediately with `Permission denied` → app writes to a read-only path as non-root → write to `/tmp` or a mounted volume.
- Compose services start before Postgres is ready → `depends_on` without health check → add `healthcheck` with `pg_isready`.

**Done when**
- [ ] Each service image is under 250 MB, runs as non-root, and Trivy shows no unaddressed HIGH/CRITICAL.
- [ ] `docker compose up` brings up the whole platform and `/actuator/health` is UP for each service.
- [ ] Rebuilding after a one-class change reuses the dependency layers.

### S3 Lab (2h): RL-20 heap in containers, RL-47 graceful shutdown
1. (10m) Add `/lab/memory` (returns `Runtime.getRuntime().maxMemory()` in MB) and `/lab/slow` (sleeps 10 s then returns 200) to a lab-only profile.
2. (20m) **RL-20 trigger:** `docker run --rm -m 512m -p 8080:8080 -e JAVA_TOOL_OPTIONS= shopflow/orders:dev`. Call `/lab/memory`. **Observe:** about 128 MB (25% of 512). Confirm: `docker run --rm -m 512m eclipse-temurin:25-jre java -XX:+PrintFlagsFinal -version | grep -E 'MaxHeapSize|MaxRAMPercentage'`.
3. (15m) **RL-20 fix:** run with `-e JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70"`. **Observe:** about 358 MB. Explain in 3 sentences: heap vs total container memory, why not 100%, what else uses memory (metaspace, threads, direct buffers).
4. (25m) **RL-47 trigger:** with graceful shutdown disabled (`SERVER_SHUTDOWN=immediate`), run `curl -m 30 localhost:8080/lab/slow &` then `docker stop` the container within 2 s. **Observe:** curl fails with `Empty reply from server` or connection reset; container log shows no "Graceful shutdown" line.
5. (20m) **RL-47 fix:** set `SERVER_SHUTDOWN=graceful`, repeat. **Observe:** log lines `Commencing graceful shutdown. Waiting for active requests to complete` then `Graceful shutdown complete`; curl returns 200 after about 10 s. Note: `docker stop` waits 10 s by default before `SIGKILL`; use `docker stop -t 40`.
6. (20m) Add regression: a shell script `scripts/graceful-check.sh` that does steps 4–5 and fails if curl does not get 200. Write the mechanism in 3 sentences (SIGTERM → Spring stops accepting, drains, exits; SIGKILL cannot be handled).
7. (10m) Write ledger entries RL-20 and RL-47.

**Tips and common problems**
- `maxMemory` is not what you expect → `JAVA_TOOL_OPTIONS` already set in the image → override it explicitly in the run command.
- The container exits with code 143 → it received SIGTERM and exited normally → that is correct; 137 means SIGKILL.
- `docker stop` returns instantly and the request dies → app is PID 2 behind a shell script that does not forward signals → use `ENTRYPOINT ["java",...]` (exec form) or `exec java` in the script.

**Done when**
- [ ] You observed 128 MB vs about 358 MB heap and can say why.
- [ ] Graceful shutdown script passes; it fails if you flip the setting.
- [ ] Two ledger entries written.

### S4 Review + Write (2h)
1. (30m) Finish ledger entries RL-20 and RL-47 using the template, including the one-minute answer.
2. (30m) Write ADR-019 "Image build strategy (Jib vs Dockerfile)": context, options (Dockerfile layered, Jib, Buildpacks), decision, consequences. Record what you actually used and why.
3. (20m) Create 6 flashcards: heap sizing flags, exit codes 137/143, layer caching order, why non-root, what Trivy scans, `docker stop` timing.
4. (20m) Answer the self-check out loud, then open the answers.
5. (10m) Add the week-47 retro to `retro.md`; merge the SF-36 PR.
6. (10m) Skim next week's S1 reading list and install anything missing.

**Self-check:** Why use multi-stage builds? Why run as non-root? How does the JVM decide its heap in a container?
<details><summary>Answers</summary>

- Multi-stage keeps compilers, caches and sources out of the runtime image: smaller, faster to pull, fewer vulnerabilities.
- A compromised app running as root has far more power inside the container (and a better chance to escape); non-root limits the blast radius.
- The JVM reads the cgroup memory limit and sets max heap to `MaxRAMPercentage` (default 25%) of it; you tune the percentage, leaving room for metaspace, thread stacks and direct memory.
</details>

**Output:** SF-36, ADR-019, RL-20/47 entries.
**If stuck:** run `docker run --rm -it --entrypoint sh <image>` and inspect the filesystem and user; most image problems are visible there.

---

## Week 48 — CI/CD pipelines
**Outcome:** every push is built, tested, scanned and published automatically.
**Before you start:** the ShopFlow repo is on GitHub with branch protection from week 1; images build locally from week 47.

### S1 Learn (3h): pipelines, gates and flow metrics
1. (30m) Read GitHub Actions docs "Understanding GitHub Actions". Write down the vocabulary: workflow, event, job, step, runner, action, artifact, cache.
2. (30m) Read "Workflow syntax": `on`, `jobs.<id>.needs`, `strategy.matrix`, `permissions`, `concurrency`, `if`. Note which keys you will use.
3. (40m) Read *Accelerate*: the chapter on the four key metrics. Write the four DORA metrics and how you would measure each for ShopFlow.
4. (30m) Read trunkbaseddevelopment.com: short-lived branches and feature flags. Compare with gitflow in 5 bullets.
5. (25m) Read PIT docs ("Quickstart") and JaCoCo's Maven plugin goals (`prepare-agent`, `report`, `check`). Note the difference between line coverage and mutation score.
6. (25m) Read OpenFeature's concepts page. From memory, draw the pipeline you will build (stages in order, where each gate sits).

**Tips and common problems**
- Reading YAML syntax feels abstract → keep a second tab with your repo and sketch the real workflow as you go.
- Confusing CI, continuous delivery and deployment → write one sentence for each in your notes.

**Done when**
- [ ] You can state the four DORA metrics and one way to measure each.
- [ ] Your pipeline diagram lists: build, test, coverage/mutation gate, scan, publish.

### S2 Build (3h): SF-37 CI pipeline
1. (15m) `git switch -c sf-37-ci`. Create `.github/workflows/ci.yml` with `on: [pull_request, push]` for `main`.
2. (40m) Build and test job:
```yaml
name: ci
on:
  pull_request:
  push:
    branches: [main]
permissions:
  contents: read
  packages: write
concurrency:
  group: ci-${{ github.ref }}
  cancel-in-progress: true
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: '25', cache: maven }
      - run: ./mvnw -B verify
```
   Pin action versions to current majors *(verify against current docs)*.
3. (30m) Make the Testcontainers tests run on the runner (Docker is preinstalled on `ubuntu-latest`). Push, watch the run. If it fails, read the first failing step log.
4. (30m) Add JaCoCo `check` (e.g., line coverage ≥ 0.80 on domain packages) and PIT (`mutationThreshold` of 60 to start) in `pom.xml`. Run `./mvnw verify` locally first.
5. (30m) Add a `scan` job: `trivy fs --exit-code 1 --severity HIGH,CRITICAL .`, then a `publish` job (`if: github.ref == 'refs/heads/main'`, `needs: [build, scan]`) that logs in to `ghcr.io` with `GITHUB_TOKEN` and runs `docker/build-push-action` per service.
6. (25m) In repo settings, set branch protection on `main`: require PR, required checks `build` and `scan`, no direct pushes.
7. (10m) Open the PR for this branch and watch all checks go green.

**Tips and common problems**
- Pipeline takes 12+ minutes → no dependency cache or sequential jobs → `cache: maven`, split lint/test/scan into parallel jobs.
- `permission denied` pushing to ghcr → `packages: write` missing or package visibility → set `permissions` and link the package to the repo.
- PIT is slow → it mutates every class → restrict `targetClasses` to the domain and application packages and use `threads`.
- Coverage gate is green but tests are weak → coverage measures execution, not assertions → that is what mutation testing adds.

**Done when**
- [ ] A PR triggers build, test, coverage/mutation, scan; main triggers publish.
- [ ] Merging is blocked when any required check fails (prove it with a red PR).
- [ ] Images appear in the GitHub package registry with a commit-SHA tag.

### S3 Lab (2h): make the pipeline fail on purpose (extra labs)
1. (10m) `git switch -c lab/ci-failures`. Open the Actions tab in a second window.
2. (25m) **Flaky test:** add a test that fails 1 in 3 runs (`new Random().nextInt(3) == 0`). Push five times. **Observe:** intermittent red. Fix by removing the randomness; if you must keep a known-flaky test, tag it `@Tag("quarantine")` and run it in a non-blocking job. Write the policy: a flaky test is fixed or quarantined within 2 days.
3. (20m) **Vulnerable dependency:** add `org.apache.logging.log4j:log4j-core:2.14.1` to a test scope. **Observe:** Trivy fails with a CRITICAL finding (Log4Shell, CVE-2021-44228) and a fixed version. Fix by upgrading; record the finding in the PR.
4. (15m) **Secret in code:** commit a fake AWS key (`AKIAIOSFODNN7EXAMPLE`). Add a secret-scanning step (gitleaks action or `gitleaks detect`) *(verify against current docs)*. **Observe:** the step fails. Remember: removing the commit is not enough, the real fix is to rotate the secret.
5. (40m) **Speed up 50%:** record the baseline duration from the Actions UI. Apply: Maven cache, parallel jobs, `-T 1C`, skip PIT on draft PRs, Docker layer cache for publish. Record before/after in `phase8-notes.md`.
6. (10m) Write ledger notes for each failure type: how it was caught, how to prevent it.

**Tips and common problems**
- Cache never hits → cache key changes every run → key on `hashFiles('**/pom.xml')`.
- You cannot reproduce CI failures locally → different JDK or timezone → pin the JDK and set `TZ=UTC` in both.

**Done when**
- [ ] Three failure types each produced a red check and you fixed them.
- [ ] Pipeline time decreased measurably; numbers recorded.

### S4 Review + Write (2h)
1. (30m) Draw the pipeline diagram (Mermaid in `docs/pipeline.md`): triggers, jobs, gates, artifacts.
2. (30m) Write ADR-020 "Branching and release strategy: trunk-based with feature flags": short-lived branches (< 1 day), flags for incomplete features, release by tag.
3. (20m) Write your ShopFlow DORA baseline: lead time (PR open → merge), deployment frequency, change failure rate (0 so far), time to restore (unknown yet).
4. (20m) Self-check out loud, then check the answers.
5. (10m) Retro entry. Merge SF-37.
6. (10m) Clean up the `lab/ci-failures` branch.

**Self-check:** What does a quality gate protect? Why prefer small frequent merges? What does mutation testing measure that coverage doesn't?
<details><summary>Answers</summary>

- A gate stops regressions or risky changes from reaching the next stage automatically, so quality does not depend on memory or goodwill.
- Small merges mean small diffs: easier review, less merge conflict, quicker detection of the change that broke something, easier rollback.
- Mutation testing changes the code and checks whether tests fail; coverage only shows lines were executed, not that assertions would catch a defect.
</details>

**Output:** SF-37, ADR-020, pipeline diagram.
**If stuck:** reproduce the failing step locally in the same container image (`docker run -v $PWD:/w -w /w ...`) before debugging YAML.

---

## Week 49 — Kubernetes core
**Outcome:** deploys and debugs workloads in a cluster.
**Before you start:** images from week 47 exist; install `kind` and `kubectl`; allocate at least 6 GB RAM to Docker.

### S1 Learn (3h): the objects you will use daily
1. (40m) Read Kubernetes docs "Concepts → Overview → Kubernetes Components". Draw control plane (API server, etcd, scheduler, controller manager) and node (kubelet, kube-proxy, runtime). Label what each does when you run `kubectl apply`.
2. (40m) Read Pods, ReplicaSets, Deployments (rolling update fields `maxSurge`, `maxUnavailable`). Read *Kubernetes in Action* early chapters on the same objects.
3. (30m) Read Services (ClusterIP/NodePort/LoadBalancer), ConfigMaps and Secrets. Note: Secrets are only base64, not encryption.
4. (30m) Read "Resource Management for Pods and Containers" and QoS classes (Guaranteed, Burstable, BestEffort).
5. (30m) Read Tasks → "Configure Liveness, Readiness and Startup Probes". Write one sentence on what Kubernetes does on each probe failure.
6. (10m) Practice 10 kubectl commands against a throwaway cluster: `kubectl run`, `get`, `describe`, `logs`, `exec`, `port-forward`, `rollout status`, `rollout undo`, `top`, `events`.

**Tips and common problems**
- Too many new words → keep a glossary in `phase8-notes.md` with one line per object.
- `kubectl top` says metrics not available → metrics-server isn't installed in kind → covered in week 50.

**Done when**
- [ ] You can explain what happens between `kubectl apply -f deployment.yaml` and a running pod.
- [ ] You know which probe restarts a container and which removes it from Service endpoints.

### S2 Build (3h): SF-38 part 1, plain manifests on kind
1. (15m) Create `kind.yaml` and the cluster:
```yaml
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
nodes:
- role: control-plane
  kubeadmConfigPatches:
  - |
    kind: InitConfiguration
    nodeRegistration:
      kubeletExtraArgs:
        node-labels: "ingress-ready=true"
  extraPortMappings:
  - { containerPort: 80, hostPort: 80 }
  - { containerPort: 443, hostPort: 443 }
```
   `kind create cluster --name shopflow --config kind.yaml`; `kubectl cluster-info --context kind-shopflow`.
2. (20m) Load images: `kind load docker-image shopflow/orders:dev --name shopflow` (repeat per service). Create namespace `shopflow`.
3. (45m) Write `k8s/orders/` manifests: Deployment, Service, ConfigMap, Secret. Key parts:
```yaml
containers:
- name: orders
  image: shopflow/orders:dev
  imagePullPolicy: IfNotPresent
  ports: [{ containerPort: 8080 }]
  envFrom: [{ configMapRef: { name: orders-config } }]
  resources:
    requests: { cpu: 250m, memory: 512Mi }
    limits:   { memory: 512Mi }
  startupProbe:
    httpGet: { path: /actuator/health/liveness, port: 8080 }
    periodSeconds: 5
    failureThreshold: 30
  readinessProbe:
    httpGet: { path: /actuator/health/readiness, port: 8080 }
  livenessProbe:
    httpGet: { path: /actuator/health/liveness, port: 8080 }
```
   Enable `management.endpoint.health.probes.enabled=true` in the app.
4. (30m) Postgres for dev: a Deployment (or StatefulSet) with a PVC and Secret for the password. Mark it "dev only" in a comment.
5. (30m) Copy manifests for Inventory, Payments, Notifications. Kafka for dev: a single-node KRaft broker Deployment or a chart/operator you trust *(verify against current docs; chart sources change)*.
6. (30m) `kubectl apply -R -f k8s/`, `kubectl get pods -n shopflow -w`, then `kubectl port-forward svc/orders 8080:80 -n shopflow` and run the checkout flow.
7. (10m) Commit as `sf-38-k8s-core`.

**Tips and common problems**
- `ErrImagePull`/`ImagePullBackOff` for a local image → kind nodes don't see your Docker images → `kind load docker-image`, and use a non-`latest` tag or `imagePullPolicy: IfNotPresent`.
- Pod `Running` but never `Ready` → readiness endpoint 404 → probes not enabled or wrong path.
- Pods `Pending` → requests exceed the node → lower requests, check `kubectl describe pod` Events.
- Port 80 already in use on host → another service → change `hostPort` and note it.

**Done when**
- [ ] All pods `Running` and `Ready`; the checkout flow works via port-forward.
- [ ] Every container has requests, limits, and three probes with deliberate values.

### S3 Lab (2h): RL-97, RL-98, RL-100
1. (10m) `kubectl create namespace lab`. Use a copy of Orders manifests there.
2. (25m) **RL-97 trigger:** set `SPRING_DATASOURCE_URL` to a host that doesn't exist. **Observe:** `kubectl get pods -n lab` shows `CrashLoopBackOff`; `kubectl describe pod` Events show `Back-off restarting failed container`; `kubectl logs <pod> --previous` shows the connection error. Contrast: a missing ConfigMap key gives `CreateContainerConfigError`, not CrashLoopBackOff. Fix the URL. 
3. (30m) **RL-98 trigger:** limit memory to 256Mi and `JAVA_TOOL_OPTIONS=-Xmx512m`; generate load or call `/lab/alloc?mb=400`. **Observe:** `kubectl describe pod` → `Last State: Terminated`, `Reason: OOMKilled`, `Exit Code: 137`, and no Java stack trace in logs. Then contrast: `-Xmx64m` with limit 512Mi and the same allocation → logs show `java.lang.OutOfMemoryError: Java heap space`. Fix by sizing heap as a percentage and giving headroom.
4. (30m) **RL-100 trigger:** make startup slow (sleep 40 s in a lab-only `@PostConstruct`) and give a liveness probe `initialDelaySeconds: 5`, `failureThreshold: 3`, `periodSeconds: 5`. **Observe:** pod restarts repeatedly; Events say `Liveness probe failed`. Fix with a `startupProbe` (`failureThreshold: 30`, `periodSeconds: 5`) so liveness starts only after startup succeeds.
5. (15m) For each lab write the mechanism in 3 sentences (who kills, what signal, how it shows up).
6. (10m) Ledger entries RL-97/98/100.

**Tips and common problems**
- `--previous` says no previous container → the pod was recreated or the container never started → use `describe` Events or `kubectl get events --sort-by=.lastTimestamp`.
- OOMKilled doesn't appear → the app's own OOM fired first or limit is higher → compare `-Xmx` with the memory limit.
- Slow start isn't reproduced → `@PostConstruct` ran in a unit-test profile only → enable the lab profile via `SPRING_PROFILES_ACTIVE`.

**Done when**
- [ ] You saw exit code 137 and a Java OOME stack trace as two different failures.
- [ ] You reproduced a probe-induced restart loop and fixed it with a startup probe.

### S4 Review + Write (2h)
1. (30m) Write the "pod won't start" decision tree in `docs/k8s-troubleshooting.md`: Pending → events (scheduling); ImagePull*; CreateContainerConfigError; CrashLoopBackOff → logs `--previous`; Running not Ready → probe/Service endpoints.
2. (30m) Ledger entries for RL-97/98/100 with one-minute answers.
3. (20m) Flashcards: exit codes 137/143/1, probe effects, QoS, request vs limit.
4. (20m) Self-check out loud, then check answers.
5. (10m) Retro entry.
6. (10m) Merge manifests; note what's rough for week 50.

**Self-check:** What is the difference between liveness, readiness and startup probes? What do requests vs limits do? Why does `OOMKilled` leave no Java stack trace?
<details><summary>Answers</summary>

- Liveness failure restarts the container; readiness failure removes the pod from Service endpoints without restarting; startup delays the other two until the app has started.
- Requests are what the scheduler reserves and what CPU shares are based on; limits are enforced ceilings (memory over limit → kill; CPU over limit → throttling).
- The kernel kills the process with SIGKILL when it exceeds the cgroup memory limit, so the JVM never gets to print anything.
</details>

**Output:** manifests running on kind, troubleshooting tree, RL-97/98/100 entries.
**If stuck:** `kubectl describe` the object and read the Events section top to bottom before anything else.

---

## Week 50 — Kubernetes networking, scaling and TLS
**Outcome:** exposes services safely and scales them sensibly.
**Before you start:** week 49 cluster and manifests running; port 80/443 mapped in `kind.yaml`.

### S1 Learn (3h): traffic, scaling, termination
1. (30m) Read "Services, Load Balancing, and Networking": cluster DNS names (`svc.namespace.svc.cluster.local`), Endpoints/EndpointSlices.
2. (40m) Read Ingress and the Gateway API overview, then the ingress-nginx or Gateway controller docs you plan to use.
3. (30m) Read cert-manager docs: Issuer vs ClusterIssuer, Certificate, self-signed issuer, renewal.
4. (30m) Read "Horizontal Pod Autoscaling" including `behavior` (stabilization windows) and why metrics-server is needed. Read PDB docs.
5. (30m) Read "Pod Lifecycle": termination sequence (endpoint removal and SIGTERM race, `preStop`, `terminationGracePeriodSeconds`).
6. (20m) Read about CPU limits and cgroup throttling, and about load-balancer idle timeouts and keep-alive. Write a 5-line summary of each.

**Tips and common problems**
- Termination reading is confusing → draw the timeline with two arrows (endpoint removal, SIGTERM) racing each other.

**Done when**
- [ ] You can explain why traffic may still arrive after SIGTERM and how `preStop` plus readiness mitigates it.

### S2 Build (3h): SF-38 part 2, Ingress, TLS, HPA, PDB, drain
1. (30m) Install ingress-nginx for kind using the manifest in the kind docs' Ingress page *(verify against current docs)*. Wait: `kubectl wait --namespace ingress-nginx --for=condition=ready pod --selector=app.kubernetes.io/component=controller --timeout=120s`.
2. (30m) Add an Ingress for the API and UI with host `shopflow.localtest.me` (resolves to 127.0.0.1) or an `/etc/hosts` entry. Test: `curl -i http://shopflow.localtest.me/api/products`.
3. (40m) Install cert-manager with Helm from the Jetstack repo as in the cert-manager docs *(verify against current docs)*. Create a self-signed `ClusterIssuer`, add `cert-manager.io/cluster-issuer` and a `tls:` section to the Ingress. Check `kubectl get certificate -A` shows `READY=True` and `curl -k https://shopflow.localtest.me`.
4. (30m) Install metrics-server; on kind it usually needs the `--kubelet-insecure-tls` argument *(verify against current docs)*. Add an HPA:
```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
spec:
  scaleTargetRef: { apiVersion: apps/v1, kind: Deployment, name: orders }
  minReplicas: 2
  maxReplicas: 6
  metrics:
  - type: Resource
    resource: { name: cpu, target: { type: Utilization, averageUtilization: 60 } }
  behavior:
    scaleDown: { stabilizationWindowSeconds: 300 }
```
5. (30m) Add `preStop` and PDB:
```yaml
lifecycle:
  preStop:
    exec: { command: ["sh","-c","sleep 10"] }
terminationGracePeriodSeconds: 45
---
apiVersion: policy/v1
kind: PodDisruptionBudget
spec:
  minAvailable: 1
  selector: { matchLabels: { app: orders } }
```
   Keep `server.shutdown=graceful` and a drain period shorter than the grace period.
6. (20m) Verify: run a k6 or `hey` constant load against the Ingress while `kubectl rollout restart deploy/orders`. Count non-2xx.

**Tips and common problems**
- Ingress returns 404 from nginx → host/path mismatch or wrong `ingressClassName` → `kubectl describe ingress`.
- `curl: (7) Failed to connect` on port 80 → `kind.yaml` lacked `extraPortMappings` or node label → recreate the cluster.
- HPA shows `<unknown>/60%` → metrics-server not ready or no CPU requests → set requests and wait a minute.
- Certificate stays `False` → issuer/CRDs not ready → `kubectl describe certificate`, `kubectl get challenges -A`.

**Done when**
- [ ] HTTPS works through the Ingress with a cert-manager certificate.
- [ ] HPA reports real CPU numbers and scales under load.
- [ ] A rollout under load produces zero failed requests (or you know exactly why not).

### S3 Lab (2h): RL-99, RL-102, RL-103, RL-94, RL-93, RL-92
1. (10m) Prepare a load script (`k6`) with a constant arrival rate against the Ingress, printing p95 and error rate.
2. (20m) **RL-99 CPU throttling:** set `limits.cpu: 200m` on Orders, run load. **Observe:** latency spikes; inside the pod, `cat /sys/fs/cgroup/cpu.stat` shows `nr_throttled` and `throttled_usec` growing (cgroup v2); or `container_cpu_cfs_throttled_periods_total` in Prometheus when available. Fix: raise or remove CPU limit (keep the request), compare p95.
3. (20m) **RL-102 rollout errors:** remove `preStop`, run constant load, `kubectl rollout restart`. **Observe:** a burst of 502/connection reset in k6. Add `preStop sleep 10` and graceful shutdown; repeat; errors drop to zero. Explain the endpoint-removal vs SIGTERM race.
4. (15m) **RL-103 HPA flapping:** target 50% CPU, remove the stabilization window, send spiky load (30 s on, 30 s off). **Observe:** `kubectl get hpa -w` and `kubectl describe hpa` Events show frequent scale up/down. Fix with `behavior.scaleDown.stabilizationWindowSeconds: 300`.
5. (15m) **RL-94 TLS expiry:** `openssl s_client -connect expired.badssl.com:443 -servername expired.badssl.com </dev/null 2>/dev/null | openssl x509 -noout -dates`. **Observe:** `notAfter` in the past; `curl -v https://expired.badssl.com` says certificate has expired. Then call it from a small Java `HttpClient` to see the exception. Fix idea: cert-manager renewal plus an alert on `certmanager_certificate_expiration_timestamp_seconds`.
6. (15m) **RL-93 keep-alive mismatch:** set Tomcat `server.tomcat.keep-alive-timeout=2s`, send requests spaced about 2 s apart through the Ingress. **Observe:** intermittent 502 with `upstream prematurely closed connection` in `kubectl logs -n ingress-nginx <controller>`. Fix: the backend's idle timeout must be longer than the proxy's.
7. (15m) **RL-92 JVM DNS caching:** write a 20-line Java program that resolves a name in a loop every second; change the answer via `/etc/hosts` in the pod (`kubectl exec`). **Observe:** the old IP persists until the TTL expires (default 30 s with no security manager; check `networkaddress.cache.ttl`). Set `java.security.Security.setProperty("networkaddress.cache.ttl","5")` and repeat *(verify behavior on your JDK)*.
8. (10m) Ledger notes for all six, one sentence each for mechanism and fix.

**Tips and common problems**
- No `cpu.stat` file → cgroup v1 → path is `/sys/fs/cgroup/cpu/cpu.stat`.
- k6 "rate limiting" or 429 → your own app limiter from Phase 5 → disable for the lab.
- Can't reproduce 502 → timings must align → keep the request gap close to the timeout and run for a minute.

**Done when**
- [ ] Each of the six produced evidence (output, metric or log line) saved in the ledger.
- [ ] Rollout error count was non-zero before and zero after the `preStop` fix.

### S4 Review + Write (2h)
1. (40m) Write `docs/zero-downtime-rollout-checklist.md`: readiness probe, graceful shutdown on, `preStop` ≥ endpoint propagation time, grace period > drain, PDB, `maxUnavailable`, backward-compatible DB changes, canary.
2. (30m) Ledger entries for the six labs; say each answer aloud.
3. (20m) Flashcards: HPA flags, termination order, keep-alive rule, cgroup throttling.
4. (20m) Self-check out loud, then check answers.
5. (10m) Retro; merge SF-38.

**Self-check:** Why does a rolling update drop requests without readiness drain? What does CPU throttling look like in metrics? Why do 502s appear intermittently?
<details><summary>Answers</summary>

- Endpoint removal and SIGTERM race: the pod can receive traffic after it begins shutting down; `preStop` delay plus graceful shutdown keeps it serving until routers have updated.
- A rising throttled-periods counter and latency spikes even though average CPU looks low (bursty usage hits the quota within each 100 ms period).
- The proxy reuses a connection that the backend has already closed because of its idle timeout; the race is probabilistic.
</details>

**Output:** SF-38 complete, rollout checklist, RL-92/93/94/99/102/103 entries.
**If stuck:** `kubectl get events -A --sort-by=.lastTimestamp | tail -30` shows what the cluster has been trying to do.

---

## Week 51 — Helm, GitOps and progressive delivery
**Outcome:** declarative, auditable, reversible deployments.
**Before you start:** manifests from week 49–50 work; images are pushed to ghcr (week 48) or loaded into kind.

### S1 Learn (3h): templates, GitOps, canaries
1. (45m) Read Helm "Chart Template Guide": values, `include`, `toYaml`, `nindent`, named templates, hooks. Run `helm create demo` and read the generated files.
2. (20m) Read Kustomize basics: base and overlays; note when you would choose it over Helm.
3. (20m) Read OpenGitOps principles (opengitops.dev) and write the four principles in your own words.
4. (40m) Read Argo CD docs: Application, sync policy, automated prune and selfHeal, health assessment, sync waves.
5. (35m) Read Argo Rollouts: canary steps, `AnalysisTemplate`, abort and rollback behavior.
6. (20m) Read External Secrets Operator or Sealed Secrets overview, and think through migration ordering during rollouts (run Flyway before or as part of the deploy; backward-compatible only).

**Tips and common problems**
- Helm templating feels like string soup → render often with `helm template . | less`.

**Done when**
- [ ] You can explain how git revert becomes a rollback in GitOps.
- [ ] You can list three things that make a migration safe during a rolling or canary release.

### S2 Build (3h): SF-39 Helm chart, Argo CD, canary
1. (15m) `helm create charts/shopflow-service`; delete what you don't need. Design `values.yaml` with `image.repository`, `image.tag`, `replicas`, `resources`, `env`, `ingress.enabled`.
2. (45m) Write the templates. Validate: `helm lint charts/shopflow-service`, `helm template orders charts/shopflow-service -f values-orders.yaml | kubectl apply --dry-run=client -f -`.
3. (20m) Install Argo CD in namespace `argocd` using the install manifest from the Argo CD "Getting Started" page *(verify against current docs)*. Port-forward `svc/argocd-server` and log in (initial password: `argocd admin initial-password -n argocd` *(verify)*).
4. (30m) Create one `Application` per service pointing to the chart path and values in git:
```yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata: { name: orders, namespace: argocd }
spec:
  project: default
  source:
    repoURL: <your repo>
    path: charts/shopflow-service
    targetRevision: main
    helm: { valueFiles: [../../envs/dev/orders.yaml] }
  destination: { server: https://kubernetes.default.svc, namespace: shopflow }
  syncPolicy:
    automated: { prune: true, selfHeal: true }
```
5. (45m) Install Argo Rollouts and convert Orders to a `Rollout` with canary steps (`setWeight: 20`, `pause: {duration: 60s}`, `setWeight: 50`, pause, full). Add an `AnalysisTemplate` that checks error rate (Prometheus provider once Prometheus exists in week 53; for now a `job` or `web` provider hitting a canary check endpoint) *(verify against current docs)*.
6. (25m) Ship a deliberately bad image (an endpoint returns 500). **Observe:** the Rollout pauses/aborts and returns traffic to stable. Then roll forward with a good image by editing `envs/dev/orders.yaml` in git only.

**Tips and common problems**
- Argo CD keeps showing OutOfSync → server-side defaulted fields differ from your manifest → use `ignoreDifferences` for those fields or render exact values.
- `selfHeal` reverts your manual `kubectl edit` → that's the feature; change git instead.
- Helm: `error converting YAML to JSON` → indentation after `toYaml` → use `{{- toYaml .Values.resources | nindent 12 }}`.
- App stuck "Progressing" → readiness never passes → check pod events first.

**Done when**
- [ ] Changing a value in git changes the cluster without any manual `kubectl`.
- [ ] A bad canary is aborted and stable keeps serving.
- [ ] `git revert` of the bad commit returns the cluster to the previous version.

### S3 Lab (2h): RL-101 incompatible migration, RL-106 secret rotation
1. (10m) Prepare a lab release with a Flyway migration and 2 replicas of the previous version running.
2. (30m) **RL-101 trigger:** add a migration that renames `orders.total` to `orders.total_amount` and deploy a new app version that uses the new name. During the rolling update, keep traffic flowing. **Observe:** old pods return 500 with `column "total" does not exist` in logs while new pods are fine; the opposite problem occurs on rollback.
3. (25m) **RL-101 fix (expand/contract):** release 1 adds `total_amount` and writes both; release 2 reads the new column; release 3 drops the old one. Re-run the rollout under load and confirm no errors at each release.
4. (35m) **RL-106 rotation:** use the dual-user pattern. Create role `app_a` and `app_b` (same grants). App uses `app_a`; update the Secret to `app_b`; trigger a rolling restart (a reloader annotation or manual `kubectl rollout restart`); then change `app_a`'s password. **Observe:** zero failed requests during the switch. If you use External Secrets, observe the Secret sync interval *(verify against current docs)*.
5. (10m) Write the mechanism in 3 sentences for each lab.
6. (10m) Ledger entries RL-101 and RL-106.

**Tips and common problems**
- Flyway fails with checksum mismatch → you edited an applied migration → never edit; add a new one.
- App still uses the old password → env vars from Secrets are read at start → roll the pods.
- The old version can't run against the new schema even in the lab → your "expand" step removed something → expand must only add.

**Done when**
- [ ] Rollout across all three releases produced zero errors.
- [ ] Password rotated without downtime; old credential revoked.

### S4 Review + Write (2h)
1. (30m) Write ADR-021 "GitOps with Argo CD; rollbacks via git revert", including what is not in git (secrets) and how they are handled.
2. (30m) Ledger entries RL-101 and RL-106.
3. (25m) Document the migration rules in `docs/migrations.md`: expand/contract, never edit applied migrations, backward compatible for N-1 app versions.
4. (20m) Self-check out loud, then check answers.
5. (15m) Retro; merge SF-39.

**Self-check:** What does GitOps give you that `kubectl apply` doesn't? Why must migrations be backward compatible? How does a canary decide to roll back?
<details><summary>Answers</summary>

- Git history as the audit trail and source of truth, continuous drift correction, review before change, rollback by revert, no cluster credentials on laptops.
- During a rolling or canary release, old and new application versions run against the same database at the same time (and rollback runs old code on new schema).
- It evaluates analysis metrics (e.g., error rate or latency) against thresholds at each step; failing the analysis aborts the rollout and shifts traffic back to stable.
</details>

**Output:** SF-39, ADR-021, RL-101/106 entries.
**If stuck:** in Argo CD click the resource, then "Diff" and "Events"; most sync problems show up there.

---

## Week 52 — Infrastructure as code and AWS essentials
**Outcome:** reproducible cloud infrastructure with cost awareness.
**Before you start:** an AWS account with MFA on root, an IAM Identity Center/SSO user or admin IAM user (never use root for work), AWS CLI v2 configured, Terraform installed. **Create the budget alert first.**

### S1 Learn (3h): Terraform and AWS building blocks
1. (45m) Read *Terraform: Up & Running* early chapters (state, variables, modules) alongside Terraform docs "Language".
2. (30m) Read Terraform docs on remote state and locking (S3 backend). Note the failure mode: two applies at once.
3. (30m) Read AWS docs "Security best practices in IAM": least privilege, roles over users, no long-lived keys.
4. (30m) Read VPC docs: subnets, route tables, internet gateway, NAT gateway, security groups. Note the NAT gateway cost.
5. (25m) Read RDS (Postgres) basics, Multi-AZ, backups, and ECS Fargate vs EKS trade-offs (control plane cost, operations load).
6. (20m) Read the Well-Architected cost pillar and tagging guidance. Write the 5 tags you will apply (`project`, `env`, `owner`, `ttl`, `cost-center`).

**Tips and common problems**
- Overwhelmed by AWS services → only learn what the project uses: IAM, VPC, one compute service, RDS, S3, Secrets Manager.

**Done when**
- [ ] You can explain public vs private subnets and why RDS lives in private ones.
- [ ] You can explain what Terraform state contains and why it is sensitive.

### S2 Build (3h): SF-41 reproducible environment
1. (20m) **Budget first:** create an AWS Budget with a low monthly limit and email alerts (console or `aws budgets create-budget` *(verify against current docs)*). `aws sts get-caller-identity` to confirm the right account and not root.
2. (15m) Create `infra/` with `providers.tf`, `variables.tf`, `main.tf`, `outputs.tf`; default tags via `provider "aws" { default_tags { tags = { project = "shopflow", env = "lab", ttl = "1d" } } }`. Start with local state for the lab; note remote state with S3 and locking as the production approach *(verify current locking option)*.
3. (50m) VPC: use the well-known community VPC module or hand-write one with 2 AZs, public and private subnets. Prefer **no NAT gateway** in the lab (cost); use public subnets for compute if required and say so in the README.
4. (40m) RDS Postgres: `db.t4g.micro`, single-AZ for the lab, `skip_final_snapshot = true` for the lab only, security group allowing only the app SG, password from Secrets Manager (`manage_master_user_password = true`) *(verify against current docs)*.
5. (30m) Compute: smallest viable option. ECS Fargate service for Orders is usually cheapest; EKS control plane has an hourly fee. Pick one and record the reason.
6. (25m) `terraform fmt -check`, `terraform validate`, `terraform plan -out=plan.out`; read the plan line by line; `terraform apply plan.out`; check the app responds; record the hourly cost estimate; **`terraform destroy`**; confirm nothing is left (`aws resourcegroupstaggingapi get-resources --tag-filters Key=project,Values=shopflow`).

**Tips and common problems**
- `Error acquiring the state lock` → another run or crashed run → confirm no other apply is running, then `terraform force-unlock <ID>`.
- `AccessDenied` or `UnauthorizedOperation` → your role lacks the action → read the error's action name, fix the policy minimally (not `*:*`).
- Destroy fails on a VPC → leftover ENIs, security groups or load balancers → delete them by tag/ID and re-run.
- Surprise bill → NAT gateway, EKS control plane, idle RDS, unattached EBS → destroy at the end of every session and check Cost Explorer next day.

**Done when**
- [ ] `plan` → `apply` → app works → `destroy` completed in one session with no leftover tagged resources.
- [ ] Cost estimate written down.

### S3 Lab (2h): least-privilege IAM and budget alerts (extra labs)
1. (10m) Create a sandbox role `shopflow-app-role` via Terraform or the console.
2. (35m) Write a least-privilege policy: `s3:GetObject` and `s3:PutObject` only on `arn:aws:s3:::shopflow-lab-bucket/orders/*`; `secretsmanager:GetSecretValue` only on one secret ARN.
3. (25m) Prove denials: `aws iam simulate-principal-policy --policy-source-arn <role-arn> --action-names s3:DeleteBucket secretsmanager:GetSecretValue --resource-arns <arn>`. **Observe:** `EvalDecision: implicitDeny` for `s3:DeleteBucket` and `allowed` for the permitted actions *(verify output format)*. Try also with the console's policy simulator.
4. (30m) Budgets: create a second tiny budget with an 80% actual and 100% forecast notification, confirm the SNS/email subscription. Billing data lags many hours, so you cannot trigger it in-session; note that, and check it the next day.
5. (10m) Run the leftover check: `aws resourcegroupstaggingapi get-resources --tag-filters Key=project,Values=shopflow` should return nothing.
6. (10m) Notes: policy, evidence, lessons.

**Tips and common problems**
- Simulator says `allowed` for everything → a broader policy (e.g., `AdministratorAccess`) is attached → inspect attached and inline policies.
- ARN mismatch → S3 object actions need `bucket/key` ARNs; bucket actions need `bucket` ARN.

**Done when**
- [ ] A denied action is proven denied by evidence.
- [ ] No tagged resources remain.

### S4 Review + Write (2h)
1. (40m) Write the cost and risk memo: monthly estimate for a minimal production setup vs lab usage; top 3 risks (cost, IAM, state).
2. (30m) Write ADR-022 "Cloud: AWS; managed Postgres; why not self-host": options, decision, consequences.
3. (20m) Flashcards: Terraform commands and state, IAM terms (principal, policy, role), public vs private subnet.
4. (20m) Self-check out loud, then check answers.
5. (10m) Retro; merge SF-41.

**Self-check:** What does Terraform state hold and why is it dangerous? What is least privilege in practice? Which parts of the stack cost the most?
<details><summary>Answers</summary>

- A map from your configuration to real resource IDs and attributes, which may include secrets; losing or corrupting it makes Terraform lose track or duplicate resources, and leaking it exposes secrets.
- Grant only the specific actions on the specific resources a workload needs, verified by simulation, reviewed over time.
- Typically NAT gateways, managed Kubernetes control planes, RDS instances (especially Multi-AZ), data transfer, and forgotten idle resources.
</details>

**Output:** SF-41 plan/apply logs, cost memo, ADR-022.
**If stuck:** `terraform plan` output plus the exact error name is nearly always enough to find the cause; read the resource's docs page for required arguments.

---

## Week 53 — Observability and SLOs
**Outcome:** can answer "is it healthy and why not" with data.
**Before you start:** the kind cluster from week 49–51 is running; Helm installed.

### S1 Learn (3h): signals and SLOs
1. (30m) Read SRE book ch. 4 (Service Level Objectives). Write SLI, SLO, SLA, error budget in your own words.
2. (30m) Read SRE book ch. 6 (Monitoring Distributed Systems): four golden signals.
3. (40m) Read *SRE Workbook* ch. 2 and 5 (Implementing SLOs, Alerting on SLOs): burn rates and multi-window alerts.
4. (25m) Read Prometheus docs "Best practices" (metric and label naming) and about cardinality.
5. (35m) Read Spring Boot reference "Observability" and the OpenTelemetry concepts (traces, spans, context propagation).
6. (20m) Skim selected chapters from *Observability Engineering* on structured events and "unknown unknowns".

**Done when**
- [ ] You can compute the error budget for a 99.9% monthly SLO (about 43 minutes).
- [ ] You can state why alerting on symptoms is preferred.

**Tips and common problems**
- Too much theory → pick three user journeys (browse, checkout, order processing) and define one SLI each while reading.

### S2 Build (3h): SF-40 dashboards, logs, traces, SLOs, runbooks
1. (15m) Install the Prometheus + Grafana stack with Helm (a community kube-prometheus-stack chart) *(verify chart repo and values in current docs)*.
2. (35m) Add `micrometer-registry-prometheus`, expose `/actuator/prometheus` (`management.endpoints.web.exposure.include=health,prometheus`), and add a `ServiceMonitor` per service. Confirm targets are UP in Prometheus.
3. (40m) Dashboards as code: JSON in a ConfigMap labelled for the Grafana sidecar. Panels: request rate, error rate, p95 latency (RED); JVM heap and GC; Hikari pool active/pending; Kafka consumer lag; pod CPU/memory.
4. (30m) Logs and traces: structured JSON logs (`logging.structured.format.console` on recent Boot; or a Logback JSON encoder) *(verify)*; include `traceId`. Add `micrometer-tracing-bridge-otel` and an OTLP exporter; deploy Tempo or Jaeger; open a trace for a checkout.
5. (35m) Define three SLOs: checkout availability 99.9%, checkout p95 < 500 ms, order processing delay < 60 s for 99%. Create burn-rate alerts, for a 99.9% availability SLO:
```yaml
- alert: CheckoutFastBurn
  expr: |
    (sum(rate(http_server_requests_seconds_count{uri="/orders",status=~"5.."}[5m]))
     / sum(rate(http_server_requests_seconds_count{uri="/orders"}[5m]))) > (14.4 * 0.001)
    and
    (sum(rate(http_server_requests_seconds_count{uri="/orders",status=~"5.."}[1h]))
     / sum(rate(http_server_requests_seconds_count{uri="/orders"}[1h]))) > (14.4 * 0.001)
  for: 2m
  labels: { severity: page }
```
6. (25m) One runbook per alert: symptom, impact, checks (dashboards, commands), mitigation, escalation. Store in `docs/runbooks/`.

**Tips and common problems**
- Targets show DOWN → ServiceMonitor selector doesn't match the Service labels or port name → fix labels and `port:` name.
- No traces for async work → context not propagated → covered in S3.
- Grafana dashboard lost on restart → created in the UI only → keep it in git.
- Alert never fires in testing → thresholds too high or metric names differ → run the expression in Prometheus first.

**Done when**
- [ ] Dashboards load from git; three SLOs have alerts and runbooks.
- [ ] A checkout shows one trace across services and a log line carries the same trace ID.

### S3 Lab (2h): RL-104 cardinality, RL-105 trace propagation
1. (10m) Create a lab branch; enable a `/lab/metrics` endpoint.
2. (30m) **RL-104 trigger:** register a counter `orders_by_user_total{user_id="..."}` and generate 50,000 distinct user IDs. **Observe:** `prometheus_tsdb_head_series` climbs, Prometheus memory rises, queries slow; query `topk(10, count by (__name__)({__name__=~".+"}))` shows your metric on top.
3. (20m) **RL-104 fix:** remove `user_id` from labels (use logs/traces for per-user detail), and add `sample_limit` in the ServiceMonitor as a guardrail *(verify field names)*. Confirm the series count stops growing.
4. (35m) **RL-105 trigger:** make Checkout publish a Kafka event and also run an `@Async` step. **Observe:** in Tempo/Jaeger the trace ends at the producer and the consumer starts a new trace; logs from the async step have no `traceId`. **Fix:** enable Kafka observation on the template and listener (`spring.kafka.template.observation-enabled`, `spring.kafka.listener.observation-enabled`) *(verify)* and propagate context to executors (`ContextExecutorService` or a context-propagating task decorator). Confirm a single trace.
5. (15m) Write the mechanism for each in 3 sentences.
6. (10m) Ledger entries RL-104/105.

**Tips and common problems**
- Series count doesn't drop after the fix → old series live until the retention/head compaction → wait or restart Prometheus in the lab.
- Trace IDs differ in producer and consumer logs → headers not propagated → check the record headers for `traceparent`.

**Done when**
- [ ] You saw the series explosion and the single-trace fix with your own eyes.

### S4 Review + Write (2h)
1. (30m) Write `docs/slo.md`: SLIs, objectives, measurement queries, error budget policy ("when the budget is spent, we pause feature work").
2. (30m) Ledger entries RL-104/105.
3. (25m) Review each runbook with fresh eyes: could someone paged at 3 a.m. follow it?
4. (20m) Self-check out loud, then check answers.
5. (15m) Retro; merge SF-40.

**Self-check:** Why alert on symptoms rather than causes? What is an error budget used for? What makes a metric high-cardinality?
<details><summary>Answers</summary>

- Symptoms are what users experience and are fewer and more reliable; cause-based alerts are noisy and miss unknown failure modes.
- To balance reliability and feature velocity: when the budget is nearly spent, prioritise reliability; when plenty remains, take more risk.
- A label with many distinct values (user ID, request ID) multiplies the number of time series, increasing memory and query cost.
</details>

**Output:** SF-40, dashboards-as-code, SLO doc, runbooks, RL-104/105 entries.
**If stuck:** query raw metrics in Prometheus's UI first; if the metric isn't there, the problem is scraping, not dashboards.

---

## Week 54 — Reliability drills and incidents
**Outcome:** practised in failing over, restoring, and writing postmortems.
**Before you start:** a Postgres setup with a replica (local operator-managed cluster on kind, or RDS Multi-AZ if budget allows). The local path is the default below.

### S1 Learn (3h): recovery, incidents, chaos
1. (40m) Read PostgreSQL docs "Backup and Restore" including continuous archiving and point-in-time recovery.
2. (30m) Read SRE book ch. 14 (Managing Incidents): roles (incident commander, communications, operations).
3. (30m) Read SRE book ch. 15 (Postmortem Culture).
4. (20m) Read Principles of Chaos Engineering (principlesofchaos.org).
5. (40m) Read two public postmortems (e.g., from GitHub, Cloudflare or AWS). For each write: trigger, root cause, detection time, mitigation, lessons, and one question you'd ask.
6. (20m) Write definitions and examples of RPO and RTO for ShopFlow's orders DB (what is acceptable?).

**Done when**
- [ ] You can state target RPO/RTO numbers for ShopFlow and justify them.
- [ ] You have two postmortem summaries in notes.

**Tips and common problems**
- Postmortems blame people → rewrite each finding as a system condition ("the deploy tool allowed X").

### S2 Build (3h): SF-42 failover and restore drill
1. (20m) Install a Postgres operator on kind (for example CloudNativePG) and create a 3-instance cluster *(verify install steps in current docs)*. Point Orders at the read-write service.
2. (20m) Write a load script: 20 writes/s to `/orders`, logging each `(timestamp, orderId, status)` to a file.
3. (30m) **Failover:** while load runs, delete the primary pod (`kubectl delete pod <primary> -n <ns>`). Record: time to first failed request, time to first success after (RTO), number of failed requests. Compare acknowledged order IDs from the client log with rows in the DB to count lost acknowledged writes (RPO; should be 0 with synchronous replication, may be non-zero otherwise).
4. (25m) Tune recovery: Hikari `connectionTimeout`, `maxLifetime`, `keepaliveTime`, and Postgres JDBC `targetServerType`/`loginTimeout` *(verify names)*; repeat the drill and compare RTO.
5. (45m) **Restore:** take a base backup (operator's `Backup` resource or `pg_basebackup`), insert more rows, then restore to a new cluster (operator recovery bootstrap) *(verify docs)*. Verify with `select count(*)` and a checksum query over key columns.
6. (40m) Write the results table (RTO, RPO, failed requests, restore time) in `docs/dr-drill.md`.

**Tips and common problems**
- Pods recreate instantly and you see no outage → failover was too quick for your load rate → increase load or measure at the 95th percentile of request latency.
- Restore "works" but is empty → you restored the base backup without WAL → restore to a target time/LSN.
- Client keeps old connections → pool doesn't validate → configure max lifetime and keepalive.

**Done when**
- [ ] Failover drill has measured RTO and an RPO check.
- [ ] A restore into a fresh instance matched row counts and checksum.

### S3 Lab (2h): RL-82 backup/restore test, RL-107 postmortem
1. (10m) Prepare a scratch Postgres container with seeded ShopFlow data.
2. (30m) **RL-82:** `pg_dump -Fc -d shopflow -f shopflow.dump`; start a new empty container; `pg_restore -d shopflow_restored shopflow.dump`. Verify `select count(*) from orders;` and a checksum such as `select md5(string_agg(id::text||total_amount::text, ',' order by id)) from orders;` on both. 
3. (20m) **Break it:** `drop table orders;` in the original, then restore from the dump and from PITR if available. Note the data you lost (rows since the dump).
4. (40m) **RL-107:** write the postmortem for the failover drill using this template: Summary, Impact, Timeline (UTC), Root cause and contributing factors, Detection, Response, What went well, What went badly, Where we got lucky, Action items (owner, due date, type: prevent/detect/mitigate).
5. (10m) Ask a peer (or re-read it next day) for blame-free language.
6. (10m) Ledger entries RL-82 and RL-107.

**Tips and common problems**
- `pg_restore` errors about roles/owners → restore with `--no-owner --no-privileges` in the lab.
- Checksum differs → ordering or float types → use `order by` and cast numerics to text.

**Done when**
- [ ] The restore matched count and checksum.
- [ ] The postmortem has action items with owners and dates.

### S4 Review + Write (2h)
1. (30m) Add one chaos experiment to the repo, e.g., Chaos Mesh `PodChaos` (kill one Orders pod for 30 s) with a k6 load and a check that SLO alerts do not fire.
```yaml
apiVersion: chaos-mesh.org/v1alpha1
kind: PodChaos
metadata: { name: kill-orders, namespace: shopflow }
spec:
  action: pod-kill
  mode: one
  selector: { namespaces: [shopflow], labelSelectors: { app: orders } }
```
   *(verify CRD fields against current docs)*
2. (30m) Run it; record the hypothesis ("no user-visible errors") and the result.
3. (30m) Ledger answers for RL-82/107 said aloud.
4. (20m) Self-check out loud, then check answers.
5. (10m) Retro; merge SF-42 docs.

**Self-check:** What are RPO and RTO? Why is an untested backup not a backup? What belongs in a blameless postmortem?
<details><summary>Answers</summary>

- RPO is how much data (time) you can afford to lose; RTO is how long you can afford to be down.
- Only a successful restore proves the backup is complete, consistent, and the procedure works within RTO.
- Timeline, impact, causes as system conditions, what went well/badly, luck, and action items with owners; no individual blame.
</details>

**Output:** SF-42 numbers, postmortem, chaos experiment, RL-82/107 entries.
**If stuck:** simplify to a single primary and replica container pair with `docker stop`; the measurement method is the same.

---

## Week 55 — Consolidation and Phase 8 gate
**Outcome:** passes the delivery gate.
**Before you start:** list the open TODOs from weeks 47–54 and any unfinished labs from Phases 4–7.

### S1 Learn (3h): review and gap closing
1. (30m) Re-read your `retro.md` entries from weeks 47–54 and list repeated blockers.
2. (40m) Trace the whole path in a diagram: PR → CI → image → registry → git change → Argo CD → canary → metrics → promote/rollback. Mark where each tool lives and what each gate checks.
3. (40m) Re-do five flashcards sets (containers, CI, K8s, GitOps, observability). Mark cards you miss.
4. (40m) Re-read the weakest topic's original reading section.
5. (20m) Write the list of "I would still struggle with" items; each becomes a task for S3.
6. (10m) Prepare the timing sheet for the gate drill: columns for PR open, checks green, merge, image pushed, sync, canary done, verified.

**Done when**
- [ ] A one-page end-to-end diagram exists.
- [ ] A short, honest gap list exists.

**Tips and common problems**
- Everything feels fine but you haven't tried it → run the drill once as a rehearsal in S2.

### S2 Build (3h): gate drill (PR to production in under 15 minutes)
1. (15m) Reset to a clean cluster state: all Argo CD apps Synced and Healthy.
2. (10m) Start a timer. Change one line (for example a pricing rule or a text constant) with a test.
3. (25m) Open a PR; watch checks; fix anything that fails; merge. Record timestamps.
4. (30m) Watch the image publish, the git change in the envs directory (automated by the pipeline or by hand), Argo CD sync and the canary steps. Keep Grafana open.
5. (20m) Verify in telemetry: version label on dashboards, no SLO alert, one trace through the new version.
6. (30m) Roll back: `git revert <sha>`; push; watch stable resume. Record how long.
7. (50m) If any step took too long, optimize it and repeat the drill until the total is under 15 minutes. Record all runs in `docs/gate-phase8.md`.

**Tips and common problems**
- Canary pauses dominate the time → shorten the pause for the drill but keep analysis; or note that production uses longer pauses.
- Manual image tag edits → automate with the pipeline updating the values file (or Argo CD Image Updater *(verify)*).

**Done when**
- [ ] Documented run with total time < 15 minutes and a timed rollback.

### S3 Lab (2h): leftover catalog items
1. (15m) List unfinished RL items from Phases 4–8 with their IDs.
2. (30m) Pick the three most relevant to production and redo them with a failing test first.
3. (30m) Second item (same procedure).
4. (30m) Third item (same procedure).
5. (15m) Write missing ledger entries; verify each has evidence (command output, screenshot or log excerpt).

**Tips and common problems**
- Items marked done but you cannot explain them → mark them "not done" and repeat; the ledger is for explanation, not for completion.

**Done when**
- [ ] Every ledger entry has reproducible evidence.

### S4 Review + Write (2h)
1. (40m) Gate review with a mentor or peer, or record yourself explaining the end-to-end flow and review the recording against the rubric.
2. (30m) Walk through the ledger entries RL-20, 47, 82, 92–94, 97–107 aloud; mark weak ones.
3. (20m) Update the progress tracker; `git tag phase-8`.
4. (20m) Retro for the whole phase.
5. (10m) Plan the Phase 9 setup (legacy sample, OSS project candidates, peer for game-day).

**Gate rubric (pass all):**
- PR to production in <15 minutes with automated tests, scan and canary.
- Rollback verified (revert and see the old version serve traffic).
- Dashboards and alerts show the deploy; at least one SLO alert has a tested runbook.
- Ledger has RL-20, 47, 82, 92–94, 97–107 explained aloud.

**Output:** `phase-8` tag, timing evidence.
**If stuck:** if the gate fails on a single step, fix that step and re-run the whole drill; don't tick the box with a partial run.
