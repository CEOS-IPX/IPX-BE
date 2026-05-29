-- ============================================================
-- PostgreSQL 초기화 스크립트
-- 컨테이너 최초 시작 시 자동 실행
-- ============================================================

-- pgvector 확장 활성화
CREATE EXTENSION IF NOT EXISTS vector;

-- 벡터 검색
CREATE TABLE patent_vectors (
                                id                  BIGSERIAL       NOT NULL,

                                application_number  VARCHAR(20)     NOT NULL UNIQUE,    -- 특허 식별

                                title               TEXT            NOT NULL,           -- 발명의 명칭
                                abstract_clean      TEXT            NOT NULL,           -- 전처리된 초록

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

-- IPC 코드 필터링용 GIN 인덱스
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