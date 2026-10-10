# encoding: utf-8
import pymysql
import random
from datetime import datetime, timedelta

random.seed(42)

conn = pymysql.connect(host='localhost', user='root', password='3523048584',
                       database='gexianmi', charset='utf8mb4')
cur = conn.cursor()

# 1. Delete June data
cur.execute("DELETE FROM environment_records WHERE recorded_at < '2026-07-01'")
conn.commit()
print(f"June deleted: {cur.rowcount} rows")

# 2. Delete old July data
cur.execute("DELETE FROM environment_records WHERE recorded_at >= '2026-07-01'")
conn.commit()
print(f"July old data deleted: {cur.rowcount} rows")

# 3. Insert clean hourly data: July 1 - July 11, every hour, 4 devices
devices = ['A-001', 'A-002', 'A-003', 'A-004']
device_base = {
    'A-001': (22.5, 7.2, 6.8, 1200, 350),
    'A-002': (21.8, 7.0, 7.2, 1100, 340),
    'A-003': (23.0, 7.3, 6.5, 1300, 360),
    'A-004': (22.0, 6.9, 7.0, 1150, 345),
}

current = datetime(2026, 7, 1, 0, 0, 0)
end = datetime(2026, 7, 11, 23, 0, 0)
total = 0
batch = []

while current <= end:
    for dev in devices:
        b_wt, b_ph, b_do, b_li, b_co = device_base[dev]
        wt = round(b_wt + random.uniform(-1.5, 1.5), 1)
        ph = round(b_ph + random.uniform(-0.3, 0.3), 1)
        doxy = round(b_do + random.uniform(-0.8, 0.8), 1)
        li = round(b_li + random.uniform(-200, 200), 0)
        co = round(b_co + random.uniform(-20, 20), 0)

        hour = current.hour
        if 6 <= hour <= 18:
            li = round(b_li + random.uniform(-100, 300), 0)
        else:
            li = round(random.uniform(10, 50), 0)

        batch.append((dev, current.strftime('%Y-%m-%d %H:%M:%S'), wt, ph, doxy, li, co))
        total += 1

        if len(batch) >= 500:
            cur.executemany(
                """INSERT INTO environment_records
                   (device_id, recorded_at, water_temp, ph, dissolved_oxygen, light_intensity, conductivity)
                   VALUES (%s, %s, %s, %s, %s, %s, %s)""", batch)
            conn.commit()
            print(f"  Inserted {total} records...")
            batch = []

    current += timedelta(hours=1)

if batch:
    cur.executemany(
        """INSERT INTO environment_records
           (device_id, recorded_at, water_temp, ph, dissolved_oxygen, light_intensity, conductivity)
           VALUES (%s, %s, %s, %s, %s, %s, %s)""", batch)
    conn.commit()

print(f"\nDone! Total: {total} records")
print(f"Range: 2026-07-01 00:00:00 ~ 2026-07-11 23:00:00")
print(f"Devices: {devices}")
print(f"Frequency: 1 record/hour/device")

cur.execute("SELECT DATE_FORMAT(recorded_at,'%Y-%m') as m, COUNT(*) FROM environment_records GROUP BY m")
for row in cur.fetchall():
    print(f"  Month {row[0]}: {row[1]} records")

cur.execute("SELECT device_id, MIN(recorded_at), MAX(recorded_at), COUNT(*) FROM environment_records GROUP BY device_id ORDER BY device_id")
for row in cur.fetchall():
    print(f"  {row[0]}: {row[1]} ~ {row[2]} ({row[3]} recs)")

cur.close()
conn.close()
