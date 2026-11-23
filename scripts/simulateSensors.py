#!/usr/bin/env python3
import math
import random
import time

import firebase_admin
from firebase_admin import credentials, db

RACK_ID = "-P78hV5_dZ4ftE8cfB3B"
SERVICE_ACCOUNT = "serviceAccountKey.json"
DATABASE_URL = "https://sentryrack-default-rtdb.firebaseio.com/"
INTERVAL_SEC = 10
BASE = f"latest/{RACK_ID}"

cred = credentials.Certificate(SERVICE_ACCOUNT)
firebase_admin.initialize_app(cred, {"databaseURL": DATABASE_URL})

state = {"door_open": False, "tank_pct": 60.0, "amps": 4.0, "eco2": 450, "tvoc": 20}


def clamp(v, lo, hi):
    return max(lo, min(hi, v))


def sim_hall(ts):
    if random.random() < 0.1:
        state["door_open"] = not state["door_open"]
    x = (20 if state["door_open"] else 900) + random.uniform(-30, 30)
    y = random.uniform(-40, 40)
    z = random.uniform(-40, 40)
    return {
        "x": round(x, 1), "y": round(y, 1), "z": round(z, 1),
        "magnitude": round(math.sqrt(x * x + y * y + z * z), 1),
        "unit": "uT",
        "doorOpen": state["door_open"],
        "timestamp": ts,
    }


def sim_water(ts):
    state["tank_pct"] = clamp(state["tank_pct"] + random.uniform(-1.5, 1.5), 0, 100)
    pct = round(state["tank_pct"], 1)
    return {
        "percent": pct,
        "overflowAlert": pct >= 90,
        "lowAlert": pct <= 15,
        "timestamp": ts,
    }


def sim_current(ts):
    state["amps"] = clamp(state["amps"] + random.uniform(-0.4, 0.4), 0.5, 12)
    amps = round(state["amps"], 2)
    watts = round(amps * 120, 1)  # assumes 120 V
    return {
        "amps": amps,
        "powerWatts": watts,
        "overCurrentAlert": amps >= 10,
        "timestamp": ts,
    }


def sim_air(ts):
    state["eco2"] = int(clamp(state["eco2"] + random.randint(-25, 25), 400, 2000))
    state["tvoc"] = int(clamp(state["tvoc"] + random.randint(-10, 10), 0, 600))
    aqi = 1 + min(4, state["eco2"] // 500)
    return {
        "eco2": state["eco2"],
        "tvoc": state["tvoc"],
        "aqi": aqi,
        "airQualityAlert": aqi >= 4 or state["tvoc"] >= 400,
        "timestamp": ts,
    }


if __name__ == "__main__":
    print(f"Simulating all sensors for rack {RACK_ID} every {INTERVAL_SEC}s (Ctrl+C to stop)")
    try:
        while True:
            ts = int(time.time() * 1000)

            hall = sim_hall(ts)
            water = sim_water(ts)
            current = sim_current(ts)
            air = sim_air(ts)

            readings = {
                "timestamp": ts,
                "simulated": True,
                "hall": hall,
                "door": {"open": hall["doorOpen"]},
                "waterLevel": water,
                "coolantLevel": {"value": water["percent"], "leak": False},
                "current": {
                    "value": current["amps"],
                    "powerW": current["powerWatts"],
                },
                "airQuality": {
                    "aqi": air["aqi"],
                    "tvoc": air["tvoc"],
                    "eco2": air["eco2"],
                },
            }

            db.reference(BASE).set(readings)
            db.reference(f"history/{RACK_ID}").push(readings)
            print(readings)
            time.sleep(INTERVAL_SEC)
    except KeyboardInterrupt:
        print("Stopped.")