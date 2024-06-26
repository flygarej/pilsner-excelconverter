package nu.pilsner;

import io.micrometer.core.instrument.MeterRegistry;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import nu.pilsner.service.POIService;
import org.jboss.logging.Logger;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

@Path("/pilsner/excel")
public class GreetingResource {
    
    private final MeterRegistry registry;

    GreetingResource(MeterRegistry registry) {
        this.registry = registry;
        
    }

    private static final Logger log = Logger.getLogger(GreetingResource.class);

    @Inject
    POIService poiservice;

//    @POST
//    @Consumes({MediaType.MULTIPART_FORM_DATA})
//    @Produces(MediaType.TEXT_PLAIN)
//    @Path("/upload2")
//    public Response convertFileDontUse(@MultipartForm FormData input) throws IOException {
//        
//        if (input != null) 
//        {
//            if (input.file!= null) {
//                File file = input.file;
//                System.out.println("File length: " + file.length());
//                System.out.println("File name: " + file.getName());
//            }
//            if (input.slim!=null) {
//                System.out.println("slim="+input.slim);
//            }
//            if (input.filename!=null) {
//                System.out.println("filename: " + input.filename);
//            }
//        }
//        return Response.ok("Ok").build();
//    
//    }    
    @POST
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/upload")
    public Response convertFile(MultipartFormDataInput input) {
        String body = "Failed to convert excel to text";
        String fileName = "";
        Boolean noJudgement = true;
        Boolean withDate = false;

        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();

        // Get slim
        List<InputPart> inputParts = uploadForm.get("checkboxvalue");
        if (inputParts != null) {
            for (InputPart ip : inputParts) {
                try {
                    if (ip.getBodyAsString() != null) {
                        noJudgement = false;
                    }
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(GreetingResource.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        inputParts = uploadForm.get("withdate");
        if (inputParts != null) {
            for (InputPart ip : inputParts) {
                try {
                    if (ip.getBodyAsString() != null) {
                        withDate = true;
                    }
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(GreetingResource.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        inputParts = uploadForm.get("file");

        InputStream inputStream = null;
        for (InputPart inputPart : inputParts) {

            try {

                MultivaluedMap<String, String> header = inputPart.getHeaders();
                fileName = getFileName(header);
                if (!fileName.endsWith(".xlsx")) {
                    return Response.status(200)
                            .entity("Filen måste ha filändelsen .xlsx").type(MediaType.TEXT_PLAIN + "; charset=UTF-8").build();
                }
                log.info("Got request to convert excel file " + fileName);
                StringBuilder sb = new StringBuilder();
                sb.append("Extracting records from file " + fileName + "\n\n");
                //convert the uploaded file to inputstream
                inputStream = inputPart.getBody(InputStream.class, null);

                poiservice.parse(inputStream, sb, noJudgement, withDate);
                registry.counter("pilsnerconverter.files.uploaded").increment();
                return Response.status(Response.Status.OK).entity(sb.toString()).type(MediaType.TEXT_PLAIN + "; charset=UTF-8").build();

            } catch (IOException e) {
                e.printStackTrace();
                log.info("Failed conversion");
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException ex) {
                        java.util.logging.Logger.getLogger(GreetingResource.class.getSimpleName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

        }

        return Response.status(200)
                .entity(body).type(MediaType.TEXT_PLAIN + "; charset=UTF-8").build();

    }

    private String getFileName(MultivaluedMap<String, String> header) {

        String[] contentDisposition = header.getFirst("Content-Disposition").split(";");

        for (String filename : contentDisposition) {
            if ((filename.trim().startsWith("filename"))) {

                String[] name = filename.split("=");

                String finalFileName = name[1].trim().replaceAll("\"", "");
                return finalFileName;
            }
        }
        return "unknown";
    }

}
