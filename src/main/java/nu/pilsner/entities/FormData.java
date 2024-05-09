/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nu.pilsner.entities;

import java.io.File;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.annotations.providers.multipart.PartType;

/**
 *
 * @author flax
 */
public class FormData {
    @FormParam("file")
    @PartType(MediaType.APPLICATION_OCTET_STREAM)
    public File file;
    
    @FormParam("filename")
    @PartType(MediaType.TEXT_PLAIN)
    public String filename;
    
    @FormParam("slim")
    @PartType(MediaType.TEXT_PLAIN)
    public String slim;
}
