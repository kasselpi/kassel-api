package com.kasselapi.data;

import java.util.Date;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

@Path("/sensorservice")
public class RaspiService {

	/*
	 * MongoDB connection
	 */
	MongoClient client = new MongoClient(
			new MongoClientURI("mongodb://kasselpi:kpi@ds129442.mlab.com:29442/kpisensors"));
	MongoDatabase db = client.getDatabase("kpisensors");
	
	/*
	 * Access to collection
	 */
	MongoCollection<Document> sensorsDB = db.getCollection("sensors");
	
	@GET
	@Produces("application/json")
	public Response findAllSensors() throws JSONException {

		JSONArray data = new JSONArray();

		sensorsDB.find().forEach(new Block<Document>() {
			@Override
			public void apply(final Document document) {
				JSONObject doc = new JSONObject(document.toJson());
				data.put(doc);
			}
		});

		return Response.ok(data.toString()).build();
	}
	
	@GET
	@Path("/{id}")
	@Produces("application/json")
	public Response findSensorById(@PathParam("id") String id) throws JSONException {

		JSONArray data = new JSONArray();

		sensorsDB.find(new Document("_id", new ObjectId(id))).forEach(new Block<Document>() {
			@Override
			public void apply(final Document document) {
				JSONObject doc = new JSONObject(document.toJson());
				data.put(doc);
			}
		});

		return Response.ok(data.toString()).build();
	}
	
	@POST
	@Produces("application/json")
	public Response addSensor(String s) throws JSONException {

		JSONObject json = new JSONObject(s);
		JSONObject locLatLng = json.getJSONObject("coordinates");

		// Convert params to document
		Document data = new Document("location", json.get("location"))
				.append("coordinates", new Document("lat", locLatLng.getDouble("lat"))
				.append("lng", locLatLng.getDouble("lng")))
				.append("lastModified", new Date());

		// Add sensor to db
		sensorsDB.insertOne(data);

		return Response.status(200).build();
	}
	
	@DELETE
	@Path("/{id}")
	@Produces("application/json")
	public Response deleteSensor(@PathParam("id") String id) {

		sensorsDB.deleteOne(new Document("_id", new ObjectId(id)));
		return Response.status(200).build();

	}
	
	
}
