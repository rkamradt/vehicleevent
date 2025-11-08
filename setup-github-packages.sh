#!/bin/bash
# Setup script for GitHub Packages authentication

set -e

echo "🔧 GitHub Packages Setup for vehicleevent APIs"
echo "================================================"
echo ""

# Check if settings.xml exists
SETTINGS_FILE="$HOME/.m2/settings.xml"
BACKUP_FILE="$HOME/.m2/settings.xml.backup.$(date +%Y%m%d_%H%M%S)"

if [ -f "$SETTINGS_FILE" ]; then
    echo "⚠️  Found existing settings.xml"
    read -p "Do you want to back it up? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        cp "$SETTINGS_FILE" "$BACKUP_FILE"
        echo "✅ Backed up to: $BACKUP_FILE"
    fi
fi

# Get GitHub credentials
echo ""
echo "Please provide your GitHub credentials:"
read -p "GitHub Username: " GITHUB_USER

echo "Please enter your GitHub Personal Access Token (PAT):"
echo "  - Go to: https://github.com/settings/tokens"
echo "  - Click: Generate new token (classic)"
echo "  - Select: read:packages scope"
echo ""
read -s -p "GitHub Token (input hidden): " GITHUB_TOKEN
echo ""

if [ -z "$GITHUB_USER" ] || [ -z "$GITHUB_TOKEN" ]; then
    echo "❌ Error: Username and token are required"
    exit 1
fi

# Create .m2 directory if it doesn't exist
mkdir -p "$HOME/.m2"

# Create or update settings.xml
cat > "$SETTINGS_FILE" << EOF
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
echo "✅ Configuration complete!"
echo ""
echo "📝 Created: $SETTINGS_FILE"
echo ""
echo "You can now use vehicleapi and lotapi packages in your projects."
echo "See PACKAGES.md for more information."
echo ""
echo "🔒 Security reminder: Keep your token secure and never commit it to version control!"
