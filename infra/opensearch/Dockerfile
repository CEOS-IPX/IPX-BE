# ============================================================
# nori 플러그인이 포함된 OpenSearch 커스텀 이미지
# ============================================================
 
FROM opensearchproject/opensearch:2.19.0
 
# 한국어 형태소 분석기(nori) 플러그인 설치
RUN /usr/share/opensearch/bin/opensearch-plugin install --batch analysis-nori