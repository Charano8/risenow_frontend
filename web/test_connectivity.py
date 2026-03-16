import requests

def test_endpoint(name, url, method='GET', data=None):
    print(f"Testing {name} ({url})...")
    try:
        if method == 'GET':
            response = requests.get(url, timeout=5)
        else:
            response = requests.post(url, json=data, timeout=5)
        print(f"  Status: {response.status_code}")
        return True
    except Exception as e:
        print(f"  FAILED: {e}")
        return False

base = "http://127.0.0.1:5000"
# We don't care about correctness here, just if the server responds
test_endpoint("Login (Ping)", f"{base}/login", 'POST', {"email": "test@test.com", "password": "123"})
test_endpoint("Register (Ping)", f"{base}/register", 'POST', {"username": "test", "email": "test@test.com", "password": "123"})
test_endpoint("Get Alarms (Ping)", f"{base}/api/get_alarms/1")
