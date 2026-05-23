#!/bin/bash

# Server management script
# Usage: ./scripts/server.sh [command] [options]
#   Commands:
#     start             Start the server
#     debug             Start the server in debug mode
#     stop              Stop the server
#     restart           Restart the server
#     status            Check server status
#     logs              Show server logs
#     
#   Options:
#     --port <port>     Specify port (default: 8080)
#     --env <env>       Environment (dev, prod) (default: dev)
#     --help, -h        Show this help message

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default options
PORT=8080
ENV="dev"
DEBUG_PORT=5005
PID_FILE=".server.pid"

# Parse options
parse_options() {
  while [[ $# -gt 0 ]]; do
    case $1 in
      --port)
        PORT="$2"
        shift 2
        ;;
      --env)
        ENV="$2"
        shift 2
        ;;
      -h|--help)
        show_help
        exit 0
        ;;
      *)
        break
        ;;
    esac
  done
}

# Show help
show_help() {
  echo "Server management script"
  echo ""
  echo "Usage: ./scripts/server.sh [command] [options]"
  echo ""
  echo "Commands:"
  echo "  start             Start the server"
  echo "  debug             Start the server in debug mode"
  echo "  stop              Stop the server"
  echo "  restart           Restart the server"
  echo "  status            Check server status"
  echo "  logs              Show server logs"
  echo "  build             Build the server"
  echo ""
  echo "Options:"
  echo "  --port <port>     Specify port (default: 8080)"
  echo "  --env <env>       Environment (dev, prod) (default: dev)"
  echo "  -h, --help        Show this help message"
  echo ""
  echo "Examples:"
  echo "  ./scripts/server.sh start"
  echo "  ./scripts/server.sh debug --port 8081"
  echo "  ./scripts/server.sh stop"
  echo "  ./scripts/server.sh logs"
}

# Check if server is running
is_running() {
  if [ -f "$PID_FILE" ]; then
    pid=$(cat "$PID_FILE")
    if ps -p "$pid" > /dev/null 2>&1; then
      return 0
    else
      rm -f "$PID_FILE"
      return 1
    fi
  fi
  return 1
}

# Start server
start_server() {
  local debug_mode=$1
  
  if is_running; then
    echo -e "${YELLOW}⚠️  Server is already running${NC}"
    pid=$(cat "$PID_FILE")
    echo -e "${BLUE}PID: $pid${NC}"
    exit 1
  fi
  
  echo -e "${BLUE}╔════════════════════════════════════════╗${NC}"
  if [ "$debug_mode" = true ]; then
    echo -e "${BLUE}║    Starting Server (Debug Mode)        ║${NC}"
  else
    echo -e "${BLUE}║         Starting Server                ║${NC}"
  fi
  echo -e "${BLUE}╚════════════════════════════════════════╝${NC}"
  echo ""
  
  echo -e "${YELLOW}🏗️  Building server...${NC}"
  ./gradlew :server:build -x test
  
  echo ""
  echo -e "${YELLOW}🚀 Starting server...${NC}"
  echo -e "${BLUE}   Port: $PORT${NC}"
  echo -e "${BLUE}   Environment: $ENV${NC}"
  
  if [ "$debug_mode" = true ]; then
    echo -e "${BLUE}   Debug Port: $DEBUG_PORT${NC}"
    echo -e "${YELLOW}   Debug URL: localhost:$DEBUG_PORT${NC}"
  fi
  
  # Set environment variables
  export PORT=$PORT
  export ENV=$ENV
  
  # Start the server
  if [ "$debug_mode" = true ]; then
    JAVA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:$DEBUG_PORT" \
      ./gradlew :server:run > server.log 2>&1 &
  else
    ./gradlew :server:run > server.log 2>&1 &
  fi
  
  # Save PID
  echo $! > "$PID_FILE"
  
  # Wait a bit and check if it started
  sleep 3
  
  if is_running; then
    echo ""
    echo -e "${GREEN}✅ Server started successfully!${NC}"
    echo -e "${GREEN}   PID: $(cat $PID_FILE)${NC}"
    echo -e "${GREEN}   URL: http://localhost:$PORT${NC}"
    echo ""
    echo -e "${BLUE}💡 Tips:${NC}"
    echo -e "${BLUE}   - Check logs: ./scripts/server.sh logs${NC}"
    echo -e "${BLUE}   - Stop server: ./scripts/server.sh stop${NC}"
    echo -e "${BLUE}   - Check status: ./scripts/server.sh status${NC}"
  else
    echo ""
    echo -e "${RED}❌ Server failed to start${NC}"
    echo -e "${YELLOW}Check logs: tail -f server.log${NC}"
    rm -f "$PID_FILE"
    exit 1
  fi
}

# Stop server
stop_server() {
  echo -e "${BLUE}╔════════════════════════════════════════╗${NC}"
  echo -e "${BLUE}║         Stopping Server                ║${NC}"
  echo -e "${BLUE}╚════════════════════════════════════════╝${NC}"
  echo ""
  
  if ! is_running; then
    echo -e "${YELLOW}⚠️  Server is not running${NC}"
    exit 0
  fi
  
  pid=$(cat "$PID_FILE")
  echo -e "${YELLOW}🛑 Stopping server (PID: $pid)...${NC}"
  
  kill "$pid" 2>/dev/null || true
  
  # Wait for process to stop
  for i in {1..10}; do
    if ! ps -p "$pid" > /dev/null 2>&1; then
      break
    fi
    sleep 1
  done
  
  # Force kill if still running
  if ps -p "$pid" > /dev/null 2>&1; then
    echo -e "${YELLOW}⚠️  Force killing server...${NC}"
    kill -9 "$pid" 2>/dev/null || true
  fi
  
  rm -f "$PID_FILE"
  echo -e "${GREEN}✅ Server stopped${NC}"
}

# Show server status
show_status() {
  echo -e "${BLUE}╔════════════════════════════════════════╗${NC}"
  echo -e "${BLUE}║         Server Status                  ║${NC}"
  echo -e "${BLUE}╚════════════════════════════════════════╝${NC}"
  echo ""
  
  if is_running; then
    pid=$(cat "$PID_FILE")
    echo -e "${GREEN}✅ Server is running${NC}"
    echo -e "${BLUE}   PID: $pid${NC}"
    echo -e "${BLUE}   URL: http://localhost:$PORT${NC}"
    echo ""
    echo -e "${BLUE}Memory usage:${NC}"
    ps -p "$pid" -o rss=,vsz= | awk '{printf "   RSS: %d MB, VSZ: %d MB\n", $1/1024, $2/1024}'
  else
    echo -e "${RED}❌ Server is not running${NC}"
  fi
}

# Show logs
show_logs() {
  echo -e "${BLUE}╔════════════════════════════════════════╗${NC}"
  echo -e "${BLUE}║         Server Logs                    ║${NC}"
  echo -e "${BLUE}╚════════════════════════════════════════╝${NC}"
  echo ""
  
  if [ -f "server.log" ]; then
    tail -f server.log
  else
    echo -e "${YELLOW}⚠️  No log file found${NC}"
  fi
}

# Build server
build_server() {
  echo -e "${BLUE}╔════════════════════════════════════════╗${NC}"
  echo -e "${BLUE}║         Building Server                ║${NC}"
  echo -e "${BLUE}╚════════════════════════════════════════╝${NC}"
  echo ""
  
  echo -e "${YELLOW}🏗️  Building...${NC}"
  ./gradlew :server:build
  
  echo ""
  echo -e "${GREEN}✅ Build successful!${NC}"
}

# Main logic
if [ $# -eq 0 ]; then
  show_help
  exit 0
fi

COMMAND=$1
shift

# Parse remaining options
parse_options "$@"

case $COMMAND in
  start)
    start_server false
    ;;
    
  debug)
    start_server true
    ;;
    
  stop)
    stop_server
    ;;
    
  restart)
    stop_server
    sleep 2
    start_server false
    ;;
    
  status)
    show_status
    ;;
    
  logs)
    show_logs
    ;;
    
  build)
    build_server
    ;;
    
  -h|--help)
    show_help
    ;;
    
  *)
    echo -e "${RED}Unknown command: $COMMAND${NC}"
    echo ""
    show_help
    exit 1
    ;;
esac
