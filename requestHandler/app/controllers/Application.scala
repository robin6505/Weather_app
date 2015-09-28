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
        
    val apiKey = "&APPID=2058452e3cf07873e426f1d723339ec6"
    
    
    

    
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
    
    def getData(city: String) = Action {
        val resp = getWeather(city: String)
        println(resp)
        
        val resp2 = getForecast(city: String)
        println(resp2)
        
        //should be possible without await, could give timeout on first try
        val current = Await.result(resp, 500 milliseconds)
        val forecast = Await.result(resp2, 500 milliseconds)
        val weather = current.as[JsObject] ++ forecast.as[JsObject]
        val weather2 = Json.obj(
            "CURRENTWEATHER" -> current,
            "FORECAST" -> forecast)
        
        Ok(weather2)
    }
  



    def index = Action {
        Ok(views.html.index("Your new application is ready."))
    }

}
