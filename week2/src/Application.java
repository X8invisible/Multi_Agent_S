import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

public class Application
{

    public static void main(String[] args)
    {
        //Setup the JADE environment
        Profile myProfile = new ProfileImpl();
        Runtime myRuntime = Runtime.instance();
        ContainerController myContainer = myRuntime.createMainContainer(myProfile);
        try
        {
            //Start the agent controller, which is itself an agent (rma)
            AgentController rma = myContainer.createNewAgent("rma", "jade.tools.rma.rma", null);
            rma.start();

            String[] books = {"Java for Dummies"};

            //Now start our own BookBuyerAgent, called buyer.
            //AgentController myAgent = myContainer.createNewAgent("buyer", BookBuyerAgent.class.getCanonicalName(), books);
            //myAgent.start();

            AgentController myAgent2 = myContainer.createNewAgent("sellerA", BookSellerAgent.class.getCanonicalName(), null);
            myAgent2.start();

            //Now start our own BookBuyerAgent, called buyer.
            //AgentController myAgent = myContainer.createNewAgent("buyer", BookBuyerAgent.class.getCanonicalName(), books);
            //myAgent.start();

        }
        catch(Exception e)
        {
            System.out.println("Exception starting agent: " + e.toString());
        }
    }
}