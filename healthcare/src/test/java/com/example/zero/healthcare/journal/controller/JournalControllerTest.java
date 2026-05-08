package com.example.zero.healthcare.journal.controller;

import com.example.zero.healthcare.Entity.DiaryFolder;
import com.example.zero.healthcare.Entity.DiaryFolderMember;
import com.example.zero.healthcare.Entity.User;
import com.example.zero.healthcare.auth.JwtTokenProvider;
import com.example.zero.healthcare.repository.DiaryFolderMemberRepository;
import com.example.zero.healthcare.repository.DiaryFolderRepository;
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
    @Autowired private DiaryFolderRepository folderRepository;
    @Autowired private DiaryFolderMemberRepository memberRepository;

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
                  "startedAt": "2026-04-20T09:00:00",
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
                  "startedAt": "2026-04-20T09:00:00",
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
                  "startedAt": "2026-04-20T09:00:00",
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
                  "startedAt": "2026-04-15T09:00:00",
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
                {"workoutDate": "2026-04-20", "startedAt": "2026-04-20T09:00:00",
                 "preCondition": {"jointMusclePain": 5, "sleepHours": 7, "sleepQuality": 6,
                   "previousFatigue": 4, "overallCondition": 8}, "painRecords": []}""";
        String body2 = """
                {"workoutDate": "2026-04-21", "startedAt": "2026-04-21T09:00:00",
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
                  "startedAt": "2026-04-20T09:00:00",
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

    // ─── GET /{id} 상세 조회 ────────────────────────────────────────────────

    private String createJournalAndGetId() throws Exception {
        String body = """
                {
                  "workoutDate": "2026-04-20",
                  "startedAt": "2026-04-20T09:00:00",
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
                                  "startedAt": "2026-04-25T09:00:00",
                                  "preCondition": {
                                    "jointMusclePain": 5, "sleepHours": 7, "sleepQuality": 6,
                                    "previousFatigue": 4, "overallCondition": 8
                                  }
                                }"""))
                .andExpect(status().isCreated());

        String completeBody = """
                {
                  "workoutDate": "2026-04-25",
                  "startedAt": "2026-04-25T09:00:00",
                  "totalDurationSeconds": 3600,
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
                .andExpect(jsonPath("$.data.postCondition.recordedAt").exists())
                .andExpect(jsonPath("$.data.attachments[0].imageUrl")
                        .value("https://cdn.ttamtam.app/complete.jpg"));
    }

    @Test
    @DisplayName("GET /{id} 상세 조회에 attachments 필드가 포함된다")
    void getJournalDetail_includesAttachments() throws Exception {
        String completeBody = """
                {
                  "workoutDate": "2020-05-01",
                  "startedAt": "2020-05-01T09:00:00",
                  "postCondition": {
                    "jointMusclePain": 6, "intensityFit": 7, "goalAchieved": 8,
                    "dizziness": 2, "mood": 9
                  },
                  "imageUrls": ["https://cdn.ttamtam.app/detail.jpg"]
                }""";

        String response = mockMvc.perform(post("/api/journals/complete")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(completeBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String journalId = com.jayway.jsonpath.JsonPath.read(response, "$.data.journalId").toString();

        mockMvc.perform(get("/api/journals/" + journalId)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.attachments").isArray())
                .andExpect(jsonPath("$.data.attachments[0].imageUrl")
                        .value("https://cdn.ttamtam.app/detail.jpg"));
    }

    @Test
    @DisplayName("POST /complete — PRE 일지 없으면 새 일지를 insert하고 200과 JournalDetailDto를 반환한다")
    void complete_noPreJournal_returns200_andCreated() throws Exception {
        String body = """
                {
                  "workoutDate": "2020-01-01",
                  "startedAt": "2020-01-01T09:00:00",
                  "postCondition": {
                    "jointMusclePain": 6, "intensityFit": 7, "goalAchieved": 8,
                    "dizziness": 2, "mood": 9
                  }
                }""";

        mockMvc.perform(post("/api/journals/complete")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.postCondition").exists());
    }

    @Test
    @DisplayName("POST /complete — postCondition 범위 초과이면 400을 반환한다")
    void complete_invalidPostConditionRange_returns400() throws Exception {
        String body = """
                {
                  "workoutDate": "2026-04-25",
                  "postCondition": {
                    "jointMusclePain": 11, "intensityFit": 7, "goalAchieved": 8,
                    "dizziness": 2, "mood": 9
                  }
                }""";

        mockMvc.perform(post("/api/journals/complete")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    @DisplayName("POST /complete — imageUrls가 5개 초과이면 400을 반환한다")
    void complete_imageUrlsOver5_returns400() throws Exception {
        String body = """
                {
                  "workoutDate": "2026-04-25",
                  "postCondition": {
                    "jointMusclePain": 6, "intensityFit": 7, "goalAchieved": 8,
                    "dizziness": 2, "mood": 9
                  },
                  "imageUrls": [
                    "https://cdn.ttamtam.app/a.jpg", "https://cdn.ttamtam.app/b.jpg",
                    "https://cdn.ttamtam.app/c.jpg", "https://cdn.ttamtam.app/d.jpg",
                    "https://cdn.ttamtam.app/e.jpg", "https://cdn.ttamtam.app/f.jpg"
                  ]
                }""";

        mockMvc.perform(post("/api/journals/complete")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    @DisplayName("POST /complete — 가입하지 않은 folderId이면 403을 반환한다")
    void complete_folderNotMember_returns403() throws Exception {
        DiaryFolder folder = folderRepository.save(DiaryFolder.create("test folder"));

        String body = """
                {
                  "workoutDate": "2026-04-25",
                  "startedAt": "2026-04-25T09:00:00",
                  "folderId": %d,
                  "postCondition": {
                    "jointMusclePain": 6, "intensityFit": 7, "goalAchieved": 8,
                    "dizziness": 2, "mood": 9
                  }
                }""".formatted(folder.getId());

        mockMvc.perform(post("/api/journals/complete")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    @DisplayName("POST /complete — JWT 없이 요청하면 401을 반환한다")
    void complete_withoutJwt_returns401() throws Exception {
        String body = """
                {
                  "workoutDate": "2026-04-25",
                  "postCondition": {
                    "jointMusclePain": 6, "intensityFit": 7, "goalAchieved": 8,
                    "dizziness": 2, "mood": 9
                  }
                }""";

        mockMvc.perform(post("/api/journals/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    // ─── PHASE S1: GET 엔드포인트 권한 검증 ───────────────────────────────────

    @Test
    @DisplayName("JWT의 userId 기준으로 조회되며 query param userId는 무시된다")
    void getMyJournals_usesJwtUserId_ignoresQueryParam() throws Exception {
        mockMvc.perform(post("/api/journals")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workoutDate": "2026-04-20", "startedAt": "2026-04-20T09:00:00",
                                 "preCondition": {"jointMusclePain": 5, "sleepHours": 7, "sleepQuality": 6,
                                   "previousFatigue": 4, "overallCondition": 8}}"""))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/journals")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken)
                        .param("userId", "99999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("JWT 없이 일지 목록 조회 시 401을 반환한다")
    void getMyJournals_withoutJwt_returns401() throws Exception {
        mockMvc.perform(get("/api/journals"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("본인 일지 상세 조회 시 200을 반환한다")
    void getJournalDetail_ownJournal_returns200() throws Exception {
        String journalId = createJournalAndGetId();

        mockMvc.perform(get("/api/journals/" + journalId)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("다른 사용자의 일지(폴더 없음) 상세 조회 시 403을 반환한다")
    void getJournalDetail_otherUserJournal_returns403() throws Exception {
        String journalId = createJournalAndGetId();

        User userB = userRepository.save(new User("ctrl-other-" + UUID.randomUUID() + "@test.com", "token", "other"));
        String tokenB = "Bearer " + jwtTokenProvider.generateToken(userB.getId());

        mockMvc.perform(get("/api/journals/" + journalId)
                        .header(HttpHeaders.AUTHORIZATION, tokenB))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("JOURNAL_FORBIDDEN"));
    }

    @Test
    @DisplayName("존재하지 않는 일지 상세 조회 시 404를 반환한다")
    void getJournalDetail_unknownJournal_returns404() throws Exception {
        mockMvc.perform(get("/api/journals/99999999")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("JOURNAL_NOT_FOUND"));
    }

    @Test
    @DisplayName("JWT 없이 일지 상세 조회 시 401을 반환한다")
    void getJournalDetail_withoutJwt_returns401() throws Exception {
        mockMvc.perform(get("/api/journals/1"))
                .andExpect(status().isUnauthorized());
    }

    // ─── PHASE S3: createJournal 폴더 멤버십 검증 ─────────────────────────────

    @Test
    @DisplayName("가입하지 않은 folderId로 일지 생성 시 403을 반환한다")
    void create_folderNotMember_returns403() throws Exception {
        DiaryFolder folder = folderRepository.save(DiaryFolder.create("test folder"));

        String body = """
                {
                  "workoutDate": "2026-04-20",
                  "startedAt": "2026-04-20T09:00:00",
                  "folderId": %d,
                  "preCondition": {
                    "jointMusclePain": 5, "sleepHours": 7, "sleepQuality": 6,
                    "previousFatigue": 4, "overallCondition": 8
                  }
                }""".formatted(folder.getId());

        mockMvc.perform(post("/api/journals")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    @DisplayName("활성 멤버인 folderId로 일지 생성 시 201을 반환한다")
    void create_folderActiveMember_returns201() throws Exception {
        DiaryFolder folder = folderRepository.save(DiaryFolder.create("test folder"));
        User user = userRepository.findById(userId).orElseThrow();
        memberRepository.save(DiaryFolderMember.join(folder, user));

        String body = """
                {
                  "workoutDate": "2026-04-20",
                  "startedAt": "2026-04-20T09:00:00",
                  "folderId": %d,
                  "preCondition": {
                    "jointMusclePain": 5, "sleepHours": 7, "sleepQuality": 6,
                    "previousFatigue": 4, "overallCondition": 8
                  }
                }""".formatted(folder.getId());

        mockMvc.perform(post("/api/journals")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /complete 응답에 startedAt과 totalDurationSeconds가 포함된다")
    void complete_includesTimerFieldsInResponse() throws Exception {
        String body = """
                {
                  "workoutDate": "2020-03-01",
                  "startedAt": "2020-03-01T09:00:00",
                  "totalDurationSeconds": 3600,
                  "postCondition": {
                    "jointMusclePain": 6, "intensityFit": 7, "goalAchieved": 8,
                    "dizziness": 2, "mood": 9
                  }
                }""";

        mockMvc.perform(post("/api/journals/complete")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.startedAt").exists())
                .andExpect(jsonPath("$.data.totalDurationSeconds").value(3600));
    }

    @Test
    @DisplayName("POST /complete — exercises 포함 시 200과 운동 기록이 포함된 JournalDetailDto를 반환한다")
    void complete_withExercises_returns200AndDetail() throws Exception {
        String body = """
                {
                  "workoutDate": "2020-02-01",
                  "startedAt": "2020-02-01T09:00:00",
                  "postCondition": {
                    "jointMusclePain": 6, "intensityFit": 7, "goalAchieved": 8,
                    "dizziness": 2, "mood": 9
                  },
                  "exercises": [
                    { "exerciseName": "스쿼트", "displayOrder": 1,
                      "sets": [{ "setNumber": 1, "reps": 10, "weightKg": 80.0 }] }
                  ]
                }""";

        mockMvc.perform(post("/api/journals/complete")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.exercises[0].exerciseName").value("스쿼트"))
                .andExpect(jsonPath("$.data.exercises[0].sets[0].reps").value(10));
    }
}
