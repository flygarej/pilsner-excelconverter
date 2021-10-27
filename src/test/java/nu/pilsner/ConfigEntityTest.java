package nu.pilsner;

import io.quarkus.test.junit.QuarkusTest;
import java.util.List;
import nu.pilsner.service.ConfigEntity;
import nu.pilsner.service.ConfigEntity.HEADERNAME;
import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author flax
 */
@QuarkusTest
public class ConfigEntityTest {
    
    @Test
    void testCOLUMNNAMES() {
        // Test that we can get each known column name
        List<String> knownColumns = HEADERNAME.getHeaders();
        for (String name : knownColumns) {
            HEADERNAME hname = HEADERNAME.getValue(name);
            System.out.println("Name " + name + " should be in enum value " + hname.name());
            assertTrue(hname.checkValue(name));
        }
    }
    
}
