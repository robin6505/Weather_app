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
    val connection = driver.connection(List("localhost"))
    val db = connection("test")
    val collection = db("places")
    
    def listDocs(city: String) = {
        import scala.util.{Failure, Success}
      // Select only the documents which field 'firstName' equals 'Jack'
      val query = BSONDocument("places" -> "Zwolle")

    
      /* Let's run this query then enumerate the response and print a readable
       * representation of each document in the response */
      val responseDB = collection.
        find(query)
        
        val document = BSONDocument(
            "name" -> city)
    
        val future1 : Future[WriteResult] = collection.insert(document)
        
        future1.onComplete {
            case Failure(e) => throw e
            case Success(writeResult) => 
                println(s"Succesfully inserted the document with result: $writeResult")
        }
    }
    
    
    
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
        listDocs(city: String)
        for {
            resp <- getWeather(city: String)
            resp2 <- getForecast(city: String)
            
        } yield Ok(Json.obj(
            "CURRENTWEATHER" -> resp,
            "FORECAST" -> resp2))
    }
  
    def index = Action {
        Ok(views.html.index("Your new application is ready."))
    }

}
