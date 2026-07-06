Get-Content ".env" | ForEach-Object {
    $line = $_.Trim()

    if ($line -eq "" -or $line.StartsWith("#")) {
        return
    }

    $name, $value = $line.Split("=", 2)

    if ($name -and $value) {
        [Environment]::SetEnvironmentVariable($name.Trim(), $value.Trim(), "Process")
    }
}

Write-Host "Loaded .env variables into current PowerShell process."