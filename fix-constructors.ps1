# 생성자 이름 수정
$files = Get-ChildItem "C:\DeukNet\deuknet-application\src\main\java\org\example\deuknetapplication\usecase" -Recurse -Filter "*UseCaseImpl.java"
foreach ($file in $files) {
    $content = Get-Content $file.FullName -Raw -Encoding UTF8
    # Service 생성자를 UseCaseImpl 생성자로 변경
    $content = $content -replace 'public (\w+)Service\(', 'public $1UseCaseImpl('
    Set-Content $file.FullName $content -NoNewline -Encoding UTF8
}

Write-Host "Constructor refactoring completed!"
