package ua.utilix.bot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ua.utilix.model.User;
import ua.utilix.service.UserService;

import java.util.ArrayList;
import java.util.List;

@Component
@PropertySource("classpath:telegram.properties")
public class ChatBot extends TelegramLongPollingBot {

    private static final Logger LOGGER = LogManager.getLogger(ChatBot.class);

    private static final String BROADCAST = "broadcast ";
    private static final String LIST_USERS = "users";

    public static final String ADD_REQUEST = "Add";
    public static final String REMOVE_REQUEST = "Del";

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
        boolean  isDel2 = false;

        User[] users = userService.findByChatId(chatId);
        try {
            user = users[users.length-1];
        }catch (Exception e){}

        //if(user != null) user =
        findUserWithNullSigfoxName(users);

        if (checkIfAdminCommand(user, text))
            return;

        BotContext context = null;
        BotState state = null;

        System.out.println(update.getMessage().getText());

        if (user == null) {
            state = BotState.getInitialState();

            user = new User(chatId, text, state.ordinal());
            userService.addUser(user);

            context = BotContext.of(this, user, text);
            state.enter(context);
//            isDel = false;
            LOGGER.info("New user registered: " + chatId);
        } else if (update.getMessage().getText().equals(ADD_REQUEST)) {
            state = BotState.Start;
            //state = BotState.getInitialState();

            user = new User(chatId, state.ordinal());
            userService.addUser(user);

            context = BotContext.of(this, user, text);
            state.enter(context);
            isDel = false;
            isDel2 = false;
            LOGGER.info("Add id registered: " + chatId);
        } else if (update.getMessage().getText().equals(REMOVE_REQUEST)) {
            //state = BotState.Start;
            state = BotState.BeginRemoving;
            user = new User(chatId, state.ordinal());
            //long id = Long.parseLong(text)
            //user = findUserById(users,id);
            //userService.delUser(user);
            isDel = true;
            context = BotContext.of(this, user, text);
            state.enter(context);
//            System.out.println("getid "+context.getUser().getId());
//            System.out.println("getinput " + context.getInput());
//            System.out.println("getBotname " + context.getBot().botName);

            LOGGER.info("Del state: " + state);
        } else if(!(isDel)){
            System.out.println("isDel false");
            context = BotContext.of(this, user, text);
            state = BotState.byId(user.getStateId());
            LOGGER.info("Update received for user in state: " + state);
        } else if(isDel) {
            System.out.println("isDel true" +  Long.parseLong(text));
            user = findUserById(users, Long.parseLong(text));
            //System.out.println("state " + state.ordinal());
            user.setStateId(BotState.BeginRemoving.ordinal());
            context = BotContext.of(this, user, text);
            state = BotState.byId(user.getStateId());
            isDel2 = true;
            LOGGER.info("DEl received for user in state: " + state);
        }

        state.handleInput(context);

        //save chain doing
        if(state.nextState() == BotState.Start || state.nextState() == BotState.EnterSigfoxName || state.nextState() == BotState.EnterSigfoxID || state.nextState() == BotState.Registred) {
            do {
                state = state.nextState();
                state.enter(context);
            } while (!state.isInputNeeded());

            user.setStateId(state.ordinal());
            userService.updateUser(user);
        }
        //delete chain doing
        else{
            //if(isDel)
            if(state.nextState() == BotState.BeginRemoving || state.nextState() == BotState.Removing || state.nextState() == BotState.Removed) {
                do {
                    state = state.nextState();
                    state.enter(context);
                    System.out.println("next state  " + context.getUser().getStateId() + " " + context.getUser().getChatId());
                } while (!state.isInputNeeded());
                if(isDel2) {
                    userService.delUser(context.getUser());
                    isDel2 = false;
                    isDel = false;
                }
            }

        }

    }




    private boolean checkIfAdminCommand(User user, String text) {
        try {
            System.out.println("admin " + user.toString());
        }catch (Exception e){}

        if (user == null || !user.getAdmin())
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

    private void sendMessage(String chatId, String text) {
        var message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(getMainMenu())
                .build();
        message.setParseMode("HTML");
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void listUsers(User admin) {
        StringBuilder sb = new StringBuilder("All users list:\r\n");
        List<User> users = userService.findAllUsers();

        users.forEach(user ->
            sb.append(user.getId())
                    .append(" - <b>")
                    .append(user.getSigfoxName())
                    .append("</b> SigfoxId: ")
                    .append(user.getSigfoxId())
                    .append("\r\n")
        );

        sendMessage(String.valueOf(admin.getChatId()), sb.toString());
    }

    private void broadcast(String text) {
        List<User> users = userService.findAllUsers();
        users.forEach(user -> sendMessage(String.valueOf(user.getChatId()), text));
    }

    protected ReplyKeyboardMarkup getMainMenu(){
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        KeyboardRow row1 = new KeyboardRow();
        row1.add(ADD_REQUEST);
        row1.add(REMOVE_REQUEST);

        List<KeyboardRow> rows = new ArrayList<>();
        rows.add(row1);
        markup.setKeyboard(rows);
        markup.setResizeKeyboard(true);
        return markup;
    }

    private User findUserWithNullSigfoxName(User[] users) {
        for (int i = 0; i < users.length; i++) {
            System.out.println(users[i].getId() + " " + users[i].getChatId() + " " + users[i].getSigfoxName() + " " + users[i].getSigfoxId() +" " + users[i].getStateId());
            if (users[i].getSigfoxName() == null)
                return users[i];
        }
        return null;
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
}
