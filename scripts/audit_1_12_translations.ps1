$ErrorActionPreference = 'Stop'

$repositoryRoot = Split-Path -Parent $PSScriptRoot
$languageDirectory = Join-Path $repositoryRoot 'src/main/resources/assets/advancedrocketry/lang'
$english = Get-Content (Join-Path $languageDirectory 'en_us.json') -Raw |
    ConvertFrom-Json -AsHashtable

$locales = [ordered]@{
    de_de = @{ Legacy = 'de_DE'; Expected = 366 }
    es_es = @{ Legacy = 'es_ES'; Expected = 101 }
    fi_fi = @{ Legacy = 'fi_FI'; Expected = 431 }
    fr_fr = @{ Legacy = 'fr_FR'; Expected = 217 }
    ru_ru = @{ Legacy = 'ru_RU'; Expected = 374 }
    uk_ua = @{ Legacy = 'ua_UA'; Expected = 128 }
    zh_cn = @{ Legacy = 'zh_CN'; Expected = 196 }
}

foreach ($locale in $locales.Keys) {
    $path = Join-Path $languageDirectory "$locale.json"
    if (!(Test-Path $path)) {
        throw "Missing modern language file: $path"
    }

    try {
        $translations = Get-Content $path -Raw | ConvertFrom-Json -AsHashtable
    }
    catch {
        throw "Invalid language JSON: $path`: $($_.Exception.Message)"
    }

    $expectedCount = $locales[$locale].Expected
    if ($translations.Count -ne $expectedCount) {
        throw "$locale has $($translations.Count) keys; expected $expectedCount."
    }

    $unknownKeys = @($translations.Keys | Where-Object { !$english.ContainsKey($_) })
    if ($unknownKeys.Count) {
        throw "$locale contains keys absent from en_us.json: $($unknownKeys -join ', ')"
    }

    $legacyPath = "src/main/resources/assets/advancedrocketry/lang/$($locales[$locale].Legacy).lang"
    $legacyLines = & git -C $repositoryRoot show "origin/1.12:$legacyPath"
    if ($LASTEXITCODE -ne 0) {
        throw "Unable to read $legacyPath from origin/1.12."
    }
    $legacyValues = @{}
    foreach ($line in $legacyLines) {
        if ($line -match '^[^#=]+=(.*)$') {
            $legacyValues[$Matches[1]] = $true
        }
    }

    $inventedValues = @(
        $translations.Values |
            Where-Object { !$legacyValues.ContainsKey([string]$_) } |
            Sort-Object -Unique
    )
    if ($inventedValues.Count) {
        throw "$locale contains values not present in its 1.12 language file."
    }

    Write-Host "$locale`: $($translations.Count) restored 1.12 translations"
}

Write-Host "Translation audit passed for $($locales.Count) modern locale files."
