import jade.core.Profile
import jade.core.ProfileImpl
import jade.wrapper.AgentController
import jade.wrapper.ContainerController
import jade.core.Runtime


fun main(){
    val myProfile = ProfileImpl()
    val myRuntime = Runtime.instance()
    val myContainer = myRuntime.createMainContainer(myProfile)
    try {
        val rma = myContainer.createNewAgent("rma","jade.tools.rma.rma",null)
        rma.start()


        val myAgent = myContainer.createNewAgent("Kenobi",TickerAgent::class.qualifiedName,null)
        myAgent.start()
    }catch (e: Exception){
        println("exception starting agent $e")
    }
}
