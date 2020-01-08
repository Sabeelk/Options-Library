//These are the fields of a node
//the fairValue of nodes will be recalulated
//The stock price is final, cannot be changed once calculated
public final class Node{
    boolean exercise = false;       //Exercised at this node, initialized to false
    boolean changed = false;        //This keeps track of if a node fv was changed
    double stockPrice;              //This stays the same once calculated
    double fairValue;               //Calculated Fair Value will go here
    double time;                    //The time of each node, for the bermuda option calculations
    double fugit;                   //Holds the FV of the pugit

    Node(){
    }

}