# spring-batch-practice

Spring Batch 실습 프로젝트.

---

## 로컬 환경 설정

### Docker (Colima)

테스트는 Testcontainers를 사용하므로 Docker 데몬이 실행 중이어야 한다.

```bash
# 1. Colima 설치
brew install colima

# 2. Docker CLI 설치
brew install docker

# 3. Colima 시작
colima start

# 4. docker.sock 전역 링크 설정

# 먼저 소켓 경로 확인 — 출력된 실제 경로를 아래 ln -s 명령에 사용한다
ls -l ~/.colima/default/docker.sock

# 위 경로에 소켓이 없으면 아래 명령으로 직접 찾아 실제 경로를 확인한다
# find ~ -name "docker.sock" 2>/dev/null

rm -f ~/.testcontainers.properties            # 충돌 방지
sudo mv /var/run/docker.sock /var/run/docker.sock.bak  # 기존 소켓 백업
sudo rm -f /var/run/docker.sock

# ↑ ls 출력에서 확인한 경로로 교체 (예: /Users/<your-name>/.colima/default/docker.sock)
sudo ln -s ${COLIMA_DOCKER_SOCKET_PATH} /var/run/docker.sock
```

이후 `docker ps` 가 정상 응답하면 준비 완료.

---

## 테스트 실행

```bash
./gradlew :batch-common:test
```
