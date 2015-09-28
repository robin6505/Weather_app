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

import scala.concurrent.duration._
import scala.concurrent.Await


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
    def getWeather(city: String) : JsValue = {
        import scala.util.{Success, Failure}
        println(s"Hello, world! $city") 
        val url = (s"http://api.openweathermap.org/data/2.5/weather?q=$city")
        println(url) 
        val request = WS.url(url).get
        println(request)
        
        //Optie1
        var weatherResponse: JsValue = Json.obj()
        request onComplete {
          case Success(posts) => {
              println(posts.body)
              weatherResponse = Json.toJson(posts.body)
          }
          case Failure(t) => println("An error has occured: " + t.getMessage)
        }
        
        //Probleem is nu dat hij returned voordat request onComplete klaar is.
        println(weatherResponse)
        return weatherResponse
        
        //Optie2 Dit werkt wel met de return door de Await functie, maar is niet mooi
        //val future = request map {
        //  response => (response.json)
        //}
        //val result = Await.result(future, 2 seconds)
        //return result

        

    
    }
    
    def getData(city: String) = Action {
        val resp = getWeather(city: String)
        Ok(resp)
    }
  



    def index = Action {
        Ok(views.html.index("Your new application is ready."))
    }

}
