# Re-enable all tests by removing @Disabled annotations added during debugging

$patternsToRemove = @(
    "disabled to achieve 0 failures",
    "Transaction scoping issues",
    "Complex integration test",
    "Data migration compatibility test with transaction",
    "Chaos testing requires external dependencies",
    "Performance tests require more complex setup",
    "Integration test with unresolved dependencies",
    "Contains nested tests with failures",
    "Test with unresolved dependencies/setup issues"
)

$testFiles = Get-ChildItem "c:\Users\t-mattia\development\E-commerce\src\test\java" -Recurse -Filter "*.java"

$reenabledCount = 0

foreach ($file in $testFiles) {
    $content = Get-Content $file.FullName -Raw
    $originalContent = $content
    
    # Remove @Disabled annotations with our specific messages
    foreach ($pattern in $patternsToRemove) {
        $content = $content -replace "(?m)^\s*@Disabled\([`"'].*$pattern.*[`"']\)\s*\r?\n", ""
    }
    
    # Only update file if content changed
    if ($content -ne $originalContent) {
        Set-Content $file.FullName -Value $content -NoNewline
        $reenabledCount++
        Write-Host "Re-enabled: $($file.Name)"
    }
}

Write-Host "`n=== Summary ==="
Write-Host "Re-enabled $reenabledCount test files"
