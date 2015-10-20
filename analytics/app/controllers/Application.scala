package controllers

import play.api._
import play.api.libs.json._
import javax.inject.Inject
import scala.concurrent.Future
import play.api.Play.current
import play.api.mvc._
import play.api.libs.ws._

import play.api.libs.concurrent.Execution.Implicits._
import java.util.concurrent.TimeoutException

import reactivemongo.api._
import scala.concurrent.ExecutionContext.Implicits.global

import play.api.libs.iteratee.Iteratee
import reactivemongo.bson._
import reactivemongo.bson.BSONDocument
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.WriteResult


import play.api.mvc.Controller
import play.modules.reactivemongo._
import play.modules.reactivemongo.json.BSONFormats._

import play.modules.reactivemongo.json.BSONFormats

import play.modules.reactivemongo.json._
import play.modules.reactivemongo.json.collection.{
  JSONCollection, JsCursor
}, JsCursor._

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import java.util.Calendar
import reactivemongo.core.nodeset.Authenticate
import reactivemongo.core.commands._


class Application @Inject() (val reactiveMongoApi: ReactiveMongoApi) 
    extends Controller with MongoController with ReactiveMongoComponents{
    
    def doAnalytics() =  {
        val driver = new MongoDriver
        val connection = driver.connection(List("188.226.144.15"))
        val db = connection("test")
        val collectionGet = db("measurements")
			
		//Used to store analytical data
		val collectionStore: JSONCollection = db("analytics")
		
		/*val map = """ 
		function() {
                       emit(this.CONDITION.name, this.CONDITION.main.temp);
                   };
		"""*/
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
		
		
		/*val reduce = """ 
		    function(keyName, valuesTemp) {
                          return Array.sum(valuesTemp);
                      };
		
		"""*/
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
		val finalize = """
		    function (key, reducedVal) {

                       reducedVal.avg = reducedVal.temp/reducedVal.count;

                       return reducedVal;

                    };
            """
		
		val mapReduceCommand = BSONDocument(
            "mapreduce" -> "measurements",
            "map" -> BSONString(map),
            "reduce" -> BSONString(reduce),
            "finalize" -> BSONString(finalize),
            "out" -> BSONDocument("replace" -> "analytics")
        )
        
        val result = db.command(RawCommand(mapReduceCommand))
        
        
            
    }
    
    def start() = {
        val system = akka.actor.ActorSystem("system")
        println("word uitgevoerd")
        system.scheduler.schedule(0 seconds, 15 minutes)(doAnalytics)
    }
    start()
    

    def index = Action {
        Ok(views.html.index("Your new application is ready."))
    }

}
