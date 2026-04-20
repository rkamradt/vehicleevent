#!/bin/bash
# Script to configure Maven for GitHub Packages authentication

set -e

echo "Maven GitHub Packages Authentication Setup"
echo "=========================================="
echo ""

# Check if settings.xml already exists
if [ -f ~/.m2/settings.xml ]; then
    echo "⚠️  Warning: ~/.m2/settings.xml already exists"
    read -p "Do you want to back it up and create a new one? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        cp ~/.m2/settings.xml ~/.m2/settings.xml.backup
        echo "✓ Backed up to ~/.m2/settings.xml.backup"
    else
        echo "Aborted. Please manually add the GitHub server configuration."
        exit 0
    fi
fi

# Prompt for credentials
read -p "Enter your GitHub username: " GITHUB_USER
read -sp "Enter your GitHub Personal Access Token (with read:packages scope): " GITHUB_TOKEN
echo ""

# Create settings.xml
mkdir -p ~/.m2
cat > ~/.m2/settings.xml <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
          http://maven.apache.org/xsd/settings-1.0.0.xsd">

  <servers>
    <server>
      <id>github</id>
      <username>${GITHUB_USER}</username>
      <password>${GITHUB_TOKEN}</password>
    </server>
  </servers>

</settings>
EOF

echo ""
echo "✓ Maven settings configured successfully!"
echo ""
echo "You can now build projects that use GitHub Packages."
echo ""
echo "Test it with:"
echo "  cd ~/github/vehicleevent"
echo "  mvn clean compile"
