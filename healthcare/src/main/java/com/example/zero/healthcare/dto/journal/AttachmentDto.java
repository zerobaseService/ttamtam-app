package com.example.zero.healthcare.dto.journal;

import com.example.zero.healthcare.Entity.journal.JournalAttachment;
import lombok.Getter;

@Getter
public class AttachmentDto {

    private final Long id;
    private final String imageUrl;
    private final Integer displayOrder;

    public AttachmentDto(JournalAttachment attachment) {
        this.id = attachment.getId();
        this.imageUrl = attachment.getImageUrl();
        this.displayOrder = attachment.getDisplayOrder();
    }
}
