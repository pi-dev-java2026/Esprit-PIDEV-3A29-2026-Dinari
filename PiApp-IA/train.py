import pandas as pd
import mysql.connector
from sklearn.linear_model import LinearRegression
import joblib
import os

MODEL_PATH = r"D:\PiApp\PiApp\PiApp\PiApp-IA\model_depense.pkl"

try:
    if os.path.exists(MODEL_PATH):
        os.remove(MODEL_PATH)
        print("Old model deleted ✔")
except Exception as e:
    print("Delete model error :", e)

# Connexion MySQL
conn = mysql.connector.connect(
    host="localhost",
    user="root",
    password="",
    database="pidev"
)

query = """
SELECT MONTH(date_depense) as month,
       SUM(montant) as amount
FROM depense
GROUP BY MONTH(date_depense)
ORDER BY MONTH(date_depense)
"""

df = pd.read_sql(query, conn)

# Train model
X = df[['month']]
y = df['amount']

model = LinearRegression()
model.fit(X, y)

# Save model
joblib.dump(model, MODEL_PATH)

print("Model trained ✔")