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

class Application @Inject() (ws: WSClient) extends Controller {
    
        
    val apiKey = "&APPID=2058452e3cf07873e426f1d723339ec6"
    
    val driver = new MongoDriver
    val connection = driver.connection(List("172.17.0.1"))
    val db = connection("test")
    val collection = db("places")
    
    def storePlace(city: String) = {
        val cityNoCase = city.toLowerCase();
        val query = BSONDocument("name" -> cityNoCase)
        
        val cursor = collection.find(query).cursor[BSONDocument]
        val futureList2: Future[List[BSONDocument]] = cursor.collect[List]()
        
        //store place name if not in database
        futureList2.map { list =>
            println("ok, got the list: " + list)
            println("Length of list is: " + list.length)
            if (list.length == 0) {
				println("Insert cityname: " + cityNoCase)
                val document = BSONDocument("name" -> cityNoCase)
                collection.insert(document)
            }
        }
    }
    
    
    //getWeather function
    def getWeather(city: String) : Future[JsValue] = {
        val url = (s"http://api.openweathermap.org/data/2.5/weather?q=$city&units=metric$apiKey")
        val request = WS.url(url).get
        request map{response => response.json}
    }
    
    def getForecast(city: String) : Future[JsValue] = {
        val url = (s"http://api.openweathermap.org/data/2.5/forecast/daily?q=$city&cnt=6&units=metric$apiKey")
        val request = WS.url(url).get
        request map{response => response.json}
    }
    
    def getData(city: String) = Action.async {
        
        storePlace(city: String)
        for {
            resp <- getWeather(city: String)
            resp2 <- getForecast(city: String)
            
        } yield Ok(Json.obj(
            "CURRENTWEATHER" -> resp,
            "FORECAST" -> resp2)).withHeaders(
                ACCESS_CONTROL_ALLOW_ORIGIN -> "*",
                ACCESS_CONTROL_ALLOW_CREDENTIALS -> "true"
            )
            
    }
  
    def index = Action {
        Ok(views.html.index("Your new application is ready."))
    }

}
