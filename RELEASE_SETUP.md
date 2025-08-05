# Release Setup Instructions

## GitHub Secrets Configuration

To enable signed release builds, you need to add the following secrets to your GitHub repository:

### Required Secrets:

1. **`SIGNING_KEYSTORE_BASE64`** - The base64 encoded keystore file
2. **`SIGNING_KEY_ALIAS`** - The alias used when creating the keystore (value: `okane`)
3. **`SIGNING_KEY_PASSWORD`** - The password for the key alias
4. **`SIGNING_STORE_PASSWORD`** - The password for the keystore file

### How to add secrets:

1. Go to your GitHub repository
2. Click **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret**
4. Add each secret with the corresponding values from your keystore creation process

### Keystore Information:
- **Alias**: `okane`
- **Keystore file**: `okane-release-key.keystore` (should be converted to base64)
- **Validity**: 10,000 days
- **Key algorithm**: RSA
- **Key size**: 2048 bits

### To convert keystore to base64:
```bash
# On Windows
certutil -encode okane-release-key.keystore okane-release-key.keystore.b64

# On Linux/Mac
base64 -i okane-release-key.keystore -o okane-release-key.keystore.b64
```

### How to create a release:

1. Make your changes and commit them
2. Create and push a version tag:
   ```bash
   git tag v0.1.0 -m "release: adds CI/CD"
   git push origin v0.1.0
   ```
3. The GitHub Action will automatically:
   - Build a signed release APK
   - Create a GitHub release
   - Upload the APK to the release

### Workflow Behavior:
- **Regular commits**: Build debug APK only
- **Tag pushes** (v*): Build signed release APK and create GitHub release
- **Pull requests**: Build debug APK only

### Security Notes:
- **NEVER** commit keystore files to your repository
- Keep your keystore and passwords secure
- The keystore files are already added to `.gitignore`
- Store a backup of your keystore in a secure location

### Build locally with signing:
```bash
# Set environment variables (replace with your actual values)
export SIGNING_KEY_ALIAS=okane
export SIGNING_KEY_PASSWORD=your_key_password
export SIGNING_STORE_FILE=path/to/okane-release-key.keystore
export SIGNING_STORE_PASSWORD=your_store_password

# Build signed release APK
./gradlew assembleRelease
```
