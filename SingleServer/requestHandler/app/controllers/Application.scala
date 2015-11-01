//This app is the request Handler for the Weather App

package controllers

import play.api._
import play.api.libs.json._
import javax.inject.Inject
import scala.concurrent.Future
import play.api.Play.current
import play.api.mvc._
import play.api.libs.ws._
import reactivemongo.api._
import reactivemongo.bson._
import reactivemongo.bson.BSONDocument
import reactivemongo.api.collections.bson.BSONCollection
import play.modules.reactivemongo._
import play.modules.reactivemongo.json._
import play.modules.reactivemongo.json.collection.{
  JSONCollection, JsCursor
}, JsCursor._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import play.api.libs.iteratee.{Concurrent, Enumerator, Iteratee}
import play.api.libs.concurrent.Promise

class Application @Inject() extends Controller {
    
    //initialize empty variable streamData, than update with streamData function
    var streamData: JsValue = Json.parse("{}")
    
    //api key for Open Weather Map    
    val apiKey = "&APPID=2058452e3cf07873e426f1d723339ec6"
    
    val driver = new MongoDriver
    val connection = driver.connection(List("188.226.144.15"))
    val db = connection("test")
    val collection = db("places")
    
    //Function to store all places searched by user. This way we automatically build a database
    //The webcrawler will get this list before fetching all weather data
    def storePlace(city: String) = {
        val cityNoCase = city.toLowerCase();
        val query = BSONDocument("name" -> cityNoCase)
        
        val cursor = collection.find(query).cursor[BSONDocument]
        val futureList2: Future[List[BSONDocument]] = cursor.collect[List]()
        
        //store place name if not in database
        futureList2.map { list =>
            if (list.length == 0) { //length of list is 0 if not in database
                val document = BSONDocument("name" -> cityNoCase)
                collection.insert(document)
            }
        }
    }
    
    def getAnalytics() : Future[JsValue] = {
        val query = BSONDocument()
        val collectionAnalytics: JSONCollection = db("analytics")
        val cursor = collectionAnalytics.find(query).cursor[JsValue]
        val futureList3: Future[List[JsValue]] = cursor.collect[List]()
        
        //Combine all items in the list into one Json Array
        futureList3.map { list => Json.toJson(list) }
    }
    
    def getStreamData() = {
        val query = BSONDocument()
        val numbPlaces = collection.count()
        val futureCount: Future[Int] = numbPlaces
        
        //Get random city from database collection places
        futureCount.map { limit =>
            val r = scala.util.Random
            val randNumb = r.nextInt(limit-1)
            val randCity = collection.find(BSONDocument()).options(QueryOpts(skipN = randNumb)).one[BSONDocument]
            val futureRandCity: Future[Option[BSONDocument]] = randCity
            
            //extract city name from returned BSON object and get the current weather for that city
            //and store this data in global streamData variable
            futureRandCity.map { 
                doc => { 
                    val stad = doc.get
                    stad.get("name") match {
                        case Some(BSONString(name)) => {
                            val current = getWeather(name)
                            current.map {
                                resp => {
                                    streamData = resp
                                }
    					    }
                        }
                        case _ => println("Could not get city name")
                        
                    }
                }
            }
        }
        
    }
        
    
    //get current Weather function
    def getWeather(city: String) : Future[JsValue] = {
        val url = (s"http://api.openweathermap.org/data/2.5/weather?q=$city&units=metric$apiKey")
        val request = WS.url(url).get
        request map{response => response.json}
    }
    //get Forecast function
    def getForecast(city: String) : Future[JsValue] = {
        val url = (s"http://api.openweathermap.org/data/2.5/forecast/daily?q=$city&cnt=6&units=metric$apiKey")
        val request = WS.url(url).get
        request map{response => response.json}
    }
    
    //Main function where the http request goes to
    //Gets all data and combines it into one Json object and sends it back to the front-end
    def getData(city: String) = Action.async {
        
        storePlace(city: String)
        for {
            resp <- getWeather(city: String)
            resp2 <- getForecast(city: String)
            resp3 <- getAnalytics()
            
        } yield Ok(Json.obj(
            "CURRENTWEATHER" -> resp,
            "FORECAST" -> resp2,
            "ANALYTICS" -> resp3)).withHeaders(
                ACCESS_CONTROL_ALLOW_ORIGIN -> "*",
                ACCESS_CONTROL_ALLOW_CREDENTIALS -> "true"
            )
            
    }
    //Websocket that sends current weather from random city from database to the front end. It does this every 7 seconds
    def wsStream = WebSocket.using[JsValue] {
        request =>
            Logger.info(s"wsStream, client connected.")
            
            val outEnumerator: Enumerator[JsValue] = Enumerator.repeatM(Promise.timeout(streamData, 7000))
            val inIteratee: Iteratee[JsValue, Unit] = Iteratee.ignore[JsValue]
            
            (inIteratee, outEnumerator)
    }
    
    //Function to change the data in streamData every 7 seconds
    def startGetStreamData() = {
        val system = akka.actor.ActorSystem("system")
        println("Stream data is updated every 7 seconds")
        system.scheduler.schedule(0 seconds, 7 seconds)(getStreamData)
    }
    startGetStreamData()
  
    def index = Action {
        Ok(views.html.index("Your new application is ready."))
    }

}
