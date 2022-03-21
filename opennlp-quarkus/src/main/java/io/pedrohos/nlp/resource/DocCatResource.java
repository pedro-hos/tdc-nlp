package io.pedrohos.nlp.resource;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.pedrohos.nlp.doccat.GeDocCat;

@Path("/nlp/doccat")
public class DocCatResource {

	@Inject
	GeDocCat mydoccat;
	
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response geCat(@QueryParam("text") String text) {
        return Response.ok().entity(GeDocCat.classifyText(text)).build();
    }
    
}