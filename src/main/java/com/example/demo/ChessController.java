package com.example.demo;

import com.example.demo.ai.Bot;
import com.example.demo.ai.objects.BestMove;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChessController {

    @GetMapping("/getMove")
    public HttpEntity<? extends Object> getMove(@RequestParam("fen") String fen) {
        BestMove result = Bot.getMove(fen);

//        ObjectMapper mapper = new ObjectMapper();
//        SimpleModule module = new SimpleModule();
//        module.addSerializer(BestMove.class, new BestMove.BestMoveSerializer());
//        mapper.registerModule(module);
//
//        String json = null;
//        try {
//            json = mapper.writeValueAsString(result);
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }

//        return new ResponseEntity<>(result, HttpStatus.OK);

//        System.out.println("Result: " + json);
        return ResponseEntity.ok().body(result.toString());
//        return result;
    }

}
