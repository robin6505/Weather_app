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


import play.api.libs.iteratee._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.iteratee.{Concurrent, Enumerator, Iteratee}
import play.api.libs.concurrent.Promise

class Application @Inject() (ws: WSClient) extends Controller {
    
    //initialize dummy data, than update with analytics get function
    var analyticData: JsValue = Json.parse("""
    { "_id" : "Amsterdam", "value" : { "count" : 114, "temp" : 32021.42000000001, "avg" : 280.8896491228071 } }
{ "_id" : "Berlin", "value" : { "count" : 129, "temp" : 36047.29500000002, "avg" : 279.4363953488374 } }
{ "_id" : "Connaught Place", "value" : { "count" : 129, "temp" : 38905.665000000095, "avg" : 301.59430232558213 } }
{ "_id" : "Groningen", "value" : { "count" : 135, "temp" : 37912.67100000002, "avg" : 280.83460000000014 } }
{ "_id" : "London", "value" : { "count" : 130, "temp" : 36841.685, "avg" : 283.3975769230769 } }
{ "_id" : "Madrid", "value" : { "count" : 130, "temp" : 37319.16900000004, "avg" : 287.0705307692311 } }
{ "_id" : "Meppel", "value" : { "count" : 135, "temp" : 37811.921, "avg" : 280.08830370370373 } }
{ "_id" : "New York", "value" : { "count" : 83, "temp" : 23676.954999999984, "avg" : 285.264518072289 } }
{ "_id" : "Zwolle", "value" : { "count" : 135, "temp" : 37745.511000000035, "avg" : 279.59637777777806 } }
    """)
    
        
    val apiKey = "&APPID=2058452e3cf07873e426f1d723339ec6"
    
    val driver = new MongoDriver
    val connection = driver.connection(List("188.226.144.15"))
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
    def retrieveAnalytics() = {
        val query = BSONDocument()
        val collectionAnalytics = db("analytics")
        val cursor = collectionAnalytics.find(query).cursor[BSONDocument]
        val futureList3: Future[List[BSONDocument]] = cursor.collect[List]()
        
        //store place name if not in database
        futureList3.map { list =>
            //println("ok, got the list: " + list)
            println("Length of the analytics list is: " + list.length)
            val uniqueList = list.distinct
            uniqueList.foreach { doc =>
              println(s"found document: ${BSONDocument pretty doc}")
              //Create json object
            }
            
            //analyticData = BSONFormats.BSONDocumentFormat.writes(list).as[JsValue]
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
        retrieveAnalytics() 
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
    // sends the time every second, ignores any input
    def wsTime = WebSocket.using[JsValue] {
        request =>
          Logger.info(s"wsTime, client connected.")
        
          val outEnumerator: Enumerator[JsValue] = Enumerator.repeatM(Promise.timeout(analyticData, 5000))
          val inIteratee: Iteratee[JsValue, Unit] = Iteratee.ignore[JsValue]
        
          (inIteratee, outEnumerator)
    }
    
    
    //Websocket implementation
    def socket1 = WebSocket.using[String] { request =>

      // Log events to the console
      val in = Iteratee.foreach[String](println).map { _ =>
        println("Disconnected")
      }
    
      // Send a single 'Hello!' message
      val out = Enumerator("Hello!")
    
      (in, out)
    }
  
    def index = Action {
        Ok(views.html.index("Your new application is ready."))
    }

}
