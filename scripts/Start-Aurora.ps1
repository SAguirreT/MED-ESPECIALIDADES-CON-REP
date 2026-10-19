param([ValidateSet('demo','mysql','xampp')][string]$Mode='demo',[int]$Port=8080,[switch]$Rebuild)
$ErrorActionPreference='Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
if (!$env:JAVA_HOME -or !(Test-Path (Join-Path $env:JAVA_HOME 'bin\java.exe'))) {
    $auroraJdk = Get-ChildItem 'C:\Program Files\Java','C:\Program Files\Eclipse Adoptium' -Directory -ErrorAction SilentlyContinue |
        Where-Object { Test-Path (Join-Path $_.FullName 'bin\javac.exe') } | Sort-Object Name -Descending | Select-Object -First 1
    if ($auroraJdk) { $env:JAVA_HOME=$auroraJdk.FullName }
}
if (!$env:JAVA_HOME) { throw 'Instala JDK 17 o 21 y configura JAVA_HOME.' }
$auroraJava=Join-Path $env:JAVA_HOME 'bin\java.exe'
if (!(Test-Path 'target\medicos-1.0.0.jar') -or $Rebuild) {
    & .\mvnw.cmd -B package
    if ($LASTEXITCODE -ne 0) { throw 'La compilación o las pruebas fallaron. Revisa la salida de Maven.' }
}
if ($Mode -ne 'demo' -and !$env:JWT_SECRET) { throw 'Configura JWT_SECRET y las variables DB_URL, DB_USER, DB_PASSWORD, ADMIN_EMAIL y ADMIN_PASSWORD descritas en README.md.' }
Write-Host "Aurora se abrirá en http://localhost:$Port. Espera el mensaje Started MedicosApplication. Ctrl+C detiene la aplicación."
$auroraArgs=@('-Djdk.net.unixdomain.tmpdir=C:/__aurora_no_unix_socket_dir__','-jar','target/medicos-1.0.0.jar',"--server.port=$Port",'--server.address=127.0.0.1')
if ($Mode -eq 'demo') { $auroraArgs+='--spring.profiles.active=demo' }
if ($Mode -eq 'xampp') { $auroraArgs+='--spring.profiles.active=xampp' }
& $auroraJava @auroraArgs
exit $LASTEXITCODE
