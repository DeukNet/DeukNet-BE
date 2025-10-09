# Post UseCase 파일들 수정
$files = Get-ChildItem "C:\DeukNet\deuknet-application\src\main\java\org\example\deuknetapplication\usecase\post\*.java"
foreach ($file in $files) {
    $content = Get-Content $file.FullName -Raw
    # 패키지 변경
    $content = $content -replace 'package org\.example\.deuknetapplication\.port\.in\.post;', 'package org.example.deuknetapplication.usecase.post;'
    $content = $content -replace 'package org\.example\.deuknetapplication\.service\.post;', 'package org.example.deuknetapplication.usecase.post;'
    # 클래스명 변경
    $content = $content -replace 'class (\w+)Service ', 'class $1UseCaseImpl '
    Set-Content $file.FullName $content -NoNewline
}

# Comment UseCase 파일들 수정
$files = Get-ChildItem "C:\DeukNet\deuknet-application\src\main\java\org\example\deuknetapplication\usecase\comment\*.java"
foreach ($file in $files) {
    $content = Get-Content $file.FullName -Raw
    $content = $content -replace 'package org\.example\.deuknetapplication\.port\.in\.post;', 'package org.example.deuknetapplication.usecase.comment;'
    $content = $content -replace 'package org\.example\.deuknetapplication\.service\.post;', 'package org.example.deuknetapplication.usecase.comment;'
    $content = $content -replace 'class (\w+)Service ', 'class $1UseCaseImpl '
    Set-Content $file.FullName $content -NoNewline
}

# Reaction UseCase 파일들 수정
$files = Get-ChildItem "C:\DeukNet\deuknet-application\src\main\java\org\example\deuknetapplication\usecase\reaction\*.java"
foreach ($file in $files) {
    $content = Get-Content $file.FullName -Raw
    $content = $content -replace 'package org\.example\.deuknetapplication\.port\.in\.post;', 'package org.example.deuknetapplication.usecase.reaction;'
    $content = $content -replace 'package org\.example\.deuknetapplication\.service\.post;', 'package org.example.deuknetapplication.usecase.reaction;'
    $content = $content -replace 'class (\w+)Service ', 'class $1UseCaseImpl '
    Set-Content $file.FullName $content -NoNewline
}

Write-Host "Package refactoring completed!"
