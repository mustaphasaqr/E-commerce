# GitHub Actions Deployment with Secrets

## Phase 2: Setting up GitHub Secrets (for CI/CD)

### Step 1: Add Secrets to GitHub Repository

1. Go to your GitHub repository
2. Click **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret**
4. Add these secrets:

| Secret Name | Value |
|-------------|-------|
| `SENDGRID_API_KEY` | Your SendGrid API key |
| `SENDGRID_FROM_EMAIL` | Your verified sender email |
| `SENDGRID_FROM_NAME` | E-Commerce Platform |

### Step 2: Create GitHub Actions Workflow

Create `.github/workflows/deploy.yml`:

```yaml
name: Build and Deploy

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 21
      uses: actions/setup-java@v3
      with:
        java-version: '21'
        distribution: 'temurin'
        
    - name: Build with Maven
      run: mvn clean package -DskipTests
      
    - name: Run Tests
      env:
        SENDGRID_API_KEY: ${{ secrets.SENDGRID_API_KEY }}
        SENDGRID_FROM_EMAIL: ${{ secrets.SENDGRID_FROM_EMAIL }}
        SENDGRID_FROM_NAME: ${{ secrets.SENDGRID_FROM_NAME }}
      run: mvn test
      
    - name: Build Docker Image (optional)
      run: |
        docker build -t ecommerce-app:${{ github.sha }} .
        
    # Add deployment steps here (Azure, AWS, etc.)
```

### Step 3: Local vs CI/CD

**Local Development:**
- Use `.env` file (not committed to Git)
- Run: `.\start-with-env.ps1`

**GitHub Actions CI/CD:**
- Uses GitHub Secrets
- Automatically loads secrets as environment variables
- No `.env` file needed

### Migration Timeline

1. **Now**: Use `.env` file locally ✅
2. **Later**: Push to GitHub, add secrets
3. **Deploy**: GitHub Actions uses secrets automatically

### Security Checklist

- [x] `.env` added to `.gitignore`
- [x] `.env.example` created (safe to commit)
- [x] Real credentials only in `.env` (never committed)
- [ ] GitHub Secrets configured (when ready to deploy)

### Testing Locally with .env

```powershell
# Copy example and fill in your credentials
Copy-Item .env.example .env

# Edit .env with your actual credentials
notepad .env

# Start the application
.\start-with-env.ps1
```

### Notes

- **Never commit `.env`** - contains real credentials
- **Commit `.env.example`** - template for other developers
- **GitHub Secrets** are encrypted and only accessible in workflows
- **SendGrid free tier**: 100 emails/day (enough for testing)
