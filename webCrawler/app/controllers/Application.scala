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


class Application @Inject() (val reactiveMongoApi: ReactiveMongoApi) 
    extends Controller with MongoController with ReactiveMongoComponents{
    
    def getWeather(city: String) : Future[JsValue] = {
        val apiKey = "&APPID=2058452e3cf07873e426f1d723339ec6"
        val url = (s"http://api.openweathermap.org/data/2.5/weather?q=$city$apiKey")
        val request = WS.url(url).get
        request map{response => response.json}
    }
    
    //Nog toevoegen timer interval en tijd etc aan object toevoegen
    def crawlData() = {
		println("Executes function in interval test")
		
		//First get available mongo instances
		val url = (s"http://127.0.0.1:4001/v2/keys/announce/services/mongo")
        val request = WS.url(url).get
        request map{response => 
            println(response.json) //when mongo is retrieved do below
            val mongoresp = response.json
            val serversResp = (mongoresp \\ "value").map(_.as[String])
            println("These are the ips: " + serversResp)
        
			//create time stamp for each set of retrieved data
			val timestamp = Calendar.getInstance().getTime()
			
        
			def driver: reactivemongo.api.MongoDriver = ???
			val servers = serversResp
			
			//connect to server with authorization
			val dbName = "test"
			val userName = "siteRootAdmin"
			val password = "password"
			val credentials = List(Authenticate(dbName, userName, password))
			val connection = driver.connection(servers, authentications = credentials)
        
        
        
			//val driver = new MongoDriver
			//def driver: reactivemongo.api.MongoDriver = ???
			
			//val connection = driver.connection(servers)
			val db = connection("test")
			
			//First retrieve all the city names stored in the database
			val collectionGet = db("places")
			
			//op deze manier kun je er meteen JSON in gooien. Met alles doen?
			val collectionStore: JSONCollection = db("measurements")
			
			val query = BSONDocument()
			// select only the fields 'name'
			val filter = BSONDocument("name" -> 1, "_id" -> 0)
			//implicit def authTimeout: scala.concurrent.duration.FiniteDuration = ???
			//val futureAuthenticated = db.authenticate("siteRootAdmin", "password")

			  //futureAuthenticated.map { _ =>
				// doSomething
			  
			
			val futureList: Future[List[BSONDocument]] =
				collectionGet.
				  find(query, filter).
				  cursor[BSONDocument].
				  collect[List]()
			
			//Kijken of dit mooier kan? werkt nu
			  futureList.map { list =>
				val uniqueList = list.distinct
				uniqueList.foreach { doc =>
				  println(s"found document: ${BSONDocument pretty doc}")
				  println(doc.get("name"))
				  doc.get("name") match {
					  case Some(BSONString(name)) => {
						  val current = getWeather(name)
						  current.map {
							  resp => {
								  //println(resp)
								  val combined = Json.obj("TIMESTAMP" -> timestamp, "CONDITION" -> resp)
								  //println(combined)
								  collectionStore.insert(combined.as[JsObject])
							  }
						  }
					  }
					  case _ => println("Could not get city name")
				  }
				}
			  }
		  //} closes db.auth future method
	  } //closes etcd mongo ip request
    }
    
    def start() = {
        val system = akka.actor.ActorSystem("system")
        println("word uitgevoerd")
        system.scheduler.schedule(0 seconds, 15 minutes)(crawlData)
    }
    start()
    

    def index = Action {
        Ok(views.html.index("Your new application is ready."))
    }

}
