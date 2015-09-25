package controllers

import play.api._
import play.api.libs.json._
import javax.inject.Inject
import scala.concurrent.Future
import play.api.Play.current
import play.api.mvc._
import play.api.libs.ws._

import play.api._
import play.api.mvc._
 

import play.api.libs.concurrent.Execution.Implicits._
import java.util.concurrent.TimeoutException


class Application @Inject() (ws: WSClient) extends Controller {
        
    case class Place(id: Int, name: String)
    
    val places = List(
        Place(1, "Latigo Beach"),
        Place(2, "Neptune's Net"),
        Place(3, "Badhi's Beachhouse")
    )
    
    implicit val placesWrites = Json.writes[Place]
    
    def listPlaces = Action {
        val json = Json.toJson(places)
        Ok(json)
    }
    
    
    //getWeather function
    def getWeather(city: String) = Action.async {
        println(s"Hello, world! $city") 
        val url = (s"http://api.openweathermap.org/data/2.5/weather?q=$city")
        println(url) 
      val request = WS.url(url).get
 
      request map { response => 
        Ok(response.json)
      } 
      
    
  }
  



    def index = Action {
        Ok(views.html.index("Your new application is ready."))
    }

}
