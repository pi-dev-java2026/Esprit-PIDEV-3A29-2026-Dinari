import pandas as pd
from sklearn.linear_model import LinearRegression
import joblib

def train_model(data):

    X = data[['month']]
    y = data['amount']

    model = LinearRegression()
    model.fit(X, y)

    joblib.dump(model, "model_depense.pkl")