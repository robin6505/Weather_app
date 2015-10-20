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
import reactivemongo.bson.BSONDocument
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.WriteResult

import play.modules.reactivemongo.json.BSONFormats._

import play.modules.reactivemongo.json.BSONFormats

import play.modules.reactivemongo.json._
import play.modules.reactivemongo.json.collection.{
  JSONCollection, JsCursor
}, JsCursor._

class Application @Inject() (ws: WSClient) extends Controller {
    
        
    val apiKey = "&APPID=2058452e3cf07873e426f1d723339ec6"
    
    
    //getWeather function
    def getWeather(city: String) : Future[JsValue] = {
        val url = (s"http://api.openweathermap.org/data/2.5/weather?q=$city$apiKey")
        val request = WS.url(url).get
        request map{response => response.json}
    }
    
    def getForecast(city: String) : Future[JsValue] = {
        val url = (s"http://api.openweathermap.org/data/2.5/forecast/daily?q=$city&cnt=6&units=metric$apiKey")
        val request = WS.url(url).get
        request map{response => response.json}
    }
    
    def getData(city: String) = Action.async {
        val driver = new MongoDriver
        val connection = driver.connection(List("188.226.144.15"))
        val db = connection("test")
        val collectionGet = db("measurements")
			
		//Used to store analytical data
		val collectionStore: JSONCollection = db("analytics")
		
		val map = """ 
		function() {
                       emit(this.CONDITION.name, this.CONDITION.main.temp);
                   };
		"""
		
		val reduce = """ 
		    function(keyName, valuesTemp) {
                          return Array.sum(valuesTemp);
                      };
		
		"""
		
		val mapReduceCommand = BSONDocument(
            "mapreduce" -> "measurements",
            "map" -> BSONString(map),
            "reduce" -> BSONString(reduce),
            "out" -> BSONDocument("replace" -> "analytics")
        )
        
        val result = db.command(RawCommand(mapReduceCommand))
        
        
            
    }
  
    def index = Action {
        Ok(views.html.index("Your new application is ready."))
    }

}
