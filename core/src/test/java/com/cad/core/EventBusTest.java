/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */

package com.cad.core;


import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import com.cad.core.api.EventBus;
 
/**
 *
 * @author marconardes
 */
public class EventBusTest {

    private class ExampleEvent {
        private final String message;
    
        public ExampleEvent(String message) {
            this.message = message;
        }
    
        public String getMessage() {
            return message;
        }
    }

    @Test
    public void testEventBus() {
        EventBus eventBus = new EventBus();
        ExampleEvent event = new ExampleEvent("Hello, world!");
        StringBuilder message = new StringBuilder();
        eventBus.registerListener(ExampleEvent.class, e -> message.append(e.getMessage()));
        eventBus.publishEvent(event);
        assertEquals("Hello, world!", message.toString());
    }

    

}

