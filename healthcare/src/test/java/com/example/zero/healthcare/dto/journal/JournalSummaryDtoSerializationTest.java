package com.example.zero.healthcare.dto.journal;

import com.example.zero.healthcare.Entity.journal.WorkoutJournal;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JournalSummaryDtoSerializationTest {

    // Spring Boot JacksonAutoConfiguration이 적용하는 설정과 동일하게 구성
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private WorkoutJournal stubJournal() {
        WorkoutJournal journal = mock(WorkoutJournal.class);
        when(journal.getId()).thenReturn(1L);
        when(journal.getWorkoutDate()).thenReturn(LocalDate.of(2026, 4, 20));
        when(journal.getCreatedAt()).thenReturn(LocalDateTime.of(2026, 4, 20, 9, 30, 0));
        when(journal.isCompleted()).thenReturn(false);
        when(journal.getPreCondition()).thenReturn(null);
        when(journal.getContent()).thenReturn(null);
        return journal;
    }

    @Test
    @DisplayName("workoutDate는 'yyyy-MM-dd' 형식의 문자열로 직렬화된다")
    void workoutDate_serializesAsIsoDateString() throws Exception {
        JournalSummaryDto dto = new JournalSummaryDto(stubJournal());
        JsonNode node = objectMapper.readTree(objectMapper.writeValueAsString(dto));

        assertThat(node.get("workoutDate").asText()).isEqualTo("2026-04-20");
    }

    @Test
    @DisplayName("createdAt은 'yyyy-MM-dd'T'HH:mm:ss' 형식의 문자열로 직렬화된다")
    void createdAt_serializesAsIsoDateTimeString() throws Exception {
        JournalSummaryDto dto = new JournalSummaryDto(stubJournal());
        JsonNode node = objectMapper.readTree(objectMapper.writeValueAsString(dto));

        String createdAt = node.get("createdAt").asText();
        assertThat(createdAt).startsWith("2026-04-20T09:30:00");
    }

    @Test
    @DisplayName("createdAt은 배열이 아닌 문자열 타입으로 반환된다")
    void createdAt_isStringNotArray() throws Exception {
        JournalSummaryDto dto = new JournalSummaryDto(stubJournal());
        JsonNode node = objectMapper.readTree(objectMapper.writeValueAsString(dto));

        assertThat(node.get("createdAt").isTextual()).isTrue();
    }
}
