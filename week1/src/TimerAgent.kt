import jade.core.Agent
import jade.core.behaviours.TickerBehaviour

class TimerAgent : Agent(){
    var w = 15
    override fun setup() {
        addBehaviour(object: TickerBehaviour(this,1000){
            override fun onTick(){
                if(w >0){
                    println("$w seconds left.")
                    w-=1
                }else{
                    println("byee")
                    myAgent.doDelete()
                }
            }
        })
    }
}