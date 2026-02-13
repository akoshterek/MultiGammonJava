# 3rd training run.
Training 200K games

| Run | alpha | lambda | gamma | Notes                                                                               |
|-----| ----- | ------ | ----- | ----------------------------------------------------------------------------------- |
| H   | 0.007 | 0.8    | 1.0   | Mid-ground between B and E — tests if slight λ increase helps without destabilizing |
| I   | 0.005 | 0.5    | 1.0   | Conservative but reactive — minimal trace memory                                    |
| J   | 0.008 | 0.7    | 0.97  | Slightly lower γ for better credit assignment control                               |
| K   | 0.003 | 0.8    | 0.99  | Very conservative α to stabilize long trace and discount                            |
| L   | 0.015 | 0.6    | 1.0   | Mild boost to α with moderate trace decay                                           |

🧠 Why These?
Run H: λ=0.8 may improve credit assignment without instability seen in E (λ=0.9).

Run I: Short traces + low α → explore stability from both ends.

Run J: γ=0.97 gives a moderate planning horizon but not too aggressive like G (γ=0.99).

Run K: Safeguarded combo of long horizon and memory but with very low α.

Run L: Pushes α a bit for faster learning with safer λ.


| Run | Stability   | Convergence | Comment                                          |
| --- | ----------- | ----------- | ------------------------------------------------ |
| H   | ❌ Unstable  | No          | TD error grew, early collapse likely.            |
| I   | ✅ Stable    | Partial     | Learns slowly, steady decay.                     |
| J   | ⚠️ Wavy     | Partial     | λ=0.9 trades stability for trace depth.          |
| K   | ✅ Good      | Yes         | Lower γ improves convergence.                    |
| L   | ✅ Very good | Near full   | Small deltas, minimal error growth — top result. |
