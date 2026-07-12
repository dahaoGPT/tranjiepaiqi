#!/bin/bash

set -e

PROJECT_DIR=$(cd "$(dirname "$0")" && pwd)
APP_NAME="jiepaiqi-backend"
APP_VERSION="0.1.0-SNAPSHOT"
JAR_FILE="${PROJECT_DIR}/target/${APP_NAME}-${APP_VERSION}.jar"
MVNW="${PROJECT_DIR}/.mvn/wrapper/mvnw"

info() {
    echo -e "\033[1;34m[INFO]\033[0m $1"
}

success() {
    echo -e "\033[1;32m[SUCCESS]\033[0m $1"
}

error() {
    echo -e "\033[1;31m[ERROR]\033[0m $1"
}

check_java() {
    if ! command -v java &> /dev/null; then
        error "Java 未安装，请先安装 JDK 8+"
        exit 1
    fi
    java_version=$(java -version 2>&1 | head -n 1 | awk -F '"' '{print $2}')
    info "Java 版本: $java_version"
}

ensure_mvnw_executable() {
    if [ ! -x "$MVNW" ]; then
        info "Maven Wrapper 缺少执行权限，正在设置..."
        chmod +x "$MVNW"
        if [ $? -eq 0 ]; then
            success "Maven Wrapper 执行权限已设置"
        else
            error "设置 Maven Wrapper 执行权限失败，请手动执行: chmod +x $MVNW"
            exit 1
        fi
    fi
}

build_project() {
    ensure_mvnw_executable
    info "开始编译项目..."
    cd "$PROJECT_DIR"
    "$MVNW" clean package -DskipTests
    if [ $? -eq 0 ]; then
        success "项目编译成功"
    else
        error "项目编译失败"
        exit 1
    fi
}

run_dev() {
    ensure_mvnw_executable
    info "启动开发模式..."
    cd "$PROJECT_DIR"
    "$MVNW" spring-boot:run
}

run_prod() {
    if [ ! -f "$JAR_FILE" ]; then
        info "JAR 文件不存在，先编译项目..."
        build_project
    fi
    info "启动生产模式..."
    nohup java -jar "$JAR_FILE" > /dev/null 2>&1 &
    echo $! > "${PROJECT_DIR}/backend.pid"
    success "服务已启动，PID 已保存到 backend.pid"
}

run_foreground() {
    if [ ! -f "$JAR_FILE" ]; then
        info "JAR 文件不存在，先编译项目..."
        build_project
    fi
    info "启动服务（前台模式）..."
    java -jar "$JAR_FILE"
}

stop_service() {
    if [ -f "${PROJECT_DIR}/backend.pid" ]; then
        pid=$(cat "${PROJECT_DIR}/backend.pid")
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
        rm -f "${PROJECT_DIR}/backend.pid"
    else
        info "未找到 backend.pid 文件"
    fi
}

show_status() {
    if [ -f "${PROJECT_DIR}/backend.pid" ]; then
        pid=$(cat "${PROJECT_DIR}/backend.pid")
        if kill -0 "$pid" 2>/dev/null; then
            success "服务运行中 (PID: $pid)"
        else
            info "backend.pid 存在但进程已停止"
        fi
    else
        info "服务未启动"
    fi
}

show_help() {
    echo "用法: $0 [命令]"
    echo ""
    echo "命令列表:"
    echo "  dev          - 开发模式启动（热重载）"
    echo "  prod         - 生产模式启动（后台运行）"
    echo "  foreground   - 前台模式启动（查看日志）"
    echo "  build        - 仅编译项目"
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
        check_java
        run_dev
        ;;
    prod)
        check_java
        run_prod
        ;;
    foreground)
        check_java
        run_foreground
        ;;
    build)
        check_java
        build_project
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