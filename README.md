# IPX-BE

## ONNX 모델 다운로드 (최초 1회)

프로젝트 루트에서 아래 명령어를 실행합니다.

### 1. huggingface-cli 설치 (이미 있으면 생략)
`pip install huggingface_hub`

### 2. 모델 다운로드
```
huggingface-cli download yuniko-software/bge-m3-onnx \
bge_m3_model.onnx \
bge_m3_model.onnx_data \
bge_m3_tokenizer.onnx \
--local-dir ./onnx
```

다운로드 완료 후 onnx/ 폴더에 아래 파일이 있는지 확인합니다.
- bge_m3_model.onnx (모델 구조)
- bge_m3_model.onnx_data (모델 가중치, 약 2GB)
- bge_m3_tokenizer.onnx (토크나이저)