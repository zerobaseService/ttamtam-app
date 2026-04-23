package com.example.zero.healthcare.dto.folder;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class InviteAcceptRequest {
    private Long folderId;
    private String token;
}
