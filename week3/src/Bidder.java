import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Bidder extends Agent{

    // The title of the book to buy
    private String targetBookTitle;
    private int maxPrice = 100;
    //The list of known seller agents
    private AID[] sellerAgents = {new AID("seller1", AID.ISLOCALNAME), new AID("seller2", AID.ISLOCALNAME)};

    //Put agent initializations here
    protected void setup() {

        //Print a welcome message
        System.out.println("Hello! Bidder-agent "+getAID().getName()+" is ready.");

        // Get the title of the book to buy as a start-up argument
        Object[] args = getArguments();

        if (args != null && args.length > 0) {

            targetBookTitle = (String) args[0];

            System.out.println("Trying to bid for "+targetBookTitle);

            //Add a TickerBehaviour that schedules a request to seller agents every minute
            addBehaviour(new TickerBehaviour(this, 1000) {

                protected void onTick() {

                    // Update the list of seller agents
                    DFAgentDescription template = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType("auction-house");
                    template.addServices(sd);
                    try
                    {
                        DFAgentDescription[] result = DFService.search(myAgent, template);
                        sellerAgents = new AID[result.length];
                        for (int i = 0; i < result.length; ++i)
                        {
                            sellerAgents[i] = result[i].getName();
                        }
                    }
                    catch (FIPAException fe) {
                        fe.printStackTrace();
                    }


                    // Perform the request
                    myAgent.addBehaviour(new RequestPerformer());
                }
            } );
        }
        else {

            // Make the agent terminate immediately
            System.out.println("No book title specified");
            doDelete();
        }
    }

    // Put agent clean-up operations here
    protected void takeDown() {
        // Printout a dismissal message
        System.out.println("Buyer-agent "+getAID().getName()+" terminating.");
    }




    /**
     Inner class RequestPerformer.
     This is the behaviour used by Book-buyer agents to request seller
     agents the target book.
     */
    private class RequestPerformer extends Behaviour {
        private int bestPrice = (int)(maxPrice* Math.random());
        private MessageTemplate mt; // The template to receive replies
        private int step = 0;
        public void action() {
            switch (step)
            {
                case 0:
                    // Send the cfp to all sellers
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    for (int i = 0; i < sellerAgents.length; ++i)
                    {
                        cfp.addReceiver(sellerAgents[i]);
                    }
                    cfp.setContent(targetBookTitle+","+bestPrice);
                    cfp.setConversationId("auction-house");
                    cfp.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
                    myAgent.send(cfp);
                    // Prepare the template to get proposals
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("auction-house"), MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                    //System.out.println(getAID().getName()+" sent bid");
                    step = 1;
                    break;
                case 1:
                    // Receive the purchase order reply

                    ACLMessage reply = myAgent.receive(mt);
                    if (reply != null)
                    {
                        System.out.println(getAID().getName()+" Waiting for response on bid");
                        // Purchase order reply received
                        if (reply.getPerformative() == ACLMessage.INFORM)
                        {
                            // Purchase successful. We can terminate
                            System.out.println(targetBookTitle+" successfully purchased.");
                            System.out.println("Price = "+bestPrice);
                            myAgent.doDelete();
                        }
                        step = 4;
                    }
                    else
                    {
                        block();
                    }
                    break;
            }
        }
        public boolean done()
        {
            return ((step != 1) || step == 4);
        }
    } // End of inner class RequestPerformer





}