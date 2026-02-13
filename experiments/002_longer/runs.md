# 2nd training run.
Training 200K games
Benchmark 1K games every 10K against Random agent
Run A was initial parameters guess (same as run 0 in 001)

| Run | alpha | lambda | gamma |
|-----|-------|--------|-------|
| A   | 0.01  | 0.7    | 1.0   |
| B   | 0.005 | 0.7    | 1.0   |
| C   | 0.02  | 0.7    | 1.0   |
| D   | 0.01  | 0.5    | 1.0   |
| E   | 0.01  | 0.9    | 1.0   |
| F   | 0.01  | 0.7    | 0.95  |
| G   | 0.01  | 0.7    | 0.99  |

---

### 📊 Learning Behavior Summary

#### 🅰️ Run A
- **TD Error**: Gently decreasing, steady learning.
- **Weight Delta**: Consistent decay, converges smoothly.
- **Conclusion**: Balanced, healthy learning.

#### 🅱️ Run B (Lower Alpha)
- **TD Error**: Stable and slightly lower overall.
- **Weight Delta**: Small changes, very smooth.
- **Conclusion**: Under-confident learning, but very stable.

#### 🆑 Run C (Higher Alpha)
- **TD Error**: High fluctuation.
- **Weight Delta**: Large jumps mid-way.
- **Conclusion**: Risky fast learning. Showed signs of instability.

#### 🅳 Run D (Lower Lambda)
- **TD Error**: More jagged curve.
- **Weight Delta**: Irregular.
- **Conclusion**: Shorter memory made learning reactive. Less smooth.

#### 🅴 Run E (High Lambda)
- **TD Error**: Early gains, then degradation.
- **Weight Delta**: Spikes and instability.
- **Conclusion**: ❗ **Unstable eligibility traces caused over-crediting.**

> Eligibility traces decayed too slowly (high λ = 0.9). This caused over-attribution of TD error to distant states. Combined with a standard learning rate (α=0.01), this likely led to weight overshooting, erasing earlier learning.

#### 🅵 Run F (Lower Gamma)
- **TD Error**: Consistent low variance.
- **Weight Delta**: Decaying smoothly.
- **Conclusion**: Shorter planning horizon stabilized learning.

#### 🅶 Run G (Gamma = 0.99)
- **TD Error**: Late rise and instability.
- **Weight Delta**: Grew late in training.
- **Conclusion**: Long planning horizon delayed reward signal.

> May have caused feedback loops where credit is spread too widely, amplifying noise. Gradient instability possible.

---

### ⚠️ What Does "Unstable Eligibility Traces or Bad Gradient Scaling" Mean?

- **Unstable Eligibility Traces**: Traces remember too much. With high λ, early states are blamed/rewarded too long. If combined with noisy TD error or high α, it causes wild updates.
- **Bad Gradient Scaling**: If network gradients vanish or explode (due to activation saturation, bad initialization), updates are ineffective or erratic.

> Result: Even a network trying to learn may reinforce bad decisions or unlearn good ones — leading to performance worse than random in some cases.
