--- ============================================================
-- IPX 전체 DB 초기화 스크립트
-- ============================================================

-- pgvector 확장 활성화
CREATE EXTENSION IF NOT EXISTS vector;

-- 벡터 검색
CREATE TABLE patent_vectors (
                                id                  BIGSERIAL       NOT NULL,

                                application_number  VARCHAR(20)     NOT NULL UNIQUE,    -- 특허 식별

                                embedding           vector(1024)    NOT NULL,           -- BGE-M3 임베딩 벡터 (1024차원)

                                ipc_codes           TEXT[]          NOT NULL DEFAULT '{}',  -- IPC 코드 배열
                                legal_status        VARCHAR(20)     NOT NULL,               -- 등록상태
                                application_date    DATE            NOT NULL,               -- 출원일자

                                created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
                                updated_at          TIMESTAMP       NOT NULL DEFAULT NOW(),

                                PRIMARY KEY(id)
);

-- 출원번호 조회용
CREATE INDEX idx_pv_app_num ON patent_vectors (application_number);

-- IPC 코드 필터링용 GIN 인덱스 (배열 타입 전용 인덱스)
CREATE INDEX idx_pv_ipc ON patent_vectors USING GIN (ipc_codes);

-- 법적 상태 필터링용
CREATE INDEX idx_pv_legal_status ON patent_vectors (legal_status);

-- 출원일 범위 필터링용
CREATE INDEX idx_pv_app_date ON patent_vectors (application_date);

-- HNSW 벡터 인덱스: 데이터 적재 완료 후 실행
--CREATE INDEX idx_pv_hnsw ON patent_vectors
--    USING hnsw (embedding vector_cosine_ops)
--    WITH (m = 16, ef_construction = 200);

-- ===================================================================
CREATE TABLE sync_history (
                              id                  SERIAL          NOT NULL,
                              job_type            VARCHAR(20)     NOT NULL,           -- 'initial' or 'incremental'
                              query_date_from     DATE,                               -- KIPRIS 조회 시작 날짜
                              query_date_to       DATE            NOT NULL,           -- KIPRIS 조회 종료 날짜 (= 다음 증분의 시작점)
                              records_added       INTEGER         NOT NULL DEFAULT 0, -- 신규 추가 건수
                              status              VARCHAR(20)     NOT NULL,           -- 'success' or 'failed'
                              created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),

                              PRIMARY KEY(id)
);

-- 최근 성공 수집 조회용
CREATE INDEX idx_sh_status ON sync_history (status, created_at DESC);


-- ============================================================
-- Spring JPA 관리 테이블
-- ============================================================

-- 1. users
CREATE TABLE IF NOT EXISTS users (
                                     id              BIGSERIAL       PRIMARY KEY,
                                     email           VARCHAR(255)    NOT NULL UNIQUE,
    password_hash   VARCHAR(255),
    name            VARCHAR(100)    NOT NULL,
    company         VARCHAR(200),
    provider        VARCHAR(20)     NOT NULL DEFAULT 'LOCAL',
    provider_id     VARCHAR(255),
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW()
    );

-- 2. terms_agreements
CREATE TABLE IF NOT EXISTS terms_agreements (
                                                id              BIGSERIAL       PRIMARY KEY,
                                                user_id         BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    terms_type      VARCHAR(50)     NOT NULL,
    terms_version   VARCHAR(20)     NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW()
    );
CREATE INDEX IF NOT EXISTS idx_ta_user ON terms_agreements (user_id);

-- 3. cases
CREATE TABLE IF NOT EXISTS cases (
                                     id                      BIGSERIAL       PRIMARY KEY,
                                     user_id                 BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title                   VARCHAR(500)    NOT NULL,
    applicant_name          VARCHAR(200),
    inventor_name           VARCHAR(200),
    technical_field         TEXT,
    description             TEXT,
    user_input_ipc          TEXT[]          NOT NULL DEFAULT '{}',
    search_completed_at     TIMESTAMP,
    novelty_completed_at    TIMESTAMP,
    inventive_completed_at  TIMESTAMP,
    report_completed_at     TIMESTAMP,
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP       NOT NULL DEFAULT NOW()
    );
CREATE INDEX IF NOT EXISTS idx_cases_user ON cases (user_id, created_at DESC);

-- 4. invention_components
CREATE TABLE IF NOT EXISTS invention_components (
                                                    id              BIGSERIAL       PRIMARY KEY,
                                                    case_id         BIGINT          NOT NULL REFERENCES cases(id) ON DELETE CASCADE,
    label           VARCHAR(1)         NOT NULL,
    name            VARCHAR(200)    NOT NULL,
    description     TEXT            NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    UNIQUE(case_id, label)
    );
CREATE INDEX IF NOT EXISTS idx_ic_case ON invention_components (case_id);

-- 5. prior_arts
CREATE TABLE IF NOT EXISTS prior_arts (
                                          id                  BIGSERIAL       PRIMARY KEY,
                                          case_id             BIGINT          NOT NULL REFERENCES cases(id) ON DELETE CASCADE,
    application_number  VARCHAR(20)     NOT NULL,
    rank                SMALLINT        NOT NULL,
    source              VARCHAR(20)     NOT NULL,
    rrf_score           FLOAT,
    included            BOOLEAN         NOT NULL DEFAULT TRUE,
    reason              TEXT,
    summary             TEXT,
    tech_purpose        TEXT,
    key_features        TEXT,
    matched_keywords    TEXT[]          NOT NULL DEFAULT '{}',
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    UNIQUE(case_id, application_number)
    );
CREATE INDEX IF NOT EXISTS idx_pa_case_rank ON prior_arts (case_id, rank);

-- 6. novelty_analyses
CREATE TABLE IF NOT EXISTS novelty_analyses (
                                                id              BIGSERIAL       PRIMARY KEY,
                                                case_id         BIGINT          NOT NULL UNIQUE REFERENCES cases(id) ON DELETE CASCADE,
    overall_verdict VARCHAR(20),
    summary         TEXT,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW()
    );

-- 7. novelty_comparisons
CREATE TABLE IF NOT EXISTS novelty_comparisons (
                                                   id                      BIGSERIAL       PRIMARY KEY,
                                                   novelty_analysis_id     BIGINT          NOT NULL REFERENCES novelty_analyses(id) ON DELETE CASCADE,
    component_id            BIGINT          NOT NULL REFERENCES invention_components(id) ON DELETE CASCADE,
    prior_art_id            BIGINT          NOT NULL REFERENCES prior_arts(id) ON DELETE CASCADE,
    match_status            VARCHAR(20)     NOT NULL,
    prior_art_excerpt       TEXT,
    UNIQUE(novelty_analysis_id, component_id, prior_art_id)
    );
CREATE INDEX IF NOT EXISTS idx_nc_analysis ON novelty_comparisons (novelty_analysis_id);

-- 8. inventive_step_analyses
CREATE TABLE IF NOT EXISTS inventive_step_analyses (
                                                       id                  BIGSERIAL       PRIMARY KEY,
                                                       case_id             BIGINT          NOT NULL UNIQUE REFERENCES cases(id) ON DELETE CASCADE,
    primary_art_id      BIGINT          NOT NULL REFERENCES prior_arts(id),
    secondary_art_id    BIGINT          REFERENCES prior_arts(id),
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW()
    );

-- 9. inventive_arguments
CREATE TABLE IF NOT EXISTS inventive_arguments (
                                                   id              BIGSERIAL       PRIMARY KEY,
                                                   analysis_id     BIGINT          NOT NULL REFERENCES inventive_step_analyses(id) ON DELETE CASCADE,
    argument_type   VARCHAR(30)     NOT NULL,
    applicable      BOOLEAN         NOT NULL DEFAULT FALSE,
    ai_recommended  BOOLEAN         NOT NULL DEFAULT FALSE,
    content         JSONB,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    UNIQUE(analysis_id, argument_type)
    );
CREATE INDEX IF NOT EXISTS idx_ia_analysis ON inventive_arguments (analysis_id);

-- 10. reports
CREATE TABLE IF NOT EXISTS reports (
                                       id                       BIGSERIAL       PRIMARY KEY,
                                       case_id                  BIGINT          NOT NULL UNIQUE REFERENCES cases(id) ON DELETE CASCADE,
    author_name              VARCHAR(100)    NOT NULL,
    novelty_satisfied        BOOLEAN         NOT NULL,
    inventive_satisfied      BOOLEAN         NOT NULL,
    overall_conclusion       TEXT            NOT NULL,
    created_at               TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at               TIMESTAMP       NOT NULL DEFAULT NOW()
    );