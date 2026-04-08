#!/usr/bin/env bash
# ============================================================
# 接口性能压测脚本
# 依赖: wrk (brew install wrk) 或 ab (Apache Bench)
# 用法: bash load-test.sh [BASE_URL] [CONCURRENCY] [DURATION]
# 示例: bash load-test.sh http://localhost:8080 100 30s
# ============================================================

BASE_URL="${1:-http://localhost:8080}"
CONCURRENCY="${2:-100}"
DURATION="${3:-30s}"
TOKEN="${AUTH_TOKEN:-}"

echo "============================================"
echo " 互助求助系统 - 接口性能压测"
echo " 目标: ${BASE_URL}"
echo " 并发: ${CONCURRENCY}"
echo " 持续: ${DURATION}"
echo "============================================"
echo ""

# 检测压测工具
if command -v wrk &> /dev/null; then
  TOOL="wrk"
elif command -v ab &> /dev/null; then
  TOOL="ab"
else
  echo "ERROR: 需要安装 wrk 或 ab (Apache Bench)"
  echo "  macOS: brew install wrk"
  echo "  Linux: apt-get install apache2-utils"
  exit 1
fi

RESULTS_DIR="./load-test-results"
mkdir -p "$RESULTS_DIR"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
REPORT="${RESULTS_DIR}/report_${TIMESTAMP}.txt"

echo "压测报告 - ${TIMESTAMP}" > "$REPORT"
echo "工具: ${TOOL}, 并发: ${CONCURRENCY}, 持续: ${DURATION}" >> "$REPORT"
echo "" >> "$REPORT"

run_wrk_test() {
  local name="$1"
  local url="$2"
  local method="${3:-GET}"

  echo ">>> 测试: ${name} (${method} ${url})"
  echo "--- ${name} (${method} ${url}) ---" >> "$REPORT"

  if [ "$method" = "GET" ]; then
    wrk -t4 -c"${CONCURRENCY}" -d"${DURATION}" --latency \
      -H "Authorization: Bearer ${TOKEN}" \
      "${url}" 2>&1 | tee -a "$REPORT"
  fi
  echo "" >> "$REPORT"
  echo ""
}

run_ab_test() {
  local name="$1"
  local url="$2"
  local n="${3:-10000}"

  echo ">>> 测试: ${name} (${url})"
  echo "--- ${name} (${url}) ---" >> "$REPORT"

  ab -n "$n" -c "${CONCURRENCY}" \
    -H "Authorization: Bearer ${TOKEN}" \
    "${url}" 2>&1 | tee -a "$REPORT"
  echo "" >> "$REPORT"
  echo ""
}

if [ "$TOOL" = "wrk" ]; then
  # 1. 求助列表 (核心读接口，目标 ≤500ms)
  run_wrk_test "求助列表" "${BASE_URL}/api/mp/help/list?page=1&size=20"

  # 2. 求助详情
  run_wrk_test "求助详情" "${BASE_URL}/api/mp/help/1"

  # 3. 附近求助 (含地理计算)
  run_wrk_test "附近求助" "${BASE_URL}/api/mp/help/nearby?lat=39.9042&lng=116.4074&radiusKm=10&limit=50"

  # 4. 管理端仪表盘
  run_wrk_test "管理端仪表盘" "${BASE_URL}/api/admin/dashboard/stats"

  # 5. 性能统计接口
  run_wrk_test "性能统计" "${BASE_URL}/api/admin/dashboard/perf?hours=24"

else
  run_ab_test "求助列表" "${BASE_URL}/api/mp/help/list?page=1&size=20"
  run_ab_test "求助详情" "${BASE_URL}/api/mp/help/1"
  run_ab_test "附近求助" "${BASE_URL}/api/mp/help/nearby?lat=39.9042&lng=116.4074&radiusKm=10&limit=50"
fi

echo "============================================"
echo " 压测完成，报告已保存至: ${REPORT}"
echo ""
echo " 性能目标:"
echo "   - 接口响应 ≤500ms (p99)"
echo "   - 支持 ${CONCURRENCY} 并发"
echo "   - 查看实时性能数据: ${BASE_URL}/api/admin/dashboard/perf"
echo "============================================"

# 自动检查性能指标
echo ""
echo ">>> 查询服务端性能统计..."
if command -v curl &> /dev/null; then
  PERF=$(curl -s -H "Authorization: Bearer ${TOKEN}" "${BASE_URL}/api/admin/dashboard/perf?hours=1")
  echo "最近1小时性能数据: ${PERF}"
  echo "" >> "$REPORT"
  echo "服务端性能统计 (最近1小时): ${PERF}" >> "$REPORT"
fi
