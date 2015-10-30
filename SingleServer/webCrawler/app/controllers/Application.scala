package controllers


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
import java.util.Calendar


class Application @Inject() (val reactiveMongoApi: ReactiveMongoApi) 
    extends Controller with MongoController with ReactiveMongoComponents{
    
    //Function to get current weather
    def getWeather(city: String) : Future[JsValue] = {
        
        //Information for the Open Weather Map API
        val apiKey = "&APPID=2058452e3cf07873e426f1d723339ec6"
        val url = (s"http://api.openweathermap.org/data/2.5/weather?q=$city&units=metric$apiKey")
        val request = WS.url(url).get
        request map{response => response.json}
    }
    
    //Function that gets current weather for all places in database and stores it in the database
    def crawlData() = {
        
        //create timestamp in case we want to do analytics based an a certain time span
		val timestamp = Calendar.getInstance().getTime()
		
        val driver = new MongoDriver
        val connection = driver.connection(List("188.226.144.15"))
        val db = connection("test")
        
        //First retrieve all the city names stored in the database
        val collectionGet = db("places")
        
        //This way we can store Json object directly into database without converting to BSON by ourselves
        val collectionStore: JSONCollection = db("measurements")
        //Empty query to get all places from database
        val query = BSONDocument()
        // select only the field 'name'
        val filter = BSONDocument("name" -> 1, "_id" -> 0)  
        
        val futureList: Future[List[BSONDocument]] =
            collectionGet.
            find(query, filter).
            cursor[BSONDocument].
            collect[List]()
        
        //When get response from database do following
        futureList.map { list =>
			val uniqueList = list.distinct //make sure there are nu duplicates
            uniqueList.foreach { doc =>     //for each place get current weather
                doc.get("name") match {
                    case Some(BSONString(name)) => {
                        val current = getWeather(name)
                        current.map {
                            resp => {
                                //Combine weather data with timestamp
							    val combined = Json.obj("TIMESTAMP" -> timestamp, "CONDITION" -> resp)
							    //and store it in the database
							    collectionStore.insert(combined.as[JsObject])
                            }
                        }
                    }
                    case _ => println("Could not get city name")
                }
            }
        }
    }
    //Function to get the weatherdata in an time interval of an hour
    def start() = {
        val system = akka.actor.ActorSystem("system")
        println("started crawler")
        system.scheduler.schedule(0 seconds, 1 hours)(crawlData)
    }
    start()
    
    def index = Action {
        Ok(views.html.index("Your new application is ready."))
    }

}
