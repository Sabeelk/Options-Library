public class Main{
    public static void main(String[] args){
        //** Print Functions can be disabled if you do not want them to show data */

        //MarketData(double mp, double stock0, double rate, double sigma, double t0)
        MarketData md = new MarketData(20, 100, 10, 40, 0);

        //VanillaOption(double k, double T, boolean putCall, boolean AE)
        //put = false, call = true || american = false, European = true 
        VanillaOption v = new VanillaOption(105, 1.0, true, false);

        //BermudanOption(double T, double k, boolean putCall, double wb, double we)
        //put = false, call = true || american = false, European = true 
        //BermudanOption b = new BermudanOption(.4, 100, false, .15, .35);

        //Library(Derivative deriv, MarketData mkt, int n)
        Library l =  new Library();

        //This is the values for the initial input with the given volatility
        l.binom(v, md, 4);

        //This will calculate the implied volatility and return 0 or 1
        //int impvol(final Derivative deriv, final MarketData mkt, int max_iter, double tol, Output out)
        l.impvol(v, md, l.n, 100, .01, l.o);
    }
}