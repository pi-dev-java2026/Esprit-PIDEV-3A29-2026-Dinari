from fastapi import FastAPI
import joblib
import numpy as np
import os
import subprocess

app = FastAPI()

MODEL_PATH = "model_depense.pkl"


def load_model():
    if not os.path.exists(MODEL_PATH):
        raise Exception("Model not found")

    return joblib.load(MODEL_PATH)


@app.get("/predict")
def predict_next_month(month: int):

    model = load_model()   # 🔥 Reload model each prediction

    prediction = model.predict([[month]])

    return {
        "prediction": float(prediction[0])
    }


@app.post("/retrain")
def retrain_model():

    subprocess.run(["python", "train.py"], check=True)

    return {"message": "Model retrained"}