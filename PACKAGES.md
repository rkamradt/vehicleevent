# Using vehicleapi and lotapi from GitHub Packages

This repository publishes `vehicleapi` and `lotapi` artifacts to GitHub Packages. These shared API modules can be consumed by other projects.

## For Package Consumers

### 1. Create a Personal Access Token (PAT)

1. Go to GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic)
2. Click "Generate new token (classic)"
3. Give it a descriptive name (e.g., "Maven GitHub Packages")
4. Select the `read:packages` scope
5. Click "Generate token" and copy it (you won't see it again!)

### 2. Configure Maven Settings

Add the following to your `~/.m2/settings.xml`:

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                              http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <servers>
        <server>
            <id>github</id>
            <username>YOUR_GITHUB_USERNAME</username>
            <password>YOUR_PERSONAL_ACCESS_TOKEN</password>
        </server>
    </servers>
</settings>
```

**Security Tip:** Use environment variables instead of hardcoding:
```xml
<server>
    <id>github</id>
    <username>${env.GITHUB_USERNAME}</username>
    <password>${env.GITHUB_TOKEN}</password>
</server>
```

Then set environment variables:
```bash
export GITHUB_USERNAME=your-username
export GITHUB_TOKEN=your-token
```

### 3. Add Repository to Your Project

In your project's `pom.xml`, add the repository:

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/rkamradt/vehicleevent</url>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>
```

### 4. Add Dependencies

```xml
<dependencies>
    <dependency>
        <groupId>net.kamradtfamily</groupId>
        <artifactId>vehicleapi</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
    <dependency>
        <groupId>net.kamradtfamily</groupId>
        <artifactId>lotapi</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
</dependencies>
```

### 5. Build Your Project

```bash
mvn clean install
```

Maven will automatically download the packages from GitHub Packages.

## For CI/CD (GitHub Actions)

If you're using GitHub Actions to build projects that consume these packages:

```yaml
- name: Set up JDK 17
  uses: actions/setup-java@v4
  with:
    java-version: '17'
    distribution: 'temurin'
    cache: maven
    server-id: github
    server-username: GITHUB_ACTOR
    server-password: GITHUB_TOKEN

- name: Build with Maven
  run: mvn clean install
  env:
    GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

The `GITHUB_TOKEN` is automatically provided by GitHub Actions.

## Publishing New Versions (Maintainers Only)

### Automatic Publishing via Releases

1. Update version in parent POM if needed
2. Commit and push changes
3. Create a GitHub release:
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```
4. Go to GitHub → Releases → Create a new release
5. Select the tag you just pushed
6. The GitHub Actions workflow will automatically publish the packages

### Manual Publishing

If you have `write:packages` permission:

```bash
# Make sure you have authentication configured in ~/.m2/settings.xml
mvn clean deploy -pl vehicleapi,lotapi -am
```

## Troubleshooting

### "Unauthorized" Error
- Verify your Personal Access Token has `read:packages` scope
- Check that your token is correctly set in `~/.m2/settings.xml`
- Ensure the `<id>github</id>` matches in both settings.xml and pom.xml

### "Not Found" Error
- Verify the package has been published (check GitHub Packages tab)
- Ensure the repository URL is correct
- Check that your GitHub username has access to the repository

### "Failed to deploy" (403/401)
- Your PAT needs `write:packages` scope
- Verify you have write access to the repository

## Package Information

### Available Artifacts

- **vehicleapi**: Vehicle domain events and commands
  - Contains: VehiclePurchaseCommand, VehicleSellCommand, etc.

- **lotapi**: Lot domain events and commands
  - Contains: LotCreateCommand, LotUpdateCommand, etc.

### Version Strategy

- **SNAPSHOT versions**: Development/testing (e.g., `1.0-SNAPSHOT`)
- **Release versions**: Stable releases (e.g., `1.0.0`, `1.1.0`)

We follow [Semantic Versioning](https://semver.org/):
- **MAJOR**: Incompatible API changes
- **MINOR**: Add functionality (backwards-compatible)
- **PATCH**: Backwards-compatible bug fixes

## Viewing Published Packages

Visit: https://github.com/rkamradt/vehicleevent/packages

## Support

For issues or questions, please open an issue in this repository.
