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

class Application @Inject() (ws: WSClient) extends Controller {
    
        
    val apiKey = "&APPID=2058452e3cf07873e426f1d723339ec6"
    
    val driver = new MongoDriver
    val connection = driver.connection(List("localhost"))
    val db = connection("test")
    val collection = db("places")
    
    def listDocs(collection: BSONCollection) = {
      // Select only the documents which field 'firstName' equals 'Jack'
      val query = BSONDocument("places" -> "Zwolle")

    
      /* Let's run this query then enumerate the response and print a readable
       * representation of each document in the response */
      collection.
        find(query).
        cursor[BSONDocument].
        enumerate().apply(Iteratee.foreach { doc =>
          println(s"found document: ${BSONDocument pretty doc}")
        })
    
      // Or, the same with getting a list
      //val futureList: Future[List[BSONDocument]] =
        //collection.
          //find(query, filter).
          //cursor[BSONDocument].
        //  collect[List]()
    
      //futureList.map { list =>
        //list.foreach { doc =>
          //println(s"found document: ${BSONDocument pretty doc}")
        //}
      //}
    }
    
    
    
    //def findPlaces = Action {
        // let's do our query
      //  val cursor = collection.
          // find all
        //  find(Json.obj())
          // sort them by creation date
          //println(cursor)
          // perform the query and get a cursor of JsObject
         // Ok("lala")
    //  }
    
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
        listDocs(collection: BSONCollection)
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
