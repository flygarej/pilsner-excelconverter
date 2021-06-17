/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nu.pilsner.service;

import io.quarkus.scheduler.Scheduled;
import javax.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

/**
 *
 * @author flax
 */
@ApplicationScoped
public class SchedulerService {
    
    public static final Logger log = Logger.getLogger(SchedulerService.class);

    @Scheduled(every="60s") 
    public void heartbeat() {
        log.info("Heartbeat: alive");
    }

}
