# GitHub Packages Quick Start

## 🚀 Quick Setup (First Time)

### Option 1: Using Setup Script (Recommended)
```bash
./setup-github-packages.sh
```

### Option 2: Manual Setup
1. Create GitHub Personal Access Token with `read:packages` scope
2. Add to `~/.m2/settings.xml`:
   ```xml
   <settings>
       <servers>
           <server>
               <id>github</id>
               <username>YOUR_GITHUB_USERNAME</username>
               <password>YOUR_GITHUB_TOKEN</password>
           </server>
       </servers>
   </settings>
   ```

## 📦 Publishing Packages (Maintainers)

### Method 1: Create GitHub Release (Automatic)
```bash
git tag v1.0.0
git push origin v1.0.0
# Then create release on GitHub - workflow publishes automatically
```

### Method 2: Manual Deploy
```bash
mvn clean deploy -pl vehicleapi,lotapi -am
```

## 🔨 Using Packages (Consumers)

### 1. Add to your pom.xml:
```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/rkamradt/vehicleevent</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>net.kamradtfamily</groupId>
        <artifactId>vehicleapi</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
</dependencies>
```

### 2. Build:
```bash
mvn clean install
```

## 📋 Common Commands

```bash
# Verify configuration
mvn help:effective-settings

# Test package download
mvn dependency:get -Dartifact=net.kamradtfamily:vehicleapi:1.0-SNAPSHOT

# Publish just API modules
mvn deploy -pl vehicleapi,lotapi -am

# View published packages
open https://github.com/rkamradt/vehicleevent/packages
```

## 🆘 Troubleshooting

| Error | Solution |
|-------|----------|
| `401 Unauthorized` | Check token has correct scopes and is in settings.xml |
| `404 Not Found` | Verify package is published and URL is correct |
| `Failed to deploy` | Token needs `write:packages` scope |

## 📚 Full Documentation

See [PACKAGES.md](PACKAGES.md) for complete documentation.

## 🔗 Useful Links

- [View Packages](https://github.com/rkamradt/vehicleevent/packages)
- [Create PAT](https://github.com/settings/tokens)
- [GitHub Packages Docs](https://docs.github.com/en/packages)
