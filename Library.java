import java.util.*;

final class Library{
//------------------------------Member Variables--------------------------------------------------------------
    //Market Data from Market Data
    public double price;
    public double stock0;       //stock price
    public double r;            //interest rate  ** INPUT AS PERCENTAGE
    public double sigma;        //**INPUT AS PERCENTAGE */
    public double t0;           //current time
    public double delT;
    public double u;            
    public double d;
    public double p;
    public double q;
    public double negE;
    public double E;
    public MarketData md;

    //Indicative Data from Derivative
    public double k;            //strike price
    public double T;            //end time
    public int n;               //depth of the triangular tree
    public boolean putOrCall;
    public boolean AE;
    public Derivative deriv;

    //Output Object needed to be passed into vol
    public Output o;

    //Tree Structure Object
    public ArrayList<ArrayList<Node> > tree =                 //This is the tree object
                    new ArrayList<ArrayList<Node> >();

//------------------------------Constructor-----------------------------------------------------

    Library(){}
            
//------------------------------Bionimal Functions-----------------------------------------------

    //This version used for the first binom call
    Output binom(final Derivative deriv, final MarketData mkt, int n)
    {   
        //create an Output object
        this.o = new Output();

        //Assign the variables here
        assignVars(deriv, mkt, n);

        //Do the warnings here, in a function


        //Create the tree object and initialize the stock prices first, then the times
        buildTree();
        fillStock();
        setTimes();

        //Fill the market Values of the tree
        //If European option, the tree doesn't need to be modified further
        //If American, redo the Fair Values based on early excercise
        if(putOrCall == false){
            putTree();
            //if it's an american option, we need to modify more
            if(AE == false) AmericanTree();
        }
        if(putOrCall == true){
            callTree();
            if(AE == false) AmericanTree();
        }

        //print the data for verfying
        printStock();
        printFV();
        printFugit();

        // //assign the data for output and print them & return
        o.FV = tree.get(0).get(0).fairValue;
        o.fugit = tree.get(0).get(0).fugit;
        printOutput1();

        return o;
    }

    //This version used for impvol call, needed because it is the only way we can set volatility
    //because Market is passsed as final
    Output binom(final Derivative deriv, final MarketData mkt, int n, double sig)
    {   
        //create an Output object
        this.o = new Output();

        //Assign the variables here
        assignVars(deriv, mkt, n, sig);

        //Do the warnings here, in a function


        //Create the tree object and initialize the stock prices first, then the times
        buildTree();
        fillStock();
        setTimes();

        //Fill the market Values of the tree
        //If European option, the tree doesn't need to be modified further
        //If American, redo the Fair Values based on early excercise
        if(putOrCall == false){
            putTree();
            //if it's an american option, we need to modify more
            if(AE == false) AmericanTree();
        }
        if(putOrCall == true){
            callTree();
            if(AE == false) AmericanTree();
        }

        //assign the data for output and print them & return
        o.FV = tree.get(0).get(0).fairValue;
        o.fugit = tree.get(0).get(0).fugit;

        return o;
    }

//----------------------------------Volatility Function----------------------------------------------------

    int impvol(final Derivative deriv, final MarketData mkt, int n, int max_iter, double tol, Output out)
    {   
        //set boundaries and count
        double start = .01;
        double end = 2.00;
        int count = 1;

        //set initial sigma and the market price
        double pr= mkt.Price;
        double sig = (end-start)/2; 

        //get the initial Fv of the first iteration
        Library y = new Library();
        Output op = y.binom(deriv, mkt, n, sig);
        double tempFV = op.FV;

        //Run the binary search
        while(count < max_iter){
            //make range higher
            if(tempFV < pr ){
                start = sig;
                sig = start + ((end-start)/2);
            }
            // make range smaller
            else if(tempFV > pr ){
                end = sig;
                sig = end - ((end-start)/2);
            }

            //calculate the new Fair Value for the sigma found by the binary search
            Library x = new Library();
            Output op2 = x.binom(deriv, mkt, n, sig);
            tempFV = op2.FV;

            //test for if the correct FV is found
            if(tempFV >= pr-tol && tempFV <= pr+tol){
                out.impvol = sig * 100;
                out.num_iter = count;
                printOutput2(0);
                return 0;
            }
            count++;
        }
        out.impvol = 0;
        out.num_iter = 0;
        printOutput2(1);
        return 1;
    }

//----------------------------------Helper Functions----------------------------------------------------------
    
    //function to help assign all the required variables
    void assignVars(final Derivative deriv, final MarketData mkt, int n)
    {
        //assign the vars
        this.deriv = deriv;
        this.n = n;
        this.k = deriv.k;
        this.T = deriv.T;
        this.price = mkt.Price;
        this.stock0 = mkt.stock0;
        this.r = mkt.r;
        this.sigma = mkt.sigma;
        this.t0 = mkt.t0;
        this.putOrCall = deriv.putOrCall;
        this.AE = deriv.AE;
        this.md = mkt;

        //calculate delta T
        this.delT = (T-t0) / n;        //found delta of time

        //calulate u and d
        this.u = Math.exp((sigma * (Math.sqrt(delT))));      //u calulated here
        this.d = 1/u;
        
        //calulate p * q
        this.E = Math.exp(r * delT);
        this.negE = Math.exp(-r * delT);
        this.p = (E - d) / (u-d);
        this.q = 1-p;    
    }

    //function to help assign all the required variables, this one contains sigma assignment
    void assignVars(final Derivative deriv, final MarketData mkt, int n, double sig)
    {
        //assign the vars
        this.deriv = deriv;
        this.n = n;
        this.k = deriv.k;
        this.T = deriv.T;
        this.price = mkt.Price;
        this.stock0 = mkt.stock0;
        this.r = mkt.r;
        this.sigma = sig;               //volatility will be what we assign it
        this.t0 = mkt.t0;
        this.putOrCall = deriv.putOrCall;
        this.AE = deriv.AE;
        this.md = mkt;

        //calculate delta T
        this.delT = (T-t0) / n;        //found delta of time

        //calulate u and d
        this.u = Math.exp((sigma * (Math.sqrt(delT))));      //u calulated here
        this.d = 1/u;
        
        //calulate p * q
        this.E = Math.exp(r * delT);
        this.negE = Math.exp(-r * delT);
        this.p = (E - d) / (u-d);
        this.q = 1-p;    
    }

    //function for displaying warnings
    //fill in as needed
    boolean validation(){
        if(n < 3) 
        {
            System.out.println("Num of iterations too small");
            return false;
        }
        return true;
    }

    //The data structure for the triangular tree will be an Arraylist of an ArrayList of nodes
    //Each ArrayList will contain nodes from the Nodes class
    //First build the data structure
    void buildTree()
    {
        //initate the linked lists in the Likned Lists 
        for(int i=1; i <= n+1; i++){
            tree.add(new ArrayList<Node>());
        }

        //add 1 node at depth 1 manually
        tree.get(0).add(new Node());

        //add 2 nodes at depth 2 manually
        tree.get(1).add(new Node());
        tree.get(1).add(new Node());

        //iterate throught the Outer Array list and create n + 1 nodes at each depth
        int nodesToBeAdded = 3;
        //at depth 2 and onward, add nodes incrmeenting by 1 each time
        for (int i=2; i< tree.size(); i++) {
			for(int j = 1; j<= nodesToBeAdded; j++){
                tree.get(i).add(new Node());
            }
            nodesToBeAdded++;
		}
    }

    //process the tree as a call option tree
    void callTree(){
        //set the terminal nodes
        if(putOrCall == true){
            int depth = tree.size()-1;
            int size = tree.get(depth).size();
            //calculate the FV on the ends of the tree (last depth) for call option
            for(int i=0; i<size; i++){
                Node n = tree.get(depth).get(i);
                deriv.terminalCondition(n);               //virtual function called here
            }
        }

        //go through the rest of the tree and calculate the FV of each node
        int depth2 = tree.size()-2;
        for(int i=depth2; i > -1; i--){
            for(int j=0; j< tree.get(i).size(); j++){
                Node n = tree.get(i).get(j);
                
                double up = tree.get(i+1).get(j+1).fairValue;
                double down = tree.get(i+1).get(j).fairValue;
                n.fairValue = calcP(up, down);

                double up2 = tree.get(i+1).get(j+1).fugit;
                double down2 = tree.get(i+1).get(j).fugit;
                n.fugit = calcFugit(up2, down2);
            }
        }
    }

    //process the tree as a put option
    void putTree(){
        //set the terminal nodes
        if(putOrCall == false){
            int depth = tree.size()-1;
            int size = tree.get(depth).size();
            //calculate the FV on the ends of the tree (last depth) for call option
            for(int i=0; i<size; i++){
                Node n = tree.get(depth).get(i);
                deriv.terminalCondition(n);               //virtual fucntion called here
            }
        }

        //go through the rest of the tree and calculate the FV of each node
        int depth2 = tree.size()-2;
        for(int i=depth2; i > -1; i--){
            for(int j=0; j< tree.get(i).size(); j++){
                Node n = tree.get(i).get(j);

                double up = tree.get(i+1).get(j+1).fairValue;
                double down = tree.get(i+1).get(j).fairValue;
                n.fairValue = calcP(up, down);

                double up2 = tree.get(i+1).get(j+1).fugit;
                double down2 = tree.get(i+1).get(j).fugit;
                n.fugit = calcFugit(up2, down2);
            }
        }
    }

    //Will take the put or call tree and modify Fair Values if it's an American Option
    void AmericanTree(){
        //we are starting from second to last dpeth
        int depth2 = tree.size()-2;
        for(int i=depth2; i > -1; i--){
            for(int j=0; j< tree.get(i).size(); j++){

                //get the node
                Node n = tree.get(i).get(j);

                //First check if the nodes it derives from, have been excercised/changed
                //If so we need to recalculate the FV
                if(tree.get(i+1).get(j+1).changed == true || tree.get(i+1).get(j).changed == true){
                    //do this for the fair Value
                    double up = tree.get(i+1).get(j+1).fairValue;
                    double down = tree.get(i+1).get(j).fairValue;
                    n.fairValue = calcP(up, down);
                    n.changed = true;
                }

                //recalc for fugit
                double up2 = tree.get(i+1).get(j+1).fugit;
                double down2 = tree.get(i+1).get(j).fugit;
                n.fugit = calcFugit(up2, down2);

                //call the valuation test here
                deriv.valuationTest(n);

                //if valuation test sees node was excercised, subtrat delT form fugit
                //Must be done in librayr class because node doesn;t knwo delT
                if(n.exercise == true) n.fugit -= delT;
            }
        }
    }

    //fill the object with the stockData
    void fillStock(){
        //fill 1st node manually
        tree.get(0).get(0).stockPrice= stock0;

        //use loops to fill in the rest of the nodes
        //first calculate the outer stockPrices at each depth after 2
        for (int i=1; i< tree.size(); i++) 
        {   
            //right leaf of tree calulated
            double tempR = tree.get(i-1).get(i-1).stockPrice;
            tree.get(i).get(i).stockPrice = tempR * u;

            //left leaf of the tree calculated
            double tempL = tree.get(i-1).get(0).stockPrice;
            tree.get(i).get(0).stockPrice = tempL * d;
        }

        //Next fill in the data of the inner nodes
        //for each depth
        for (int i=2; i< tree.size(); i++) 
        {   
            //for each of the inner nodes
            for(int j=1; j<tree.get(i).size()-1; j++){
                //set them equal to the values of the nodes 2 depths above it
                tree.get(i).get(j).stockPrice = tree.get(i-2).get(j-1).stockPrice;
            }
        }
    }

    void setTimes(){
        double timeCounter= t0;
        for(int i=0; i< tree.size(); i++){
            for(int j=0; j<tree.get(i).size(); j++){
                tree.get(i).get(j).time = timeCounter;
            }
            timeCounter+= delT;
        }
    }

    double calcP(double upFV, double downFV){
        return negE * ((p * upFV) + (q * downFV));
    }

    double calcFugit(double upFug, double downFug){
        return ((p * upFug) + (q * downFug));
    }

//--------------------------------Debugging Print Functions----------------------------------------------

    //prints the tree with time data
    void printStock(){
        System.out.println();
        System.out.println("************ StockPrice of Tree ************");
        for(int i=0; i< tree.size(); i++){
            for(int j=0; j<tree.get(i).size(); j++){
                System.out.printf("%.3f", tree.get(i).get(j).stockPrice); 
                System.out.print("   ");
            }
            System.out.println();
        }
    }

    //prints the Stock Price tree, A star next to the node, means 
    void printFV(){
        System.out.println();
        System.out.println("************ FV's of Tree ************");
        for(int i=0; i< tree.size(); i++){
            for(int j=0; j<tree.get(i).size(); j++){
                System.out.printf("%.3f", tree.get(i).get(j).fairValue);
                if(tree.get(i).get(j).exercise == true) System.out.print("*");; 
                System.out.print("   ");
            }
            System.out.println();
        }
    }

    //Time printing function for Testing of the Bermudan Option
    void printTime(){
        System.out.println();
        System.out.println("************ Time's of Bermuda Tree ************");
        for(int i=0; i< tree.size(); i++){
            for(int j=0; j<tree.get(i).size(); j++){
                System.out.print(tree.get(i).get(j).time + "   "); 
            }
            System.out.println();
        }
    }

    //Printing function for Testing of the Bermudan Option
    void printFugit(){
        System.out.println();
        System.out.println("************ Fugit of Tree ************");
        for(int i=0; i< tree.size(); i++){
            for(int j=0; j<tree.get(i).size(); j++){
                System.out.printf("%.3f", tree.get(i).get(j).fugit);
                System.out.print("   ");
            }
            System.out.println();
        }
    }

    void printOutput1(){
        System.out.println();
        System.out.println("************ Output Data - Binom ************");
        System.out.print("The FV is:    ");
        System.out.printf("%.3f",o.FV);
        System.out.println();
        System.out.print("The Fugit is: ");
        System.out.printf("%.3f",o.fugit);
        System.out.println();
    }

    void printOutput2(int i){
        System.out.println();
        System.out.println("************ Output Data - ImpVol | Returned: " + i + " ************");
        System.out.print("The Implied Volatility is:   ");
        System.out.printf("%.3f",o.impvol);
        System.out.println("%");
        System.out.println("The Number of Iterations is: " + o.num_iter);
    }
}