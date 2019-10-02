import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Hashtable;

public class Auctioneer extends Agent {

    // The catalogue of books for sale (maps the title of a book to its price)
    private Hashtable catalogue;
    private Hashtable bids;
    // The GUI by means of which the user can add books in the catalogue
    private BookSellerGui myGui;

    // Put agent initializations here
    protected void setup(){
        System.out.println("Auctioneer-agent "+getAID().getName()+" starting.");

        // Create the catalogue
        catalogue = new Hashtable();
        bids = new Hashtable();
        // Create and show the GUI
        myGui = new BookSellerGui(this);
        myGui.showGui();

        // Add the behaviour serving requests for offer from buyer agents
        addBehaviour(new OfferRequestsServer());

        // Add the behaviour serving purchase orders from buyer agents
        addBehaviour(new PurchaseOrdersServer());

        // Register the book-selling service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("auction-house");
        sd.setName("you-want-it-i-got-it");
        dfd.addServices(sd);

        try
        {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe)
        {
            fe.printStackTrace();
        }

    }

    // Put agent clean-up operations here
    protected void takeDown()
    {
        // Deregister from the yellow pages
        try
        {
            DFService.deregister(this);
        }
        catch (FIPAException fe)
        {
            fe.printStackTrace();
        }
        //

        // Close the GUI
        myGui.dispose();

        // Printout a dismissal message
        System.out.println("Auctioneer-agent "+getAID().getName()+" terminating.");
    }

    /**
     This is invoked by the GUI when the user adds a new item for sale
     */
    public void updateCatalogue(final String title, final int id)
    {
        addBehaviour(new OneShotBehaviour()
        {
            public void action()
            {
                catalogue.put(title, id);
            }
        } );
    }




    /**
     Inner class OfferRequestsServer.
     This is the behaviour used by Book-seller agents to serve incoming requests
     for offer from buyer agents.
     If the requested book is in the local catalogue the seller agent replies
     with a PROPOSE message specifying the price. Otherwise a REFUSE message is
     sent back.
     */
    private class OfferRequestsServer extends CyclicBehaviour
    {
        public void action()
        {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null)
            {
                // Message received. Process it
                String[] offer = msg.getContent().split(",");
                String title = offer[0];
                int price = Integer.parseInt(offer[1]);
                //ACLMessage reply = msg.createReply();
                if (catalogue.contains(title))
                {
                    //If the item is up for auction, remember the bid and the message
                    bids.put(msg, price);

                    // The requested book is available for sale. Reply with the price
                    //reply.setPerformative(ACLMessage.PROPOSE);
                    //reply.setContent(String.valueOf(price.intValue()));
                }
                else
                {
                    //System.out.println("no item with that title");
                    // The requested book is NOT available for sale.
                    //reply.setPerformative(ACLMessage.REFUSE);
                    //reply.setContent("not-available");
                }
                //myAgent.send(reply);
            }
            else
            {
                block();
            }
        }
    } // End of inner class OfferRequestsServer



    private class PurchaseOrdersServer extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            int maxBid = 0;
            for (Object i: bids.values()) {
                if(maxBid < (int) i){
                    maxBid = (int) i;
                }
            }
            ACLMessage msg = (ACLMessage) bids.get((Object) maxBid);
            if (msg != null) {
                // ACCEPT_PROPOSAL Message received. Process it
                String[] offer = msg.getContent().split(",");
                String title = offer[0];
                ACLMessage reply = msg.createReply();
                Integer id = (Integer) catalogue.remove(title);
                if (id != null) {
                    reply.setPerformative(ACLMessage.INFORM);
                    System.out.println(title+" sold to agent "+msg.getSender().getName());
                }
                else {
                    // The requested book has been sold to another buyer in the meanwhile .
                    reply.setPerformative(ACLMessage.FAILURE);
                    reply.setContent("not-available");
                }
                myAgent.send(reply);
            }
            else {
                block();
            }
        }
    } // End of inner class PurchaseOrdersServer

}