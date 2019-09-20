import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.core.Runtime;
public class Application {
    public static void main(String[] args){
        //setup jade env
        Profile myProfile = new ProfileImpl();
        Runtime myRuntime = Runtime.instance();
        ContainerController myContainer = myRuntime.createMainContainer(myProfile);
        try {
            //start agent controller
            AgentController rma = myContainer.createNewAgent("rma","jade.tools.rma.rma",null);
            rma.start();

            //start simpleagent
            AgentController myAgent = myContainer.createNewAgent("Fred", TimerAgent.class.getCanonicalName(),null);
            myAgent.start();
        }catch (Exception e){
            System.out.println("exception starting agent: "+ e.toString());
        }
    }
}
