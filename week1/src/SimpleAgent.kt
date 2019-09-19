import jade.core.Agent

class SimpleAgent : Agent() {
    protected override fun setup(){
        println("hello there! Agent "+getAID().name+" is ready,")
    }
}