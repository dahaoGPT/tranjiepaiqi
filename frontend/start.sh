#!/bin/bash

set -e

PROJECT_DIR=$(cd "$(dirname "$0")" && pwd)
PID_FILE="${PROJECT_DIR}/frontend.pid"
LOG_FILE="${PROJECT_DIR}/frontend.log"

info() {
    echo -e "\033[1;34m[INFO]\033[0m $1"
}

success() {
    echo -e "\033[1;32m[SUCCESS]\033[0m $1"
}

error() {
    echo -e "\033[1;31m[ERROR]\033[0m $1"
}

check_node() {
    if ! command -v node &> /dev/null; then
        error "Node.js 未安装，请先安装 Node.js"
        exit 1
    fi
    node_version=$(node --version)
    info "Node.js 版本: $node_version"
}

install_deps() {
    if [ ! -d "${PROJECT_DIR}/node_modules" ]; then
        info "安装依赖..."
        cd "$PROJECT_DIR"
        npm install
        if [ $? -eq 0 ]; then
            success "依赖安装成功"
        else
            error "依赖安装失败"
            exit 1
        fi
    else
        info "依赖已存在，跳过安装"
    fi
}

run_dev() {
    install_deps
    info "启动开发模式..."
    cd "$PROJECT_DIR"
    npm run dev
}

run_prod() {
    install_deps
    info "构建生产版本..."
    cd "$PROJECT_DIR"
    npm run build
    if [ $? -ne 0 ]; then
        error "构建失败"
        exit 1
    fi
    success "构建成功"
    info "启动生产模式..."
    nohup npx next start > "$LOG_FILE" 2>&1 &
    echo $! > "$PID_FILE"
    success "服务已启动，PID 已保存到 frontend.pid"
}

run_foreground() {
    install_deps
    info "启动服务（前台模式）..."
    cd "$PROJECT_DIR"
    npm run dev
}

stop_service() {
    if [ -f "$PID_FILE" ]; then
        pid=$(cat "$PID_FILE")
        info "停止服务 (PID: $pid)..."
        if kill -0 "$pid" 2>/dev/null; then
            kill "$pid"
            sleep 2
            if kill -0 "$pid" 2>/dev/null; then
                kill -9 "$pid"
            fi
            success "服务已停止"
        else
            info "进程 $pid 不存在"
        fi
        rm -f "$PID_FILE"
    else
        info "未找到 frontend.pid 文件"
    fi
}

show_status() {
    if [ -f "$PID_FILE" ]; then
        pid=$(cat "$PID_FILE")
        if kill -0 "$pid" 2>/dev/null; then
            success "服务运行中 (PID: $pid)"
        else
            info "frontend.pid 存在但进程已停止"
        fi
    else
        info "服务未启动"
    fi
}

show_help() {
    echo "用法: $0 [命令]"
    echo ""
    echo "命令列表:"
    echo "  dev          - 开发模式启动"
    echo "  prod         - 生产模式启动（后台运行）"
    echo "  foreground   - 前台模式启动（查看日志）"
    echo "  build        - 仅构建生产版本"
    echo "  stop         - 停止服务"
    echo "  status       - 查看服务状态"
    echo "  restart      - 重启服务"
    echo "  help         - 显示帮助信息"
    echo ""
    echo "示例:"
    echo "  $0 dev              # 开发模式启动"
    echo "  $0 prod             # 生产模式后台启动"
    echo "  $0 foreground       # 前台启动并查看日志"
    echo "  $0 stop             # 停止服务"
}

case "$1" in
    dev)
        check_node
        run_dev
        ;;
    prod)
        check_node
        run_prod
        ;;
    foreground)
        check_node
        run_foreground
        ;;
    build)
        check_node
        install_deps
        cd "$PROJECT_DIR"
        npm run build
        ;;
    stop)
        stop_service
        ;;
    status)
        show_status
        ;;
    restart)
        stop_service
        sleep 2
        run_prod
        ;;
    help)
        show_help
        ;;
    *)
        show_help
        exit 1
        ;;
esac