import jade.core.Agent;

public class SimpleAgent extends Agent{

    protected void setup(){
        System.out.println("Hello! Agent " +getAID().getName()+" is ready.");
    }

}
