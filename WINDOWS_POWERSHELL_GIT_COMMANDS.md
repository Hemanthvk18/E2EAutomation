# 🪟 Windows PowerShell Git Commands - Complete Reference

## For Windows Users (PowerShell)

All commands optimized for PowerShell on Windows.

---

## 🚀 QUICK START COMMANDS

### Before Every Commit
```powershell
# See status
git status

# Preview what will be committed
git diff --cached --name-only

# Check for unwanted .class files
$badFiles = git diff --cached | Select-String "\.class"
if ($badFiles) { Write-Host "❌ Found .class files!" } else { Write-Host "✅ All clean!" }
```

### After Commit
```powershell
# Verify it was pushed
git log --oneline -5

# Confirm remote is updated
git remote -v
```

---

## 🔍 AUDIT COMMANDS (Run Regularly)

### Check if .class files are tracked
```powershell
git ls-files | Where-Object {$_ -match '\.class$'}

# Alternative: with count
$classFiles = @(git ls-files | Where-Object {$_ -match '\.class$'})
Write-Host "Found $($classFiles.Count) .class files"
```

### Check if build directories are tracked
```powershell
git ls-files | Where-Object {$_ -match '^target/|^build/|^dist/'}

# Alternative: for specific path
git ls-files | Where-Object {$_ -match '^target/'}
```

### Check if log files are tracked
```powershell
git ls-files | Where-Object {$_ -match '\.log$'}
```

### Check if test outputs are tracked
```powershell
git ls-files | Where-Object {$_ -match 'test-output/|allure-reports/|allure-results/'}
```

### Complete repository audit
```powershell
Write-Host "=== REPOSITORY AUDIT ===" -ForegroundColor Cyan
Write-Host ""
Write-Host ".class files:" -ForegroundColor Yellow
git ls-files | Where-Object {$_ -match '\.class$'} | ForEach-Object { Write-Host "  $_" }

Write-Host ""
Write-Host "Build directories:" -ForegroundColor Yellow
git ls-files | Where-Object {$_ -match '^target/|^build/'} | ForEach-Object { Write-Host "  $_" }

Write-Host ""
Write-Host "Test outputs:" -ForegroundColor Yellow
git ls-files | Where-Object {$_ -match 'test-output|allure'} | ForEach-Object { Write-Host "  $_" }

Write-Host ""
Write-Host "Log files:" -ForegroundColor Yellow
git ls-files | Where-Object {$_ -match '\.log$'} | ForEach-Object { Write-Host "  $_" }

Write-Host ""
Write-Host "Total tracked files: $((git ls-files | Measure-Object -Line).Lines)" -ForegroundColor Green
```

### Check repository size
```powershell
# Total size
git count-objects -vH

# By directory (top 10)
git ls-files | ForEach-Object { $_.Split('/')[0] } | Group-Object | Select-Object @{N='Directory';E={$_.Name}}, @{N='Count';E={$_.Count}} | Sort-Object Count -Descending | Select-Object -First 10
```

---

## 🛠️ FIXING MISTAKES

### I staged .class files, now what?

```powershell
# Remove ALL .class files from staging
git reset HEAD "*.class"

# Verify they're removed
git status

# Or: Remove specific file
git reset HEAD "target/classes/MyClass.class"
```

### I staged test-output files, now what?

```powershell
# Remove entire directory from staging
git reset HEAD test-output/

# Verify
git status
```

### I accidentally added the target/ directory

```powershell
# Remove from staging
git reset HEAD target/

# Or: Remove from git tracking (use after commit)
git rm -r --cached target/
git commit -m "Remove target directory from tracking"
```

### I committed .class files (before push)

```powershell
# Undo the commit (keeps changes staged)
git reset --soft HEAD~1

# Remove unwanted files from staging
git reset HEAD "*.class"

# Re-commit with just source files
git commit -m "Add features (without .class files)"
```

### I committed .class files (already pushed)

```powershell
# Remove from tracking
git rm -r --cached "*.class"

# Commit the removal
git commit -m "Remove .class files from git tracking"

# Push
git push origin master

# Other devs need to clean up
# (They'll have local files, but git won't track them)
```

---

## 📝 GIT STATUS INTERPRETATION

### Understanding `git status` output

```powershell
# Run it
git status

# Output interpretation:

# "Changes to be committed" = Files staged (will be in next commit)
#   Use: git reset HEAD filename (to unstage)

# "Changes not staged for commit" = Modified files (not staged)
#   Use: git add filename (to stage)

# "Untracked files" = New files git doesn't know about
#   Use: git add filename (to track) or add to .gitignore
```

### Color-coded status
```
If you installed Git for Windows with color support:
- Green = Staged (ready to commit)
- Red = Unstaged (not ready)
- Yellow = Untracked (unknown to git)
```

---

## 🔐 PRE-COMMIT SAFETY SCRIPT

Save this as `GitPreCommitCheck.ps1`:

```powershell
# ============================================
# GitPreCommitCheck.ps1
# Run this BEFORE committing
# ============================================

Write-Host "`n=== PRE-COMMIT SAFETY CHECK ===" -ForegroundColor Cyan

$issues = $false

# Check 1: .class files
Write-Host "`n1. Checking for .class files..."
$classFiles = @(git diff --cached | Select-String "\+.*\.class")
if ($classFiles) {
    Write-Host "   ❌ Found .class files! Remove with: git reset HEAD *.class" -ForegroundColor Red
    $issues = $true
} else {
    Write-Host "   ✅ No .class files" -ForegroundColor Green
}

# Check 2: target/ directory
Write-Host "`n2. Checking for build artifacts..."
$buildFiles = @(git diff --cached --name-only | Where-Object {$_ -match '^target/|^build/'})
if ($buildFiles) {
    Write-Host "   ❌ Found build files! Remove with: git reset HEAD target/" -ForegroundColor Red
    $issues = $true
} else {
    Write-Host "   ✅ No build artifacts" -ForegroundColor Green
}

# Check 3: .log files
Write-Host "`n3. Checking for log files..."
$logFiles = @(git diff --cached --name-only | Where-Object {$_ -match '\.log$'})
if ($logFiles) {
    Write-Host "   ❌ Found log files! Remove with: git reset HEAD *.log" -ForegroundColor Red
    $issues = $true
} else {
    Write-Host "   ✅ No log files" -ForegroundColor Green
}

# Check 4: test-output/
Write-Host "`n4. Checking for test outputs..."
$testFiles = @(git diff --cached --name-only | Where-Object {$_ -match 'test-output|allure-results'})
if ($testFiles) {
    Write-Host "   ❌ Found test outputs! Remove with: git reset HEAD test-output/" -ForegroundColor Red
    $issues = $true
} else {
    Write-Host "   ✅ No test outputs" -ForegroundColor Green
}

# Summary
Write-Host "`n" 
if ($issues) {
    Write-Host "❌ Issues found! Fix them before committing." -ForegroundColor Red
    exit 1
} else {
    Write-Host "✅ All checks passed! Safe to commit." -ForegroundColor Green
    
    Write-Host "`nFiles being committed:"
    git diff --cached --name-only | ForEach-Object { Write-Host "  ✓ $_" }
    exit 0
}
```

**How to use:**
```powershell
# Make it executable
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope Process

# Run it
.\GitPreCommitCheck.ps1

# If it says ✅, then safety commit
git commit -m "Your message"
git push
```

---

## 📊 REPORTING COMMANDS

### Generate repository statistics

```powershell
# Files per directory
Write-Host "=== FILES BY DIRECTORY ===" -ForegroundColor Cyan
git ls-files | ForEach-Object { $_.Split('/')[0] } | Group-Object | `
  Select-Object @{N='Directory';E={$_.Name}}, @{N='Files';E={$_.Count}} | `
  Sort-Object Files -Descending | Format-Table -AutoSize

# Total tracked files
Write-Host "`nTotal files in git: $((git ls-files | Measure-Object -Line).Lines)" -ForegroundColor Green

# Repository size
Write-Host "`nRepository size:" -ForegroundColor Green
git count-objects -vH
```

### Show recent changes

```powershell
# Last 10 commits with file count
git log --oneline --name-status -10

# Last commit details
git log --oneline -1 --name-status
```

### Find large files in history

```powershell
# Show top 10 largest files
git rev-list --all --objects | `
  ForEach-Object { 
    $hash, $size, $path = $_.Split(' ')
    [PSCustomObject]@{
      Size = if([int]::TryParse($size, [ref]$null)) { $size } else { 0 }
      Path = $path
    }
  } | `
  Sort-Object { [int]$_.Size } -Descending | `
  Select-Object -First 10 | `
  Format-Table -AutoSize
```

---

## 🔄 SYNC WITH REMOTE

### Push changes

```powershell
# Simple push
git push

# Push to specific branch
git push origin master

# Push and set upstream
git push -u origin feature-branch

# Force push (DANGEROUS - only if you know what you're doing)
git push --force origin master
```

### Pull changes

```powershell
# Simple pull
git pull

# Pull from specific branch
git pull origin master

# Rebase instead of merge
git pull --rebase origin master
```

### Check remote status

```powershell
# Show remote branches
git branch -r

# See what's different from remote
git log origin/master..HEAD --oneline

# See commits ready to push
git log --oneline -5
```

---

## 🐛 TROUBLESHOOTING

### Git not found

```powershell
# Check if git is installed
git --version

# If not, check PATH
$env:PATH -split ';' | Where-Object {$_ -match 'git'}

# Or reinstall Git for Windows from: https://git-scm.com/
```

### Permission denied

```powershell
# Make script executable
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned

# Or run PowerShell as Administrator
# Right-click PowerShell > Run as administrator
```

### Line endings issues (CRLF vs LF)

```powershell
# Check current setting
git config core.autocrlf

# Set for Windows (CR LF)
git config --global core.autocrlf true

# Or per project
git config core.autocrlf true
```

### Case sensitivity issues

```powershell
# Check git config
git config --global core.ignorecase

# If you need case sensitivity
git config core.ignorecase false
```

---

## 📋 STEP-BY-STEP WORKFLOW

### Complete workflow from start to finish

```powershell
# 1. Check status first
Write-Host "Checking current status..." -ForegroundColor Cyan
git status

# 2. Make your changes (edit files in IDE)
# ... (wait for you to edit files) ...

# 3. See what changed
Write-Host "`nFiles changed:" -ForegroundColor Cyan
git status

# 4. Stage your changes
git add src/main/java/MyNewClass.java
git add src/main/resources/config.properties

# 5. Preview what will be committed
Write-Host "`nPreview commit:" -ForegroundColor Cyan
git diff --cached --name-only

# 6. Safety check
Write-Host "`nSafety check:" -ForegroundColor Cyan
.\GitPreCommitCheck.ps1

# 7. Commit
git commit -m "Add new feature: MyNewClass"

# 8. Verify commit
Write-Host "`nYour commit:" -ForegroundColor Cyan
git log --oneline -1 --name-status

# 9. Push
git push origin master

# 10. Verify push
Write-Host "`nVerifying push..." -ForegroundColor Cyan
git log --oneline -1
```

---

## 🎓 Useful Tips for Windows PowerShell

### Create an alias for common commands

```powershell
# Add to your PowerShell profile (type: $PROFILE to find it)
function gs { git status }
function ga { git add @args }
function gc { git commit @args }
function gp { git push }
function gl { git log --oneline -10 }
function gd { git diff --cached --name-only }

# Then you can use:
gs        # instead of git status
ga .      # instead of git add .
gc -m "message"  # instead of git commit -m "message"
```

### Add to git config

```powershell
git config --global alias.st status
git config --global alias.co checkout
git config --global alias.br branch
git config --global alias.ci commit

# Then use:
git st    # git status
git co    # git checkout
```

---

## ✅ Your Maintenance Schedule

### DAILY
```powershell
# Before committing
git status
git diff --cached --name-only
```

### WEEKLY
```powershell
# Full audit
git ls-files | Where-Object {$_ -match '\.class|^target'}
```

### MONTHLY
```powershell
# Size check
git count-objects -vH
```

### QUARTERLY
```powershell
# Cleanup
git gc --aggressive
```

---

## 📞 Quick Problem Solutions

| Problem | Solution |
|---------|----------|
| Want to undo last commit | `git reset --soft HEAD~1` |
| Want to unstage a file | `git reset HEAD filename` |
| Want to discard local changes | `git checkout -- filename` |
| Want to see what will be committed | `git diff --cached --name-only` |
| Want to see all branches | `git branch -a` |
| Want to switch branch | `git checkout branch-name` |
| Want to create new branch | `git checkout -b new-branch-name` |
| Want to merge branch | `git merge branch-name` |

---

**All commands tested on Windows PowerShell v5.1 ✅**

