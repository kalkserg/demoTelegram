package ua.utilix.controller;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ua.utilix.model.User;
import ua.utilix.service.SendMessageService;
import ua.utilix.service.UserService;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Controller
public class myController {

    private UserService userService;
    private SendMessageService sendMessageService;

    @Autowired
    public void setSendMessageService(SendMessageService sendMessageService) {
        this.sendMessageService = sendMessageService;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    String text ="";
//    @RequestMapping(value = "/greeting")
//    public String helloWorldController(@RequestParam(name = "name", required = false, defaultValue = "World!") String name, Model model) {
//        model.addAttribute("name", name);
//        return "greeting";
//    }

//    @RequestMapping()
    /*
    @PostMapping()
    public void postBody(@RequestBody(required = false) String str, Model model) {
        System.out.println("post  " + str);

        if (str!=null) { text = text + str + "\n";model.addAttribute("str", text);}
        else model.addAttribute("str", "EMPTY");
        //return "sample";
    }

    //{"device" : "{device}","time" : "{time}","data" : "{data}","seqNumber" : "{seqNumber}","lqi" : "{lqi}","operatorName" : "{operatorName}"}

    @GetMapping()
    public void getBody(@RequestBody(required = false) String str, Model model) {
        System.out.println("get" + str);
        model.addAttribute("str", text);

        //HelloWorldBot helloWorldBot = new HelloWorldBot();

        //return "byby";
    },



     */
    @PostMapping()
    public ResponseEntity<String> postBody(@RequestBody(required = false) String str, Model model) {
        System.out.println("post  " + str);
        //Message m = new Message();
        //m.setText(str);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        SendMessage sm = new SendMessage();

        JSONObject obj = new JSONObject(str);
        String sigfoxId = obj.getString("device");
        String unixTime = obj.getString("time");
        String data = obj.getString("data");
//        String sigfoxName = userService.findBySigfoxId(sigfoxId).getSigfoxName();
//        final String formattedDtm = Instant.ofEpochSecond(Long.parseLong(unixTime))
//                .atZone(ZoneId.of("GMT+3"))
//                .format(formatter);
//
//        //sm.setText("<b>Device</b> " + sigfoxId + " push button at " + formattedDtm + " (<i>data " + data + "</i>)");
//        sm.setText("<b>"+ sigfoxName + "</b> " + " push button" + sigfoxId);
//        sm.setParseMode("HTML");
//        User user = userService.findBySigfoxId(sigfoxId);
//        User user = userService.findByChatId(1263775963);
//        List<User> list = userService.findAllUsers();
//        System.out.println(list.get(0).getChatId());
//        System.out.println(list.get(0).getSigfoxId());
        try {
            String sigfoxName = userService.findBySigfoxId(sigfoxId).getSigfoxName();
            final String formattedDtm = Instant.ofEpochSecond(Long.parseLong(unixTime))
                    .atZone(ZoneId.of("GMT+3"))
                    .format(formatter);

            //sm.setText("<b>Device</b> " + sigfoxId + " push button at " + formattedDtm + " (<i>data " + data + "</i>)");
            sm.setText("<b>"+ sigfoxName + "</b> " + " push button " + sigfoxId);
            sm.setParseMode("HTML");
            User user = userService.findBySigfoxId(sigfoxId);
            sm.setChatId(String.valueOf(user.getChatId()));

            //sm.setChatId(String.valueOf(1263775963));

            //sendMessageService.test2(m);
            //System.out.println(sm.getChatId());
            sendMessageService.test3(sm);
        }catch (Exception e){
            System.out.println("There is not user");
        }
        return new ResponseEntity<String>(HttpStatus.OK);

//        if (str!=null) { text = text + str + "\n";model.addAttribute("str", text);}
//        else model.addAttribute("str", "EMPTY");
//        return "sample";

    }

}
