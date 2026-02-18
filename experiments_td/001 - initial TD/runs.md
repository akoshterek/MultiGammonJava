# Initial training run.
Training 100K games
Benchmark 1K games every 10K against Random agent
Run 0 was initial parameters guess

| Run | alpha | lambda | gamma |
|-----|-------|--------|-------|
| 0   | 0.01  | 0.7    | 1.0   |
| A   | 0.005 | 0.7    | 1.0   |
| B   | 0.02  | 0.7    | 1.0   |
| C   | 0.01  | 0.5    | 1.0   |
| D   | 0.01  | 0.9    | 1.0   |
| E   | 0.01  | 0.7    | 0.95  |
| F   | 0.01  | 0.7    | 0.99  |


| Run | Avg. Points/Game vs Random | Final Avg. TD Error | Final Weight Delta | Notes                           |                                                                                            |
| --- | -------------------------- | ------------------- | ------------------ | ------------------------------- |
| 0   | 0.396                      | 0.0301              | 0.227              | Baseline run                    |                                                                                            |
| A   | 0.230                      | 0.0339              | 0.384              | Lower performance than baseline |                                                                                            |
| B   | 0.192                      | 0.0301              | 0.227              | Similar TD error to baseline    |                                                                                            |
| C   | 0.310                      | 0.0301              | 0.227              | Improved points per game        |                                                                                            |
| D   | 0.370                      | 0.0301              | 0.227              | Further improvement             |                                                                                            |
| E   | 0.590                      | 0.0301              | 0.227              | Highest performance             |     
| F   | 0.547                      | 0.0301              | 0.227              | Slight decrease from E          |  

