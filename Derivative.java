abstract class Derivative{
    public double T;                                //Expiration Time
    public double k;                                //Strike Price
    public boolean putOrCall;                       //false will be put and true will be call
    public boolean AE;                              //false will be American, and true will be European Options

    //virtual functions
    void terminalCondition(Node n){};               //sets payoff and fugit value on exp. date
    void valuationTest(Node n){};     //checks for early exercise and FV, need i & j
}