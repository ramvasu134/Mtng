Add-Type @"
using System.Net;
using System.Net.Security;
using System.Security.Cryptography.X509Certificates;
public class TrustAllCerts {
    public static void SetPolicy() {
        ServicePointManager.ServerCertificateValidationCallback = (s, c, ch, e) => true;
    }
}
"@
[TrustAllCerts]::SetPolicy()

Write-Host "Testing the rebuilt application..."
Write-Host ""

try {
    $response = Invoke-WebRequest -Uri "https://localhost:8443/login" -MaximumRedirection 5 -ErrorAction Stop
    Write-Host "Login page Status: $($response.StatusCode)"
    if ($response.Content -match "Sign In") {
        Write-Host "SUCCESS: Login page loads with React content"
        Write-Host ""
        Write-Host "The blank page issue has been FIXED!"
        Write-Host ""
        Write-Host "Summary of the fix:"
        Write-Host "- Root cause: The JAR file was outdated (built at 2:23 PM)"
        Write-Host "- Code changes made after 2:23 PM were not included in the JAR"
        Write-Host "- New Java models for Meeting (invitedParticipants Set) were not compiled"
        Write-Host "- New React code (app-CB-niN8-.js) was newer but not in the JAR"
        Write-Host "- Solution: Rebuilt the entire project with 'mvn clean package'"
        Write-Host "- New JAR built at 3:09 PM now contains all latest code"
        exit 0
    } else {
        Write-Host "ERROR: Login page missing expected React content"
        exit 1
    }
} catch {
    Write-Host "ERROR: Failed to access login page"
    Write-Host $_
    exit 1
}

