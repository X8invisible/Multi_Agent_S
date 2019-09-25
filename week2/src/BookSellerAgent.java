import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.*;


public class BookSellerAgent extends Agent{

    private Hashtable catalogue;

    private BookSellerGui myGui;

    protected void setup(){
        catalogue = new Hashtable();

        myGui = new BookSellerGui(this);
        myGui.showGui();

        //behaviour serving req for offer from buyer
        addBehaviour(new OfferRequestServer());
        //serving purchase orders from buyer
        addBehaviour(new PurchaseOrdersServer());

    }

    protected void takeDown(){
        myGui.dispose();

        System.out.println("Seller agent: "+getAID().getName()+" terminating");
    }
    public void updateCatalogue(final String title, final int price){
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                catalogue.put(title, new Integer(price))
            }
        });
    }
    private class OfferRequestServer extends CyclicBehaviour {
        public void action(){
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive(mt);
            if(msg != null){
                String title = msg.getContent();
                ACLMessage reply = msg.createReply();

                Integer price = (Integer) catalogue.get(title);
                if(price != null){
                    reply.setPerformative(ACLMessage.PROPOSE);

                }
            }else {
                block();
            }
        }
    }

    private class PurchaseOrdersServer extends CyclicBehaviour{
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
        }
    }

}
