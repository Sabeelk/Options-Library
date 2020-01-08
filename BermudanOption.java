//class will typically support american style options, with specific excercise window
class BermudanOption extends VanillaOption
{
    //extra data members here
    public double window_begin;
    public double window_end;
    public boolean AE = false;                      //Bermudan Options are automatically American

    BermudanOption(double k, double T, boolean putCall, double wb, double we)
    {   
        this.T = T;
        this.window_begin = wb;
        this.window_end = we;
    }

    //Bermudan Option uses the overriden function in VanillaOption, which it derives from
    //noo need to overrwrite
    public void terminalCondition(){}

    //Override Valuation Test specifically for the Bermudan option
    void valuationTest(Node n)
    {
        //Do these steps ONLY if the time of the node is within the window
        if(n.time >= window_begin && n.time <= window_end){
            //find the intrinsic value depending on the put or call option
            if(putOrCall == false){
                intrinsic = k - n.stockPrice;
            }
            else{
                intrinsic = n.stockPrice - k;
            }

            //Next test whether the new intrinsic value is higher
            //if so set the fv to be the calculated intrinsic
            if(intrinsic >= n.fairValue){
                n.fairValue = intrinsic;
                n.changed = true;
                n.exercise = true;
            }
        }
    }
}