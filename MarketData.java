//MarketData contains a few of the Indicative data to make the program easier
public class MarketData{
    public double Price;        // market price of security
    public double stock0;       //stock price
    public double r;            //interest rate  ** INPUT AS PERCENTAGE
    public double sigma;        //the market value**INPUT AS PERCENTAGE */
    public double t0;           //current time

    MarketData(double mp, double st0, double ra, double sig, double t0)
    {
        //assign the vars
        this.Price = mp;
        this.stock0 = st0;
        this.r = ra/100;
        this.sigma = sig/100;
        this.t0 = t0;
    }
}