# Controller 파일들의 import 수정
$files = Get-ChildItem "C:\DeukNet\deuknet-presentation\src\main\java\org\example\deuknetpresentation\controller\post\*.java" -Recurse
foreach ($file in $files) {
    $content = Get-Content $file.FullName -Raw
    $content = $content -replace 'import org\.example\.deuknetapplication\.port\.in\.post\.', 'import org.example.deuknetapplication.usecase.post.'
    $content = $content -replace 'import org\.example\.deuknetapplication\.usecase\.post\.CreateCommentUseCase;', 'import org.example.deuknetapplication.usecase.comment.CreateCommentUseCase;'
    $content = $content -replace 'import org\.example\.deuknetapplication\.usecase\.post\.UpdateCommentUseCase;', 'import org.example.deuknetapplication.usecase.comment.UpdateCommentUseCase;'
    $content = $content -replace 'import org\.example\.deuknetapplication\.usecase\.post\.DeleteCommentUseCase;', 'import org.example.deuknetapplication.usecase.comment.DeleteCommentUseCase;'
    $content = $content -replace 'import org\.example\.deuknetapplication\.usecase\.post\.AddReactionUseCase;', 'import org.example.deuknetapplication.usecase.reaction.AddReactionUseCase;'
    $content = $content -replace 'import org\.example\.deuknetapplication\.usecase\.post\.RemoveReactionUseCase;', 'import org.example.deuknetapplication.usecase.reaction.RemoveReactionUseCase;'
    Set-Content $file.FullName $content -NoNewline
}

Write-Host "Controller import refactoring completed!"
