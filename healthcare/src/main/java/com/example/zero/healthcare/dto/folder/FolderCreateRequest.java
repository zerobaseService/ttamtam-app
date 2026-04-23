package com.example.zero.healthcare.dto.folder;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FolderCreateRequest {
    @NotBlank
    private String name;
}
