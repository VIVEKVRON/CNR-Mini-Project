import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestClassifier
from skl2onnx import convert_sklearn
from skl2onnx.common.data_types import FloatTensorType

def generate_data(num_samples=1000):
    np.random.seed(42)
    # Features: pH (0-14, but realistic 4-9), moisture (0-100), N, P, K (0-100)
    ph = np.random.normal(6.5, 1.0, num_samples).clip(4, 9)
    moisture = np.random.normal(50, 15, num_samples).clip(10, 90)
    n = np.random.normal(40, 15, num_samples).clip(0, 100)
    p = np.random.normal(30, 10, num_samples).clip(0, 100)
    k = np.random.normal(30, 10, num_samples).clip(0, 100)
    
    # Target: Soil Quality Score (0, 1, 2 for Poor, Average, Good)
    # Simple logic: ideal pH is 6-7.5, moisture 40-60, high NPK is generally better.
    score = np.zeros(num_samples)
    for i in range(num_samples):
        points = 0
        if 6 <= ph[i] <= 7.5: points += 1
        if 40 <= moisture[i] <= 60: points += 1
        if n[i] > 30: points += 1
        if p[i] > 20: points += 1
        if k[i] > 20: points += 1
        
        if points >= 4:
            score[i] = 2 # Good
        elif points >= 2:
            score[i] = 1 # Average
        else:
            score[i] = 0 # Poor
            
    df = pd.DataFrame({'pH': ph, 'Moisture': moisture, 'N': n, 'P': p, 'K': k, 'Quality': score})
    return df

if __name__ == "__main__":
    df = generate_data()
    df.to_csv('soil_dataset.csv', index=False)
    
    X = df[['pH', 'Moisture', 'N', 'P', 'K']]
    y = df['Quality']
    
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)
    
    model = RandomForestClassifier(n_estimators=100, random_state=42)
    model.fit(X_train, y_train)
    
    # Export to ONNX
    initial_type = [('float_input', FloatTensorType([None, 5]))]
    onnx_model = convert_sklearn(model, initial_types=initial_type, target_opset=12)
    with open("soil_model.onnx", "wb") as f:
        f.write(onnx_model.SerializeToString())
    
    print("Model trained and exported to soil_model.onnx")
