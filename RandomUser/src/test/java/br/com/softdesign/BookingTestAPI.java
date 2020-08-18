package br.com.softdesign;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BookingTestAPI {

    private final String RESERVA_JSON = "{" +
            "    \"firstname\" : \"Henrique\",\n" +
            "    \"lastname\" : \"Almeida\",\n" +
            "    \"totalprice\" : 80,\n" +
            "    \"depositpaid\" : false,\n" +
            "    \"bookingdates\" : {\n" +
            "        \"checkin\" : \"2020-07-07\",\n" +
            "        \"checkout\" : \"2020-10-10\"\n" +
            "    }," +
            "    \"additionalneeds\" : \"Lunch\"" +
            "}";

    @BeforeClass
    public static void adicionarParametroPadrao() {

        RestAssured.baseURI = "https://restful-booker.herokuapp.com/";
        RestAssured.basePath = "booking/";
    }

    @Test
    public void listarReservas() {

        Map<String, Integer> objetoMap = given().when().get().jsonPath().getMap("[0]");

        assertFalse(objetoMap.isEmpty());
        assertTrue(objetoMap.containsKey("bookingid"));
    }

    @Test
    public void criarReserva() {

        given().log().body().contentType(ContentType.JSON).body(RESERVA_JSON)
                .when().post()
                .then().log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("$", hasKey("bookingid"))
                .body("bookingid", notNullValue())
                .body("bookingid", isA(Integer.class))
                .body("booking.firstname", equalToIgnoringCase("henrique"));
    }

    @Test
    public void validarReservaCriada() {

        int idCriado = given().contentType(ContentType.JSON).body(RESERVA_JSON)
                .when()
                .post()
                .jsonPath().getInt("bookingid");

        given().contentType(ContentType.JSON)
                .when().get()
                .then().log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("find{it.bookingid == " + idCriado + "}", notNullValue())
                .body("find{it.bookingid == " + idCriado + "}.bookingid", equalTo(idCriado));
    }

    @Test
    public void excluirReserva() {

        int idValido = given().when().get().jsonPath().getInt("bookingid[0]");

        given().log().all().contentType(ContentType.JSON).auth().preemptive().basic("admin", "password123")
                .when().delete(Integer.toString(idValido))
                .then().log().status()
                .statusCode(HttpStatus.SC_CREATED)
                .body(containsString("Created"));
    }
}
