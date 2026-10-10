"""
葛仙米环境监测 — 硬件传感器模拟器
每 10 秒向 Java 后台上报一条环境数据，模拟真实传感器行为。
Ctrl+C 停止。
"""
import json
import random
import time
import urllib.request
from datetime import datetime

# ============ 配置 ============
BASE_URL = "http://localhost:8000"
DATA_URL = f"{BASE_URL}/api/environment/data"

# 可用的设备编号（必须在 devices 表中已注册）
DEVICES = ["A-001", "A-002", "A-003", "A-004"]

# 每个设备的基线参数（模拟不同养殖筒的细微差异）
DEVICE_BASE = {
    "A-001": {"water_temp": 22.5, "ph": 7.2, "dissolved_oxygen": 6.8, "light_intensity": 1200, "conductivity": 350},
    "A-002": {"water_temp": 21.8, "ph": 7.0, "dissolved_oxygen": 7.2, "light_intensity": 1100, "conductivity": 340},
    "A-003": {"water_temp": 23.0, "ph": 7.3, "dissolved_oxygen": 6.5, "light_intensity": 1300, "conductivity": 360},
    "A-004": {"water_temp": 22.0, "ph": 6.9, "dissolved_oxygen": 7.0, "light_intensity": 1150, "conductivity": 345},
}

# ============ 数据生成 ============

def generate_one(device_id: str) -> dict:
    """生成一条模拟传感器数据"""
    base = DEVICE_BASE.get(device_id, DEVICE_BASE["A-001"])
    hour = datetime.now().hour

    # 水温：基线 ± 1.5°C 随机波动
    water_temp = round(base["water_temp"] + random.uniform(-1.5, 1.5), 1)

    # pH：基线 ± 0.3
    ph = round(base["ph"] + random.uniform(-0.3, 0.3), 1)

    # 溶解氧：基线 ± 1.0
    dissolved_oxygen = round(base["dissolved_oxygen"] + random.uniform(-1.0, 1.0), 1)

    # 光照：白天正常、夜间几乎为 0
    if 6 <= hour <= 18:
        light_intensity = round(base["light_intensity"] + random.uniform(-300, 300), 0)
    else:
        light_intensity = round(random.uniform(5, 40), 0)

    # 电导率：基线 ± 25
    conductivity = round(base["conductivity"] + random.uniform(-25, 25), 0)

    return {
        "deviceId": device_id,
        "timestamp": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
        "waterTemp": water_temp,
        "ph": ph,
        "dissolvedOxygen": dissolved_oxygen,
        "lightIntensity": light_intensity,
        "conductivity": conductivity,
    }


def send(data: dict) -> bool:
    """POST 数据到后端，返回是否成功"""
    body = json.dumps(data).encode("utf-8")
    req = urllib.request.Request(
        DATA_URL,
        data=body,
        headers={"Content-Type": "application/json"},
        method="POST",
    )
    try:
        resp = json.loads(urllib.request.urlopen(req, timeout=10).read())
        return resp.get("code") == 200
    except Exception as e:
        print(f"  [ERROR] {e}")
        return False


# ============ 主循环 ============

def main():
    print("=" * 55)
    print("  葛仙米环境传感器模拟器（Python）")
    print(f"  目标: {DATA_URL}")
    print(f"  设备: {', '.join(DEVICES)}")
    print(f"  频率: 每 10 秒发送一条")
    print(f"  停止: Ctrl+C")
    print("=" * 55)
    print()

    success = 0
    fail = 0
    idx = 0  # 轮流发给各设备

    while True:
        device = DEVICES[idx % len(DEVICES)]
        idx += 1

        data = generate_one(device)
        now = datetime.now().strftime("%H:%M:%S")
        info = (f"设备={data['deviceId']} 水温={data['waterTemp']}°C "
                f"pH={data['ph']} 溶氧={data['dissolvedOxygen']}mg/L "
                f"光照={data['lightIntensity']}lux 电导={data['conductivity']}μS/cm")

        if send(data):
            success += 1
            print(f"[{now}] [OK] {info}")
        else:
            fail += 1
            print(f"[{now}] [FAIL] {info}")

        print(f"      累计: 成功 {success} / 失败 {fail}")

        time.sleep(10)


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\n已停止。")
