import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;


public class BookBuyerAgent extends Agent{

    private String targetBookTitle;


    private AID[] sellerAgents = {
            new AID("seller1", AID.ISLOCALNAME),
            new AID("seller2", AID.ISLOCALNAME)
    };
    protected void setup(){
        System.out.println("Ahoy! Buyer Agent: "+getAID().getName()+" is ready");


        //get title of book to buy as startup argument
        Object[] args = getArguments();
        if(args != null && args.length > 0){
            targetBookTitle = (String) args[0];
            System.out.println("Trying to 'buy' "+ targetBookTitle);
            addBehaviour(new TickerBehaviour(this, 60000) {
                @Override
                protected void onTick() {
                    myAgent.addBehaviour(new RequestPerformer());
                }
            });
        }
        else{
            System.out.println("No book title specified :(");
            doDelete();
        }
    }
    //cleanup operations
    protected void takeDown(){
        System.out.println("Buyer Agent: "+getAID().getName()+" is retiring");
    }

    private class RequestPerformer extends Behaviour{
        private AID bestSeller;
        private int bestPrice;
        private int repliesCnt = 0; //the counter of replies from seller agents
        private MessageTemplate mt;
        private int step =0;

        public void action(){
            switch (step){
                case 0:
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    for (int i =0; i< sellerAgents.length; ++i){
                        cfp.addReceiver(sellerAgents[i]);
                    }
                    cfp.setContent(targetBookTitle);
                    cfp.setConversationId("book-trade");
                    cfp.setReplyWith("cfp"+System.currentTimeMillis());
                    myAgent.send(cfp);

                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("book-trade"),
                            MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                    step =1;
                    break;
                case 1:
                    ACLMessage reply = myAgent.receive(mt);
                    if(reply != null){
                        if(reply.getPerformative() == ACLMessage.PROPOSE){
                            //this is an offer
                            int price = Integer.parseInt(reply.getContent());
                            if(bestSeller == null || price < bestPrice){
                                bestPrice = price;
                                bestSeller = reply.getSender();
                            }
                        }
                        repliesCnt++;
                        if(repliesCnt >= sellerAgents.length){
                            //all replies received
                            step = 2;
                        }
                    }else {
                        block();
                    }
                    break;
                case 2:
                    //send purchase order to the seller that had best offer
                    ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                    order.addReceiver(bestSeller);
                    order.setContent(targetBookTitle);
                    order.setConversationId("book-trade");
                    order.setReplyWith("order"+System.currentTimeMillis());
                    myAgent.send(order);
                    //prepare templtate to get purchase order reply
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("book-trade"),
                            MessageTemplate.MatchInReplyTo(order.getReplyWith()));
                    step =3;
                    break;
                case 3:
                    //receive purchase order reply
                    reply = myAgent.receive(mt);
                    if(reply !=null){
                        if(reply.getPerformative() == ACLMessage.INFORM){
                            //purchase succesful
                            System.out.println(targetBookTitle+" succesfully acquired");
                            System.out.println("Price = "+bestPrice);
                            myAgent.doDelete();
                        }
                        step = 4;
                    }else {
                        block();
                    }
                    break;
            }
        }
        public boolean done(){
            return ((step == 2 && bestSeller == null) || step == 4);
        }
    }
}
