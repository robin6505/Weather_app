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


class Application @Inject() (val reactiveMongoApi: ReactiveMongoApi) 
    extends Controller with MongoController with ReactiveMongoComponents{
    
    def getWeather(city: String) : Future[JsValue] = {
        val apiKey = "&APPID=2058452e3cf07873e426f1d723339ec6"
        val url = (s"http://api.openweathermap.org/data/2.5/weather?q=$city$apiKey")
        val request = WS.url(url).get
        request map{response => response.json}
    }
    
    //Nog toevoegen timer interval en tijd etc aan object toevoegen
    def crawlData() = Action {
        val driver = new MongoDriver
        val connection = driver.connection(List("localhost"))
        val db = connection("test")
        
        //First retrieve all the city names stored in the database
        val collectionGet = db("places")
        
        //op deze manier kun je er meteen JSON in gooien. Met alles doen?
        val collectionStore: JSONCollection = db("measurements")
        
        val query = BSONDocument()
        // select only the fields 'name'
        val filter = BSONDocument("name" -> 1, "_id" -> 0)
        
        val futureList: Future[List[BSONDocument]] =
            collectionGet.
              find(query, filter).
              cursor[BSONDocument].
              collect[List]()
        
        //Kijken of dit mooier kan? werkt nu
          futureList.map { list =>
            list.foreach { doc =>
              println(s"found document: ${BSONDocument pretty doc}")
              println(doc.get("name"))
              doc.get("name") match {
                  case Some(BSONString(name)) => {
                      val current = getWeather(name)
                      current.map {
                          resp => {
                              println(resp)
                              collectionStore.insert(resp.as[JsObject])
                          }
                      }
                  }
                  case _ => println("Could not get city name")
              }
            }
          }
          
          Ok("JA DOEI")
    }
    

    def index = Action {
        Ok(views.html.index("Your new application is ready."))
    }

}
