package org.example.deuknetapplication.port.in.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserProfileCommand {
    private String displayName;
    private String bio;
    private String avatarUrl;
}
