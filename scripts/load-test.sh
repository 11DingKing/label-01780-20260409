#!/bin/bash
# 高并发压测脚本 - 使用 wrk 测试核心接口
# 安装 wrk: brew install wrk (macOS) / apt install wrk (Ubuntu)
#
# 使用方式:
#   单机测试: ./scripts/load-test.sh http://localhost:8080
#   多节点测试: ./scripts/load-test.sh http://localhost (通过 Nginx)

BASE_URL="${1:-http://localhost:8080}"
DURATION="30s"
THREADS=8
CONNECTIONS=500

echo "=========================================="
echo "  互助小程序 高并发压测"
echo "  目标: ${BASE_URL}"
echo "  线程: ${THREADS}  连接: ${CONNECTIONS}  时长: ${DURATION}"
echo "=========================================="

echo ""
echo "--- 1. 求助列表 GET /api/mp/help/list ---"
wrk -t${THREADS} -c${CONNECTIONS} -d${DURATION} \
    "${BASE_URL}/api/mp/help/list?page=1&size=10"

echo ""
echo "--- 2. 附近求助 GET /api/mp/help/nearby ---"
wrk -t${THREADS} -c${CONNECTIONS} -d${DURATION} \
    "${BASE_URL}/api/mp/help/nearby?lat=39.9042&lng=116.4074&radius=50"

echo ""
echo "--- 3. 求助详情 GET /api/mp/help/1 ---"
wrk -t${THREADS} -c${CONNECTIONS} -d${DURATION} \
    "${BASE_URL}/api/mp/help/1"

echo ""
echo "--- 4. 管理后台列表 GET /api/admin/help/list ---"
wrk -t${THREADS} -c${CONNECTIONS} -d${DURATION} \
    "${BASE_URL}/api/admin/help/list?page=1&size=10"

echo ""
echo "=========================================="
echo "  压测完成"
echo "  单机预期 QPS: 2000-3000"
echo "  3节点预期 QPS: 6000-9000"
echo "  生产环境(K8s 10+ Pod): 10000+ QPS"
echo "=========================================="
