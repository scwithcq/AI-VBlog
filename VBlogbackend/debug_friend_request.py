import requests
import json

# 基础URL
BASE_URL = "http://localhost:8080/api"

# 1. 注册两个用户
print("=== 步骤1: 注册用户 ===")
user1_data = {
    "username": f"testdebug{int(__import__('time').time())}",
    "password": "123456"
}
user2_data = {
    "username": f"testdebug{int(__import__('time').time()) + 1}",
    "password": "123456"
}

r1 = requests.post(f"{BASE_URL}/user/register", data=user1_data)
r2 = requests.post(f"{BASE_URL}/user/register", data=user2_data)

user1 = r1.json()['data']
user2 = r2.json()['data']

token1 = user1['token']
token2 = user2['token']
user1_id = user1['user']['userId']
user2_id = user2['user']['userId']

print(f"用户1: ID={user1_id}, Token={token1[:20]}...")
print(f"用户2: ID={user2_id}, Token={token2[:20]}...")

# 2. 发送好友申请
print("\n=== 步骤2: 发送好友申请 ===")
headers1 = {"Authorization": f"Bearer {token1}"}
r = requests.post(
    f"{BASE_URL}/friendship/request",
    headers=headers1,
    data={"toUserId": user2_id, "message": "测试好友申请"}
)
print(f"响应: {r.status_code}")
print(f"内容: {r.json()}")
request_id = r.json()['data']['requestId']
print(f"好友申请ID: {request_id}")

# 3. 获取好友申请
print("\n=== 步骤3: 获取好友申请 ===")
headers2 = {"Authorization": f"Bearer {token2}"}
r = requests.get(
    f"{BASE_URL}/friendship/requests/received",
    headers=headers2,
    params={"status": 0}
)
print(f"响应: {r.status_code}")
print(f"内容: {r.json()}")

# 4. 同意好友申请 - 测试不同参数格式
print("\n=== 步骤4: 同意好友申请 (测试不同参数) ===")

# 测试1: accept=true (字符串)
print("\n测试1: accept='true' (字符串)")
r = requests.put(
    f"{BASE_URL}/friendship/request/{request_id}",
    headers=headers2,
    params={"accept": "true"}
)
print(f"响应: {r.status_code}")
print(f"内容: {r.json()}")

if r.status_code != 200 or r.json().get('code') != 0:
    # 测试2: accept=1 (数字)
    print("\n测试2: accept=1 (数字)")
    r = requests.put(
        f"{BASE_URL}/friendship/request/{request_id}",
        headers=headers2,
        params={"accept": 1}
    )
    print(f"响应: {r.status_code}")
    print(f"内容: {r.json()}")

if r.status_code != 200 or r.json().get('code') != 0:
    # 测试3: accept=True (Python布尔)
    print("\n测试3: accept=True (Python布尔)")
    r = requests.put(
        f"{BASE_URL}/friendship/request/{request_id}",
        headers=headers2,
        params={"accept": True}
    )
    print(f"响应: {r.status_code}")
    print(f"内容: {r.json()}")

# 5. 检查好友关系
print("\n=== 步骤5: 检查好友关系 ===")
r = requests.get(
    f"{BASE_URL}/friendship/friends",
    headers=headers1
)
print(f"用户1的好友列表: {r.json()}")

r = requests.get(
    f"{BASE_URL}/friendship/friends",
    headers=headers2
)
print(f"用户2的好友列表: {r.json()}")
