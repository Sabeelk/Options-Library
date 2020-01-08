//class will typically support put and call options
class VanillaOption extends Derivative
{   
    public double intrinsic;                    //stores the temp intrinsic

    VanillaOption(){}
    
    VanillaOption(double k, double T, boolean putCall, boolean AE)
    {   
        this.T = T;
        this.k = k;
        this.putOrCall = putCall;
        this.AE= AE;
    }

    //override the virtual function terminalcondition
    //This function sets the terminal nodes
    public void terminalCondition(Node n){
        n.fugit = T;

        if(putOrCall == false){
            n.fairValue = Math.max(k - n.stockPrice, 0);
        }
        else if(putOrCall == true){
            n.fairValue = Math.max(n.stockPrice - k, 0);
        }
    }

    //Test each node to determine its new fair value/ intrinsic value
    void valuationTest(Node n)
    {
        //find the intrinsic value depending on the put or call option
        if(putOrCall == false){
            intrinsic = Math.max(k - n.stockPrice, 0);
        }
        else if(putOrCall == true){
            intrinsic = Math.max(n.stockPrice - k, 0);
        }

        //Next test whether the new intrinsic value is higher
        //if so set the fv to be the calculated intrinsic
        if(intrinsic > n.fairValue){
            n.fairValue = intrinsic;
            n.changed = true;
            n.exercise = true;
        }
    }
}