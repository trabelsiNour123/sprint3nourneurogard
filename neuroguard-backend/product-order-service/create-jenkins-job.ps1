Param(
    [string]$JenkinsUrl = "https://jenkins.example.com",
    [string]$User = "username",
    [string]$ApiToken = "apitoken",
    [string]$JobName = "product-order-service-docker",
    [string]$JobXmlPath = "jenkins-job-docker.xml",
    [switch]$UseCli
)

function Show-CLI-Command {
    Write-Host "Jenkins CLI command (example):" -ForegroundColor Cyan
    Write-Host "java -jar jenkins-cli.jar -s $JenkinsUrl -auth $User:`$ApiToken create-job $JobName < $JobXmlPath" -ForegroundColor Yellow
}

if ($UseCli) {
    Show-CLI-Command
    exit 0
}

# REST approach (PowerShell)
try {
    $cred = "$User:$ApiToken"
    $base64AuthInfo = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes($cred))
    $headers = @{ Authorization = "Basic $base64AuthInfo" }

    # Get crumb (CSRF protection)
    $crumbUrl = "$JenkinsUrl/crumbIssuer/api/xml?xpath=concat(//crumbRequestField,':',//crumb)"
    Write-Host "Fetching Jenkins crumb from $crumbUrl" -ForegroundColor Cyan
    $crumb = Invoke-RestMethod -Uri $crumbUrl -Headers $headers -Method Get -ErrorAction Stop

    if (-not $crumb) {
        Write-Host "No crumb returned; attempting to create job without crumb..." -ForegroundColor Yellow
        $resp = Invoke-RestMethod -Uri "$JenkinsUrl/createItem?name=$JobName" -Headers $headers -Method Post -InFile $JobXmlPath -ContentType 'application/xml' -ErrorAction Stop
    } else {
        $parts = $crumb -split ":"
        $crumbField = $parts[0]
        $crumbValue = $parts[1]
        $headers[$crumbField] = $crumbValue

        Write-Host "Creating job '$JobName' at $JenkinsUrl using XML file $JobXmlPath" -ForegroundColor Cyan
        $resp = Invoke-RestMethod -Uri "$JenkinsUrl/createItem?name=$JobName" -Headers $headers -Method Post -InFile $JobXmlPath -ContentType 'application/xml' -ErrorAction Stop
    }

    Write-Host "Job created (or updated) successfully: $JobName" -ForegroundColor Green
} catch {
    Write-Error "Failed to create job: $_"
    Write-Host "If you prefer, run the CLI command instead (use -UseCli)." -ForegroundColor Yellow
}
