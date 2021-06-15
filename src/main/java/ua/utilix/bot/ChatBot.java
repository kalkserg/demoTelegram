package ua.utilix.bot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ua.utilix.model.User;
import ua.utilix.service.UserService;

import java.util.List;

@Component
@PropertySource("classpath:telegram.properties")
public class ChatBot extends TelegramLongPollingBot {

    private static final Logger LOGGER = LogManager.getLogger(ChatBot.class);

    private static final String BROADCAST = "broadcast ";
    private static final String LIST_USERS = "list";

    public static final String ADD_REQUEST = "add";
    public static final String REMOVE_REQUEST = "del";

    boolean isDel = false;

    @Value("${bot.name}")
    private String botName;

    @Value("${bot.token}")
    private String botToken;

    private final UserService userService;

    public ChatBot(UserService userService) {
        this.userService = userService;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText())
            return;

        final String text = update.getMessage().getText();
        final long chatId = update.getMessage().getChatId();

        User user = null;

        String command = null;
        String sigfoxName = null;
        String sigfoxId = null;
        Long Id = null;

        command = parseCommand(text);

        //if(user != null) user =
        //findUserWithNullSigfoxName(users);
        //System.out.println(user.getId());
//        sendMessage(chatId,"chatId " + chatId);
        User[] users = userService.findByChatId(chatId);
//        sendMessage(chatId,"chatId " + users[0].getId());
        try {
            user = users[0];
        }catch (Exception ex){}
        if(user!=null) {if (checkIfAdminCommand(user, text)) return;}
        else {sendMessage(chatId,"No registred user for this chatID");}

        // button add
        if (command.equals(ADD_REQUEST)) {
            sigfoxName = parseSigfoxName(text);
            sigfoxId = parseSigfoxId(text);
            if(sigfoxId == null || sigfoxName == null){
                sendMessage(chatId,"add sigfoxName sigfoxId");
                LOGGER.info("add sigfoxName sigfoxId");
            }else {
                user = userService.findByChatIdAndSigfoxId(chatId, sigfoxId);

                if (user == null) {
                    user = new User(chatId, sigfoxName, sigfoxId);
                    userService.addUser(user);
                    sendMessage(chatId, "Add id registered: " + sigfoxId);
                    LOGGER.info("Add id registered: " + sigfoxId);
                } else {
                    sendMessage(chatId, "Id already has registered: " + sigfoxId + " for " + chatId);
                    LOGGER.info("Id already has registered: " + sigfoxId + " for " + chatId);
                }
            }
        // button del
        } else if (command.equals(REMOVE_REQUEST)) {
            try {
                Id = Long.parseLong(parseSigfoxName(text));
                user = userService.findById(Id);
                userService.delUser(user);
                sendMessage(chatId,"Deleted");
                LOGGER.info("Del id: " + Id);
            } catch (Exception ex) {
                sendMessage(chatId,"There is not such id");
                LOGGER.info("Del id: " + Id);
            }

        }else{
            sendMessage(chatId,(command + "?\r\nadd/del/list/broadcast "));
        }

    }


    private boolean checkIfAdminCommand(User user, String text) {
        try {
            System.out.println("admin " + user.toString());
        }catch (Exception e){}
        System.out.println("text "+text);
//        if (user == null || !user.getAdmin())
        if (user == null)
            return false;

        if (text.startsWith(BROADCAST)) {
            LOGGER.info("Admin command received: " + BROADCAST);

            text = text.substring(BROADCAST.length());
            broadcast(text);

            return true;
        } else if (text.equals(LIST_USERS)) {
            LOGGER.info("Admin command received: " + LIST_USERS);

            listUsers(user);
            return true;
        }

        return false;
    }

    private void sendMessage(Long chatId, String text) {
        var message = SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(text)
                .build();
        message.setParseMode("HTML");
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void listUsers(User admin) {
        StringBuilder sb = new StringBuilder("chatId "+ admin.getChatId() + "\r\n" + "All users list:\r\n");
        List<User> users = userService.findAllUsers();

        users.forEach(user ->
            sb.append(user.getId())
                    .append(" - <b>")
                    .append(user.getSigfoxName())
                    .append("</b> SigfoxId: ")
                    .append(user.getSigfoxId())
                    .append(" chatId: ")
                    .append(user.getChatId())
                    .append("\r\n")
        );

        sendMessage(admin.getChatId(), sb.toString());
    }

    private void broadcast(String text) {
        List<User> users = userService.findAllUsers();
        users.forEach(user -> sendMessage(user.getChatId(), text));
    }

    private User findUserById(User[] users, long id) {
        for (int i = 0; i < users.length; i++) {
            //System.out.println(users[i].getId() + " " + users[i].getChatId() + " " + users[i].getSigfoxName() + " " + users[i].getSigfoxId() +" " + users[i].getStateId());
            if (users[i].getId() == id) {
                System.out.println( " find " + users[i].getId());
                return users[i];
            }
            System.out.println( " f " + users[i].getId());
        }
        return null;
    }

    private User getFirstUserWithChatId(long id) {
        User user = null;
        User[] users = userService.findByChatId(id);
        try {
            user = users[users.length-1];
        }catch (Exception e){}

        return user;
    }

    private String parseCommand(String text){
        String[] subStr;
        String delimeter = " "; // Разделитель
        subStr = text.split(delimeter); // Разделения строки str с помощью метода split()
        return subStr[0];
    }

    private String parseSigfoxName(String text){
        String[] subStr;
        String delimeter = " "; // Разделитель
        subStr = text.split(delimeter); // Разделения строки str с помощью метода split()
        try {
            return subStr[1];
        }catch (Exception ex) {
            return null;
        }
    }

    private String parseSigfoxId(String text){
        String[] subStr;
        String delimeter = " "; // Разделитель
        subStr = text.split(delimeter); // Разделения строки str с помощью метода split()
        try{
            return subStr[2];
        }catch (Exception ex) {
            return null;
        }
    }

}
