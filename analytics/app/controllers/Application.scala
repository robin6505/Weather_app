package controllers

import javax.inject.Inject
import play.api.mvc._
import reactivemongo.api._
import reactivemongo.bson._
import play.modules.reactivemongo._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import reactivemongo.core.commands._


class Application @Inject() (val reactiveMongoApi: ReactiveMongoApi) 
    extends Controller with MongoController with ReactiveMongoComponents{
    
    def doAnalytics() =  {
        val driver = new MongoDriver
        val connection = driver.connection(List("188.226.144.15"))
        val db = connection("test")
        val collectionGet = db("measurements")
		
		//For each city get temp and keep track of how many records are there for each city
		val map = """ 
    		function() {
                var key = this.CONDITION.name;
                var value = {
                    count: 1,
                    temp: this.CONDITION.main.temp
                };
                               
                emit(key, value);
            };
		"""
		
		//Sum temps of all records for each city
		val reduce = """ 
            function(keyName, countObjVals) {
                reducedVal = { count: 0, temp: 0 };
                
                for (var idx = 0; idx < countObjVals.length; idx++) {
                    reducedVal.count += countObjVals[idx].count;
                    reducedVal.temp += countObjVals[idx].temp;
                }
                
                return reducedVal;
            };
		
		"""
		
		//Calculate average temp over whole recorded history for each city
		//and assign the city to a temperature group
		val finalize = """
		    function (key, reducedVal) {

                reducedVal.avg = reducedVal.temp/reducedVal.count;
                reducedVal.tempGroup = parseInt(reducedVal.avg/10);
                
                return reducedVal;
                
            };
        """
		//Create the command and put the data in the analytics collection
		val mapReduceCommand = BSONDocument(
            "mapreduce" -> "measurements",
            "map" -> BSONString(map),
            "reduce" -> BSONString(reduce),
            "finalize" -> BSONString(finalize),
            "out" -> BSONDocument("replace" -> "analytics")
        )
        
        //execute the command
        val result = db.command(RawCommand(mapReduceCommand))
        
    }
    
    //Calculates analytics every hour
    def start() = {
        val system = akka.actor.ActorSystem("system")
        println("word uitgevoerd")
        system.scheduler.schedule(0 seconds, 1 hours)(doAnalytics)
    }
    start()
    

    def index = Action {
        Ok(views.html.index("Your new application is ready."))
    }

}
