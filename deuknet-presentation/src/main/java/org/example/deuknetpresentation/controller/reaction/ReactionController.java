package org.example.deuknetpresentation.controller.reaction;

import org.example.deuknetapplication.port.in.reaction.AddReactionUseCase;
import org.example.deuknetapplication.port.in.reaction.RemoveReactionUseCase;
import org.example.deuknetdomain.domain.reaction.ReactionType;
import org.example.deuknetpresentation.controller.reaction.dto.AddReactionRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/posts/{postId}/reactions")
public class ReactionController implements ReactionApi {

    private final AddReactionUseCase addReactionUseCase;
    private final RemoveReactionUseCase removeReactionUseCase;

    public ReactionController(
            AddReactionUseCase addReactionUseCase,
            RemoveReactionUseCase removeReactionUseCase
    ) {
        this.addReactionUseCase = addReactionUseCase;
        this.removeReactionUseCase = removeReactionUseCase;
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UUID addReaction(@PathVariable UUID postId, @RequestBody AddReactionRequest request) {
        AddReactionUseCase.AddReactionCommand command = new AddReactionUseCase.AddReactionCommand(
                postId,
                ReactionType.valueOf(request.getReactionType())
        );

        return addReactionUseCase.addReaction(command);
    }

    @Override
    @DeleteMapping("/{reactionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeReaction(@PathVariable UUID reactionId) {
        removeReactionUseCase.removeReaction(reactionId);
    }
}
