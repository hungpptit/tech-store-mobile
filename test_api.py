#!/usr/bin/env python3
"""
Automated Integration and Load Testing Suite for Tech Store Stripe Backend
Author: Senior Software Engineer & Backend Developer
"""

import sys
import json
import time
import subprocess
import urllib.request
import urllib.error
from datetime import datetime

# Configure UTF-8 safe output for Windows console
if sys.stdout.encoding != 'utf-8':
    try:
        sys.stdout.reconfigure(encoding='utf-8')
    except Exception:
        pass

BASE_URL = "http://127.0.0.1:3000"
SERVER_PROC = None

class Colors:
    GREEN = "\033[92m"
    RED = "\033[91m"
    YELLOW = "\033[93m"
    CYAN = "\033[96m"
    BOLD = "\033[1m"
    RESET = "\033[0m"

def log_info(msg):
    print(f"{Colors.CYAN}[INFO]{Colors.RESET} {msg}")

def log_pass(test_name, duration_ms):
    print(f" {Colors.GREEN}[PASS]{Colors.RESET} {test_name} ({duration_ms:.2f}ms)")

def log_fail(test_name, reason):
    print(f" {Colors.RED}[FAIL]{Colors.RESET} {test_name}: {reason}")

def http_request(method, endpoint, payload=None, timeout=5):
    url = f"{BASE_URL}{endpoint}"
    data = None
    headers = {"Content-Type": "application/json"}
    
    if payload is not None:
        data = json.dumps(payload).encode("utf-8")
        
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    
    start_time = time.time()
    try:
        with urllib.request.urlopen(req, timeout=timeout) as response:
            status = response.status
            body = response.read().decode("utf-8")
            elapsed = (time.time() - start_time) * 1000
            try:
                parsed_json = json.loads(body)
            except Exception:
                parsed_json = body
            return status, parsed_json, elapsed
    except urllib.error.HTTPError as e:
        body = e.read().decode("utf-8")
        elapsed = (time.time() - start_time) * 1000
        try:
            parsed_json = json.loads(body)
        except Exception:
            parsed_json = body
        return e.code, parsed_json, elapsed
    except Exception as e:
        elapsed = (time.time() - start_time) * 1000
        return 0, str(e), elapsed

def is_server_running():
    status, _, _ = http_request("GET", "/health", timeout=1)
    return status == 200

def start_backend_if_needed():
    global SERVER_PROC
    if is_server_running():
        log_info("Backend is already running on port 3000.")
        return True
    
    log_info("Starting backend server in background...")
    try:
        SERVER_PROC = subprocess.Popen(
            ["node", "server.js"],
            cwd="./backend",
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True
        )
        # Wait for server to initialize
        for _ in range(15):
            time.sleep(0.5)
            if is_server_running():
                log_info(f"{Colors.GREEN}Backend started successfully.{Colors.RESET}")
                return True
        log_fail("Server startup", "Timed out waiting for server on port 3000")
        return False
    except Exception as e:
        log_fail("Server startup", str(e))
        return False

def stop_backend():
    global SERVER_PROC
    if SERVER_PROC:
        log_info("Stopping spawned backend process...")
        try:
            SERVER_PROC.terminate()
            SERVER_PROC.wait(timeout=3)
        except Exception:
            SERVER_PROC.kill()
        SERVER_PROC = None

def run_test_suite():
    print(f"\n{Colors.BOLD}======================================================{Colors.RESET}")
    print(f"{Colors.BOLD}  TECH STORE BACKEND AUTOMATED TEST SUITE (Python 3)  {Colors.RESET}")
    print(f"{Colors.BOLD}======================================================{Colors.RESET}\n")
    
    if not start_backend_if_needed():
        print(f"{Colors.RED}Cannot proceed without backend server.{Colors.RESET}")
        return False
    
    test_results = []
    
    # Test Case 1: GET /
    status, body, duration = http_request("GET", "/")
    if status == 200 and isinstance(body, dict) and body.get("ok") is True:
        log_pass("TC_01: GET / (Service Metadata)", duration)
        test_results.append((True, "GET /", duration))
    else:
        log_fail("TC_01: GET /", f"Expected 200 and ok:true, got status={status}, body={body}")
        test_results.append((False, "GET /", duration))

    # Test Case 2: GET /health
    status, body, duration = http_request("GET", "/health")
    if status == 200 and isinstance(body, dict) and body.get("ok") is True:
        log_pass("TC_02: GET /health (Healthcheck)", duration)
        test_results.append((True, "GET /health", duration))
    else:
        log_fail("TC_02: GET /health", f"Expected 200, got {status}")
        test_results.append((False, "GET /health", duration))

    # Test Case 3: POST /api/payment-methods/create-card (Missing userId)
    status, body, duration = http_request("POST", "/api/payment-methods/create-card", {"cardToken": "tok_visa"})
    if status == 400 and "Missing required card token" in str(body):
        log_pass("TC_03: POST /create-card (Validate Missing userId)", duration)
        test_results.append((True, "POST /create-card (Missing userId)", duration))
    else:
        log_fail("TC_03: POST /create-card", f"Expected 400, got {status}, body={body}")
        test_results.append((False, "POST /create-card (Missing userId)", duration))

    # Test Case 4: POST /api/payment-methods/create-card (Missing cardToken)
    status, body, duration = http_request("POST", "/api/payment-methods/create-card", {"userId": "usr_999"})
    if status == 400 and "Missing required card token" in str(body):
        log_pass("TC_04: POST /create-card (Validate Missing cardToken)", duration)
        test_results.append((True, "POST /create-card (Missing cardToken)", duration))
    else:
        log_fail("TC_04: POST /create-card", f"Expected 400, got {status}, body={body}")
        test_results.append((False, "POST /create-card (Missing cardToken)", duration))

    # Test Case 5: POST /api/payments/create-payment-intent (Zero/Negative Amount)
    status, body, duration = http_request("POST", "/api/payments/create-payment-intent", {
        "totalAmount": 0,
        "paymentMethod": "pm_12345"
    })
    if status == 400 and "totalAmount must be a positive number" in str(body):
        log_pass("TC_05: POST /create-payment-intent (Validate Non-positive Amount)", duration)
        test_results.append((True, "POST /create-payment-intent (Zero/Negative Amount)", duration))
    else:
        log_fail("TC_05: POST /create-payment-intent", f"Expected 400, got {status}, body={body}")
        test_results.append((False, "POST /create-payment-intent (Zero/Negative Amount)", duration))

    # Test Case 6: POST /api/payments/create-payment-intent (Missing paymentMethod)
    status, body, duration = http_request("POST", "/api/payments/create-payment-intent", {
        "totalAmount": 99.99
    })
    if status == 400 and "paymentMethod is required" in str(body):
        log_pass("TC_06: POST /create-payment-intent (Validate Missing paymentMethod)", duration)
        test_results.append((True, "POST /create-payment-intent (Missing paymentMethod)", duration))
    else:
        log_fail("TC_06: POST /create-payment-intent", f"Expected 400, got {status}, body={body}")
        test_results.append((False, "POST /create-payment-intent (Missing paymentMethod)", duration))

    # Test Case 7: POST /api/payments/create-payment-intent (Invalid paymentMethod prefix)
    status, body, duration = http_request("POST", "/api/payments/create-payment-intent", {
        "totalAmount": 99.99,
        "paymentMethod": "card_test_invalid"
    })
    if status == 400 and "must be a saved Stripe payment method (pm_)" in str(body):
        log_pass("TC_07: POST /create-payment-intent (Validate Invalid pm_ Prefix)", duration)
        test_results.append((True, "POST /create-payment-intent (Invalid pm_ Prefix)", duration))
    else:
        log_fail("TC_07: POST /create-payment-intent", f"Expected 400, got {status}, body={body}")
        test_results.append((False, "POST /create-payment-intent (Invalid pm_ Prefix)", duration))

    # Test Case 8: GET Non-existent Route (404 Handling)
    status, body, duration = http_request("GET", "/api/non-existent-endpoint")
    if status == 404:
        log_pass("TC_08: GET /api/non-existent-endpoint (404 Fallback)", duration)
        test_results.append((True, "404 Route Fallback", duration))
    else:
        log_fail("TC_08: 404 Route", f"Expected 404, got {status}")
        test_results.append((False, "404 Route Fallback", duration))

    # Benchmark: Latency Across 20 Concurrent Healthcheck Requests
    print(f"\n{Colors.YELLOW}--- Running Benchmark & Latency Measurement (20 Requests) ---{Colors.RESET}")
    latencies = []
    for i in range(20):
        _, _, lat = http_request("GET", "/health")
        latencies.append(lat)
    
    avg_lat = sum(latencies) / len(latencies)
    min_lat = min(latencies)
    max_lat = max(latencies)
    print(f" Latency Metrics: Min={min_lat:.2f}ms | Avg={avg_lat:.2f}ms | Max={max_lat:.2f}ms")
    test_results.append((True, "Benchmark (20 requests latency)", avg_lat))

    # Summary
    passed_count = sum(1 for passed, _, _ in test_results if passed)
    total_count = len(test_results)
    pass_rate = (passed_count / total_count) * 100

    print(f"\n{Colors.BOLD}======================================================{Colors.RESET}")
    print(f"{Colors.BOLD}  TEST EXECUTION SUMMARY                              {Colors.RESET}")
    print(f"{Colors.BOLD}======================================================{Colors.RESET}")
    print(f" Total Tests Run: {total_count}")
    print(f" Passed: {Colors.GREEN}{passed_count}{Colors.RESET}")
    print(f" Failed: {Colors.RED}{total_count - passed_count}{Colors.RESET}")
    print(f" Pass Rate: {Colors.BOLD}{pass_rate:.1f}%{Colors.RESET}")
    print(f" Average API Response Time: {avg_lat:.2f} ms")
    print(f" Timestamp: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")

    return total_count == passed_count

if __name__ == "__main__":
    try:
        success = run_test_suite()
        sys.exit(0 if success else 1)
    finally:
        stop_backend()
