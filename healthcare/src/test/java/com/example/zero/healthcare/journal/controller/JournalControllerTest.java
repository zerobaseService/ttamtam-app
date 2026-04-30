package com.example.zero.healthcare.journal.controller;

import com.example.zero.healthcare.Entity.User;
import com.example.zero.healthcare.auth.JwtTokenProvider;
import com.example.zero.healthcare.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class JournalControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private JwtTokenProvider jwtTokenProvider;

    private Long userId;
    private String bearerToken;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(new User("ctrl-" + UUID.randomUUID() + "@test.com", "token", "tester"));
        userId = user.getId();
        bearerToken = "Bearer " + jwtTokenProvider.generateToken(userId);
    }

    @Test
    @DisplayName("유효한 요청으로 일지 생성 시 201과 journalId를 반환한다")
    void createJournal_validRequest_returns201() throws Exception {
        String body = """
                {
                  "workoutDate": "2026-04-20",
                  "folderId": null,
                  "preCondition": {
                    "jointMusclePain": 5, "sleepHours": 7, "sleepQuality": 6,
                    "previousFatigue": 4, "overallCondition": 8
                  },
                  "painRecords": [
                    {"bodyPart": "SHOULDER", "side": "LEFT", "painLevel": 7}
                  ]
                }""";

        mockMvc.perform(post("/api/journals")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.journalId").exists());
    }

    @Test
    @DisplayName("painRecords가 없어도 일지 생성이 성공한다")
    void createJournal_emptyPainRecords_returns201() throws Exception {
        String body = """
                {
                  "workoutDate": "2026-04-20",
                  "preCondition": {
                    "jointMusclePain": 5, "sleepHours": 7, "sleepQuality": 6,
                    "previousFatigue": 4, "overallCondition": 8
                  },
                  "painRecords": []
                }""";

        mockMvc.perform(post("/api/journals")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("preCondition 값이 범위 초과이면 400을 반환한다")
    void createJournal_outOfRangeCondition_returns400() throws Exception {
        String body = """
                {
                  "workoutDate": "2026-04-20",
                  "preCondition": {
                    "jointMusclePain": 11, "sleepHours": 7, "sleepQuality": 6,
                    "previousFatigue": 4, "overallCondition": 8
                  },
                  "painRecords": []
                }""";

        mockMvc.perform(post("/api/journals")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    @DisplayName("중복 bodyPart+side가 있으면 400을 반환한다")
    void createJournal_duplicatePainRecord_returns400() throws Exception {
        String body = """
                {
                  "workoutDate": "2026-04-20",
                  "preCondition": {
                    "jointMusclePain": 5, "sleepHours": 7, "sleepQuality": 6,
                    "previousFatigue": 4, "overallCondition": 8
                  },
                  "painRecords": [
                    {"bodyPart": "KNEE", "side": "LEFT", "painLevel": 5},
                    {"bodyPart": "KNEE", "side": "LEFT", "painLevel": 3}
                  ]
                }""";

        mockMvc.perform(post("/api/journals")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    @DisplayName("존재하지 않는 userId의 JWT로 요청하면 401을 반환한다")
    void createJournal_jwtWithUnknownUser_returns401() throws Exception {
        String tokenForUnknownUser = "Bearer " + jwtTokenProvider.generateToken(99999L);
        String body = """
                {
                  "workoutDate": "2026-04-20",
                  "preCondition": {
                    "jointMusclePain": 5, "sleepHours": 7, "sleepQuality": 6,
                    "previousFatigue": 4, "overallCondition": 8
                  },
                  "painRecords": []
                }""";

        mockMvc.perform(post("/api/journals")
                        .header(HttpHeaders.AUTHORIZATION, tokenForUnknownUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("workoutDate가 없으면 400을 반환한다")
    void createJournal_missingWorkoutDate_returns400() throws Exception {
        String body = """
                {
                  "preCondition": {
                    "jointMusclePain": 5, "sleepHours": 7, "sleepQuality": 6,
                    "previousFatigue": 4, "overallCondition": 8
                  },
                  "painRecords": []
                }""";

        mockMvc.perform(post("/api/journals")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    @DisplayName("미래 날짜 workoutDate이면 400을 반환한다")
    void createJournal_futureWorkoutDate_returns400() throws Exception {
        String body = """
                {
                  "workoutDate": "2099-01-01",
                  "preCondition": {
                    "jointMusclePain": 5, "sleepHours": 7, "sleepQuality": 6,
                    "previousFatigue": 4, "overallCondition": 8
                  },
                  "painRecords": []
                }""";

        mockMvc.perform(post("/api/journals")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    @DisplayName("date 파라미터로 특정일 일지를 조회할 수 있다")
    void getJournals_withDate_returnsFiltered() throws Exception {
        String body = """
                {
                  "workoutDate": "2026-04-20",
                  "preCondition": {
                    "jointMusclePain": 5, "sleepHours": 7, "sleepQuality": 6,
                    "previousFatigue": 4, "overallCondition": 8
                  },
                  "painRecords": []
                }""";
        mockMvc.perform(post("/api/journals")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/journals")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken)
                        .param("userId", String.valueOf(userId))
                        .param("date", "2026-04-20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].workoutDate").value("2026-04-20"));
    }

    @Test
    @DisplayName("from/to 파라미터로 기간 내 일지를 조회할 수 있다")
    void getJournals_withFromTo_returnsRange() throws Exception {
        String body = """
                {
                  "workoutDate": "2026-04-15",
                  "preCondition": {
                    "jointMusclePain": 5, "sleepHours": 7, "sleepQuality": 6,
                    "previousFatigue": 4, "overallCondition": 8
                  },
                  "painRecords": []
                }""";
        mockMvc.perform(post("/api/journals")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/journals")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken)
                        .param("userId", String.valueOf(userId))
                        .param("from", "2026-04-01")
                        .param("to", "2026-04-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].workoutDate").value("2026-04-15"));
    }

    @Test
    @DisplayName("파라미터 없이 조회하면 전체 일지를 반환한다")
    void getJournals_noParams_returnsAll() throws Exception {
        String body1 = """
                {"workoutDate": "2026-04-20",
                 "preCondition": {"jointMusclePain": 5, "sleepHours": 7, "sleepQuality": 6,
                   "previousFatigue": 4, "overallCondition": 8}, "painRecords": []}""";
        String body2 = """
                {"workoutDate": "2026-04-21",
                 "preCondition": {"jointMusclePain": 5, "sleepHours": 7, "sleepQuality": 6,
                   "previousFatigue": 4, "overallCondition": 8}, "painRecords": []}""";

        mockMvc.perform(post("/api/journals").header(HttpHeaders.AUTHORIZATION, bearerToken).contentType(MediaType.APPLICATION_JSON).content(body1));
        mockMvc.perform(post("/api/journals").header(HttpHeaders.AUTHORIZATION, bearerToken).contentType(MediaType.APPLICATION_JSON).content(body2));

        mockMvc.perform(get("/api/journals")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken)
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("date와 from/to를 동시 지정하면 400을 반환한다")
    void getJournals_dateAndRangeTogether_returns400() throws Exception {
        mockMvc.perform(get("/api/journals")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken)
                        .param("userId", String.valueOf(userId))
                        .param("date", "2026-04-20")
                        .param("from", "2026-04-01")
                        .param("to", "2026-04-30"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("from만 지정하고 to가 없으면 400을 반환한다")
    void getJournals_onlyFromWithoutTo_returns400() throws Exception {
        mockMvc.perform(get("/api/journals")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken)
                        .param("userId", String.valueOf(userId))
                        .param("from", "2026-04-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("잘못된 날짜 포맷은 400을 반환한다 (500 아님)")
    void getJournals_invalidDateFormat_returns400() throws Exception {
        mockMvc.perform(get("/api/journals")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken)
                        .param("userId", String.valueOf(userId))
                        .param("date", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("body에 userId 없이 JWT만으로 일지를 생성할 수 있다")
    void createJournal_userIdFromJwtOnly_returns201() throws Exception {
        String body = """
                {
                  "workoutDate": "2026-04-20",
                  "preCondition": {
                    "jointMusclePain": 5, "sleepHours": 7, "sleepQuality": 6,
                    "previousFatigue": 4, "overallCondition": 8
                  },
                  "painRecords": []
                }""";

        mockMvc.perform(post("/api/journals")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.journalId").exists());
    }

    @Test
    @DisplayName("JWT 없이 일지 생성 요청 시 401을 반환한다")
    void createJournal_withoutJwt_returns401() throws Exception {
        String body = """
                {
                  "workoutDate": "2026-04-20",
                  "preCondition": {
                    "jointMusclePain": 5, "sleepHours": 7, "sleepQuality": 6,
                    "previousFatigue": 4, "overallCondition": 8
                  },
                  "painRecords": []
                }""";

        mockMvc.perform(post("/api/journals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    // ─── PATCH /{id}/post ──────────────────────────────────────────────────

    private String createJournalAndGetId() throws Exception {
        String body = """
                {
                  "workoutDate": "2026-04-20",
                  "preCondition": {
                    "jointMusclePain": 5, "sleepHours": 7, "sleepQuality": 6,
                    "previousFatigue": 4, "overallCondition": 8
                  },
                  "painRecords": []
                }""";
        String response = mockMvc.perform(post("/api/journals")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return com.jayway.jsonpath.JsonPath.read(response, "$.data.journalId").toString();
    }

    @Test
    @DisplayName("PATCH /post 성공 시 200과 JournalDetailDto를 반환한다")
    void updatePost_validRequest_returns200AndDetail() throws Exception {
        String journalId = createJournalAndGetId();
        String body = """
                {
                  "userId": %d,
                  "postCondition": {
                    "jointMusclePain": 6, "intensityFit": 7, "goalAchieved": 8,
                    "dizziness": 2, "mood": 9
                  },
                  "painRecords": [{"bodyPart": "SHOULDER", "side": "LEFT", "painLevel": 5}],
                  "imageUrls": ["https://cdn.ttamtam.app/img.jpg"],
                  "content": "잘했다"
                }""".formatted(userId);

        mockMvc.perform(patch("/api/journals/" + journalId + "/post")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.postRecordedAt").exists())
                .andExpect(jsonPath("$.data.attachments").isArray())
                .andExpect(jsonPath("$.data.attachments[0].imageUrl").value("https://cdn.ttamtam.app/img.jpg"))
                .andExpect(jsonPath("$.data.attachments[0].displayOrder").value(0));
    }

    @Test
    @DisplayName("이미지 6개 이상이면 400을 반환한다")
    void updatePost_sixImages_returns400() throws Exception {
        String journalId = createJournalAndGetId();
        String body = """
                {
                  "userId": %d,
                  "postCondition": {
                    "jointMusclePain": 6, "intensityFit": 7, "goalAchieved": 8,
                    "dizziness": 2, "mood": 9
                  },
                  "imageUrls": [
                    "https://cdn.ttamtam.app/a.jpg",
                    "https://cdn.ttamtam.app/b.jpg",
                    "https://cdn.ttamtam.app/c.jpg",
                    "https://cdn.ttamtam.app/d.jpg",
                    "https://cdn.ttamtam.app/e.jpg",
                    "https://cdn.ttamtam.app/f.jpg"
                  ]
                }""".formatted(userId);

        mockMvc.perform(patch("/api/journals/" + journalId + "/post")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    @DisplayName("이미 기록 완료된 일지에 PATCH /post 호출 시 409를 반환한다")
    void updatePost_alreadyRecorded_returns409() throws Exception {
        String journalId = createJournalAndGetId();
        String body = """
                {
                  "userId": %d,
                  "postCondition": {
                    "jointMusclePain": 6, "intensityFit": 7, "goalAchieved": 8,
                    "dizziness": 2, "mood": 9
                  }
                }""".formatted(userId);

        mockMvc.perform(patch("/api/journals/" + journalId + "/post")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/journals/" + journalId + "/post")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("POST_ALREADY_RECORDED"));
    }

    // ─── POST /complete ────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /complete — PRE-only 일지를 lookup하여 post 기록 완료 후 200과 JournalDetailDto 반환")
    void complete_validRequest_returns200AndDetail() throws Exception {
        mockMvc.perform(post("/api/journals")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "workoutDate": "2026-04-25",
                                  "preCondition": {
                                    "jointMusclePain": 5, "sleepHours": 7, "sleepQuality": 6,
                                    "previousFatigue": 4, "overallCondition": 8
                                  }
                                }"""))
                .andExpect(status().isCreated());

        String completeBody = """
                {
                  "workoutDate": "2026-04-25",
                  "postCondition": {
                    "jointMusclePain": 6, "intensityFit": 7, "goalAchieved": 8,
                    "dizziness": 2, "mood": 9
                  },
                  "imageUrls": ["https://cdn.ttamtam.app/complete.jpg"],
                  "content": "완료"
                }""";

        mockMvc.perform(post("/api/journals/complete")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(completeBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.postRecordedAt").exists())
                .andExpect(jsonPath("$.data.attachments[0].imageUrl")
                        .value("https://cdn.ttamtam.app/complete.jpg"));
    }

    @Test
    @DisplayName("GET /{id} 상세 조회에 attachments 필드가 포함된다")
    void getJournalDetail_includesAttachments() throws Exception {
        String journalId = createJournalAndGetId();
        String patchBody = """
                {
                  "userId": %d,
                  "postCondition": {
                    "jointMusclePain": 6, "intensityFit": 7, "goalAchieved": 8,
                    "dizziness": 2, "mood": 9
                  },
                  "imageUrls": ["https://cdn.ttamtam.app/detail.jpg"]
                }""".formatted(userId);

        mockMvc.perform(patch("/api/journals/" + journalId + "/post")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchBody))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/journals/" + journalId)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.attachments").isArray())
                .andExpect(jsonPath("$.data.attachments[0].imageUrl")
                        .value("https://cdn.ttamtam.app/detail.jpg"));
    }

    @Test
    @DisplayName("POST /complete — PRE-only 일지 없으면 404를 반환한다")
    void complete_noMatchingJournal_returns404() throws Exception {
        String body = """
                {
                  "workoutDate": "2020-01-01",
                  "postCondition": {
                    "jointMusclePain": 6, "intensityFit": 7, "goalAchieved": 8,
                    "dizziness": 2, "mood": 9
                  }
                }""";

        mockMvc.perform(post("/api/journals/complete")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("JOURNAL_NOT_FOUND"));
    }
}
