"""
Train a simple autoencoder on the user-item interaction matrix.
Saves per-student predictions into dataset/deeplearning_predictions.csv
"""
import os
import pandas as pd
import numpy as np
from tensorflow import keras
from tensorflow.keras import layers
from sklearn.model_selection import train_test_split

DATA_DIR = os.path.join(os.path.dirname(__file__), "..", "dataset")
INTERACTIONS = os.path.join(DATA_DIR, "interactions.csv")
FACULTIES = os.path.join(DATA_DIR, "faculties.csv")
OUT = os.path.join(DATA_DIR, "deeplearning_predictions.csv")
MODEL_DIR = os.path.join(os.path.dirname(__file__), "model")
os.makedirs(MODEL_DIR, exist_ok=True)

print("Loading interactions...")
df = pd.read_csv(INTERACTIONS)
mat = df.pivot_table(index='student_id', columns='faculty_id', values='rating', fill_value=0)
user_ids = mat.index.tolist()
item_ids = mat.columns.tolist()
X = mat.values.astype('float32') / 5.0

X_train, X_val = train_test_split(X, test_size=0.1, random_state=42)
n_items = X.shape[1]
encoding_dim = max(16, n_items//8)

input_layer = keras.Input(shape=(n_items,))
encoded = layers.Dense(encoding_dim, activation='relu')(input_layer)
encoded = layers.Dense(max(8, encoding_dim//2), activation='relu')(encoded)
decoded = layers.Dense(encoding_dim, activation='relu')(encoded)
decoded = layers.Dense(n_items, activation='sigmoid')(decoded)

autoencoder = keras.Model(inputs=input_layer, outputs=decoded)
autoencoder.compile(optimizer='adam', loss='mse')

autoencoder.fit(X_train, X_train, epochs=10, batch_size=128, validation_data=(X_val,X_val))

preds = autoencoder.predict(X, batch_size=256)
fac_df = pd.read_csv(FACULTIES)
faculty_names = fac_df['name'].astype(str).tolist()

with open(OUT, 'w', encoding='utf-8') as f:
    f.write("student_identifier,faculty_name,score\n")
    for i, uid in enumerate(user_ids):
        for j, fid in enumerate(item_ids):
            score = float(preds[i,j])
            f.write(f"{uid},{faculty_names[j]},{score:.6f}\n")

print("Saved predictions to", OUT)
autoencoder.save(os.path.join(MODEL_DIR, "autoencoder.h5"))
print("Saved model")
