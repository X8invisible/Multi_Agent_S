import jade.core.Agent
import jade.core.behaviours.Behaviour
import jade.core.behaviours.TickerBehaviour

abstract class TickerAgent : Agent(){
    var t0= 9
    var loop : Behaviour? = null
    override fun setup() {
        loop = object: TickerBehaviour(this,300){
            override fun onTick() {
                println("${9-t0} :  ${myAgent.localName}")
            }

        }
        addBehaviour(loop)
    }

}