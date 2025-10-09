package org.example.deuknetpresentation.controller.post;

import org.example.deuknetapplication.usecase.reaction.AddReactionUseCase;
import org.example.deuknetapplication.usecase.reaction.RemoveReactionUseCase;
import org.example.deuknetdomain.model.command.reaction.ReactionType;
import org.example.deuknetpresentation.controller.post.dto.AddReactionRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/posts/{postId}/reactions")
public class ReactionController {

    private final AddReactionUseCase addReactionUseCase;
    private final RemoveReactionUseCase removeReactionUseCase;

    public ReactionController(
            AddReactionUseCase addReactionUseCase,
            RemoveReactionUseCase removeReactionUseCase
    ) {
        this.addReactionUseCase = addReactionUseCase;
        this.removeReactionUseCase = removeReactionUseCase;
    }

    @PostMapping
    public ResponseEntity<Void> addReaction(
            @PathVariable UUID postId,
            @RequestBody AddReactionRequest request
    ) {
        AddReactionUseCase.AddReactionCommand command = new AddReactionUseCase.AddReactionCommand(
                postId,
                ReactionType.valueOf(request.getReactionType())
        );
        
        addReactionUseCase.addReaction(command);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{reactionId}")
    public ResponseEntity<Void> removeReaction(@PathVariable UUID reactionId) {
        removeReactionUseCase.removeReaction(reactionId);
        return ResponseEntity.ok().build();
    }
}
