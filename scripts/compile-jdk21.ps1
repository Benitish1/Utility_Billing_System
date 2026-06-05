$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.11"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

Write-Host "Using Java:"
java -version

& "C:\Program Files\JetBrains\IntelliJ IDEA 2024.3.2.1\plugins\maven\lib\maven3\bin\mvn.cmd" clean compile -DskipTests
