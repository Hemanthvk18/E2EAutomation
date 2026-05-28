# 🎯 Git Best Practices Cheat Sheet

## BEFORE YOU COMMIT - Run This!

```powershell
# Step 1: See what you're about to commit
git status

# Step 2: Preview changes
git diff --cached --name-only

# Step 3: Check for unwanted files
git diff --cached | Select-String "\.class"  # Should show NOTHING
```

---

## ✅ SAFE TO COMMIT

```
✅ .java files (source code)
✅ .feature files (Cucumber features)
✅ .xml files (pom.xml, feature files XML)
✅ .properties files (config files)
✅ .md files (documentation)
✅ .yml/.yaml files (GitHub Actions workflows)
✅ .gitignore file
✅ gradle/Maven wrapper files
```

---

## ❌ NEVER COMMIT

```
❌ *.class files (compiled Java)
❌ target/ directory (Maven builds)
❌ build/ directory
❌ out/ directory
❌ *.log files
❌ test-output/ directory
❌ allure-reports/ directory
❌ allure-results/ directory
❌ .idea/ directory
❌ .vscode/ directory
❌ node_modules/
❌ .DS_Store
```

---

## 🔧 If You Accidentally Staged Wrong Files

```powershell
# Unstage .class files
git reset HEAD "*.class"

# Unstage entire test-output directory
git reset HEAD test-output/

# Unstage and see status
git reset HEAD .
git status
```

---

## 🔍 Quick Audits

### Check your .gitignore is working:
```powershell
# Should show: target/
git check-ignore -v target/

# Should show: *.class
git check-ignore -v MyFile.class
```

### Find what files are taking space:
```powershell
# Show git repository size
git count-objects -vH

# Show largest files in history
git rev-list --all --objects | Sort-Object { [int]$_.Split()[1] } -Descending | Select-Object -First 10
```

### List all tracked files by directory:
```powershell
git ls-files | ForEach-Object { $_.Split('/')[0] } | Group-Object | Sort-Object Count -Descending
```

---

## 🚨 Emergency Fixes

### I committed a .class file. Now what?

```powershell
# Option 1: Remove from latest commit only (if not pushed)
git reset --soft HEAD~1
git reset HEAD "*.class"
git commit -m "Remove .class files"

# Option 2: Remove from tracking (file stays locally)
git rm --cached "*.class"
git commit -m "Remove .class files from git"

# Option 3: Completely undo the commit (dangerous!)
git revert HEAD
```

### Repository has 500 MB of build files in history

```powershell
# Clean up git database
git gc --aggressive

# Check if it helped
git count-objects -vH
```

---

## 📋 Pre-Commit Checklist

```
Before `git add`:
  [ ] Did I compile? (mvn compile)
  [ ] Did I test? (mvn test)
  [ ] Did I clean? (mvn clean)

Before `git commit`:
  [ ] Run: git status
  [ ] Run: git diff --cached --name-only
  [ ] Any .class files? (should be NO)
  [ ] Any target/ files? (should be NO)
  [ ] Any test-output/ files? (should be NO)
  [ ] Only source files? (should be YES)

Before `git push`:
  [ ] Reviewed all changes
  [ ] Tests passing locally
  [ ] No merge conflicts expected
```

---

## 🎓 Why This Matters

| Problem | Cause | Symptom | Fix |
|---------|-------|---------|-----|
| Huge repo size | Tracked build files | Clone is 100+ MB | Remove from git |
| Merge conflicts | Binary .class files committed | Can't merge | Use .gitignore |
| Stale tests fail | Old test outputs tracked | Tests don't match code | Remove test files |
| GitHub Actions slow | Large initial clone | Pipeline takes 5+ min to checkout | Clean repository |
| Confusing history | Multiple devs commit .class files | 50 MB commits visible | Rewrite history |

---

## 🚀 Your GitHub Actions is Protected By:

✅ `mvn clean test` - Removes ALL artifacts first
✅ Fresh Ubuntu container - No local cache
✅ Proper .gitignore - Build files never committed
✅ Actions use latest code - Not cached builds

**Result: Your CI/CD is SAFE! 🎉**

---

## 📞 Quick Problem Solver

**Q: I see duplicate files with .class and .java**
A: The .java is source, .class is compiled. Only commit .java.

**Q: Should I commit test results?**
A: NO. Tests run in CI/CD, not stored in git.

**Q: Will unwanted files break GitHub Actions?**
A: Not with `mvn clean`, but they bloat history.

**Q: How do I check repo health?**
A: Run: `git ls-files | Where-Object {$_ -match '\.class|^target'}`

**Q: Is my repository too large?**
A: Run: `git count-objects -vH` and check first number.

---

## 📝 Summary Commands (Copy & Paste Ready)

```powershell
# Daily use
git status
git diff --cached --name-only
git reset HEAD filename
git commit -m "message"
git push

# Weekly audit
git ls-files | Where-Object {$_ -match '\.class$|^target/|^build/'}
git count-objects -vH

# Before major commits
git status
git diff --cached | Select-String "\.class"  # Should be empty
git ls-files | Measure-Object -Line
```

---

**Remember:** Your `.gitignore` file is your best friend! ✨

