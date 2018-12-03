package com.plinz.photos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
public class PhotosController {

	@Autowired
    public SimpMessageSendingOperations simpMessageSendingOperations;
	
	@Autowired
	public TopicSendReceive topicSendReceive;
	
    @MessageMapping("/send-photos")
    public void receiveGreeting(String message) throws Exception {
    	topicSendReceive.startProducer(message);
    }
    
    public String sendGreeting(String message) throws Exception {
        Thread.sleep(1000);
        simpMessageSendingOperations.convertAndSend("/topic/upload-photos", message);
        return message;
    }
    

}
