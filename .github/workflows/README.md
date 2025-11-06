# GitHub Actions CI/CD Pipelines

This directory contains the CI/CD pipelines for the Vehicle Event microservices project.

## Workflows

### 1. CI Build and Test (`ci.yml`)
**Triggers:** Push to main, Pull requests to main

**What it does:**
- Builds all Maven modules
- Runs unit tests
- Packages JAR artifacts
- Uploads build artifacts for download

**Requirements:** None - runs automatically

---

### 2. Docker Build (`docker-build.yml`)
**Triggers:** Push to main, Tags (v*), Manual dispatch

**What it does:**
- Builds Docker images for all 4 microservices using Jib
- Pushes images to GitHub Container Registry (ghcr.io)
- Tags images with commit SHA and `latest`
- Uses matrix strategy to build all services in parallel

**Images produced:**
- `ghcr.io/<owner>/vehicleupdateservice`
- `ghcr.io/<owner>/vehiclequeryservice`
- `ghcr.io/<owner>/lotupdateservice`
- `ghcr.io/<owner>/lotqueryservice`

**Requirements:** GitHub Container Registry access (automatically provided via GITHUB_TOKEN)

---

### 3. Dependency Security Check (`dependency-check.yml`)
**Triggers:** Push to main, Pull requests, Weekly schedule (Mondays 9 AM UTC), Manual dispatch

**What it does:**
- Runs dependency review for PRs
- Performs OWASP dependency check for known vulnerabilities
- Fails build on CVSS score >= 7
- Uploads security report as artifact

**Requirements:** None - runs automatically

---

### 4. CodeQL Security Analysis (`codeql.yml`)
**Triggers:** Push to main, Pull requests, Weekly schedule (Wednesdays 3 AM UTC)

**What it does:**
- Scans Java code for security vulnerabilities
- Detects code quality issues
- Reports findings in Security tab
- Integrates with GitHub Advanced Security

**Requirements:** GitHub Advanced Security (free for public repos)

---

### 5. Pull Request Checks (`pr-checks.yml`)
**Triggers:** Pull requests (opened, synchronized, reopened)

**What it does:**
- Lists changed files
- Performs code style checks
- Verifies build succeeds
- Comments on PR with status

**Requirements:** None - runs automatically

---

## Caching

All workflows use Maven dependency caching to speed up builds:
- Cache key: Based on `pom.xml` hash
- Cached directory: `~/.m2/repository`

## Artifacts

### Build Artifacts (7 day retention)
- All JAR files from target directories
- Available for download from workflow runs

### Security Reports (30 day retention)
- OWASP dependency check reports
- Available under Security > Dependency check

## Manual Workflow Dispatch

Some workflows can be triggered manually:
1. Go to Actions tab
2. Select the workflow
3. Click "Run workflow"
4. Choose branch and click "Run"

## Environment Variables

### Docker Build
- `REGISTRY`: ghcr.io (GitHub Container Registry)
- `IMAGE_PREFIX`: GitHub repository owner

## Secrets Required

None! All workflows use:
- `GITHUB_TOKEN` - Automatically provided by GitHub Actions
- No additional secrets needed

## Branch Protection

Recommended branch protection rules for `main`:
- ✅ Require status checks to pass before merging
  - CI Build and Test
  - Build Verification
- ✅ Require branches to be up to date before merging
- ✅ Require pull request before merging

## Badge Status

Add these badges to your README.md:

```markdown
![CI](https://github.com/<owner>/vehicleevent/workflows/CI%20Build%20and%20Test/badge.svg)
![Docker](https://github.com/<owner>/vehicleevent/workflows/Docker%20Build/badge.svg)
![Security](https://github.com/<owner>/vehicleevent/workflows/CodeQL%20Security%20Analysis/badge.svg)
```

## Local Testing

To test the build locally before pushing:

```bash
# Run the same commands as CI
mvn clean compile -B
mvn test -B
mvn package -DskipTests -B

# Build Docker images locally
mvn clean package jib:dockerBuild -DskipTests
```
